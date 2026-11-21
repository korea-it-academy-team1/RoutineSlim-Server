package org.routineslim.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.community.dto.CommentDTO;
import org.routineslim.domain.community.dto.CommentRequestDTO;
import org.routineslim.domain.community.dto.PostDTO;
import org.routineslim.domain.community.dto.PostRequestDTO;
import org.routineslim.domain.community.entity.Comment;
import org.routineslim.domain.community.entity.Post;
import org.routineslim.domain.community.entity.PostBoard;
import org.routineslim.domain.community.entity.RecruitStatus;
import org.routineslim.domain.community.repository.CommentRepository;
import org.routineslim.domain.community.repository.PostRepository;
import org.routineslim.domain.user.entity.User;
import org.routineslim.domain.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class CommunityServiceImpl implements CommunityService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final int MAX_COMMENT_LENGTH = 1000;
    private static final int MAX_SERVICE_NAME_LENGTH = 100;
    private static final int MIN_PARTY_MEMBERS = 2;
    private static final int MAX_PARTY_MEMBERS = 10;
    private static final int MAX_PRICE = 10_000_000;

    private static final String UNKNOWN_AUTHOR = "알 수 없음";
    private static final String WITHDRAWN_AUTHOR = "탈퇴한 회원";

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    // 게시글 목록 (board 가 null 이면 전체, 최신순)
    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getPosts(UUID memberId, String board, int page, int size) {

        // 페이지 확인
        if (page < 0) {
            throw new IllegalArgumentException("page 는 0 이상이어야 합니다.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size 는 1~" + MAX_PAGE_SIZE + " 사이여야 합니다.");
        }
        PageRequest pageRequest = PageRequest.of(page, size);

        // 게시판별 또는 전체 조회
        List<Post> posts = (board == null || board.isBlank())
                ? postRepository.findAllByOrderByCreatedAtDescPostIdDesc(pageRequest)
                : postRepository.findByBoardOrderByCreatedAtDescPostIdDesc(parseBoard(board), pageRequest);

        if (posts.isEmpty()) {
            return List.of();
        }

        // 댓글 수·작성자 이름을 한 번에 조회 (게시글마다 따로 조회하지 않는다)
        List<Long> postIds = posts.stream().map(Post::getPostId).toList();
        Map<Long, Long> commentCounts = new HashMap<>();
        for (Object[] row : commentRepository.countByPostIds(postIds)) {
            commentCounts.put((Long) row[0], (Long) row[1]);
        }
        Map<UUID, User> authors = findUsers(posts.stream().map(Post::getMemberId).toList());

        return posts.stream()
                .map(post -> toDTO(post, memberId, authors, commentCounts.getOrDefault(post.getPostId(), 0L)))
                .toList();
    }

    // 게시글 상세
    @Override
    @Transactional(readOnly = true)
    public PostDTO getPost(UUID memberId, Long postId) {
        Post post = findPost(postId);
        return toDTO(post, memberId);
    }

    // 게시글 작성
    @Override
    public PostDTO createPost(UUID memberId, PostRequestDTO postRequestDTO) {

        // 회원 확인 (없는 회원으로 쓰면 FK 오류 500 대신 400)
        userRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Post post = Post.builder()
                .memberId(memberId)
                .viewCount(0)
                .createdAt(LocalDateTime.now(clock).withNano(0))
                .build();

        // 입력값 확인 후 반영 (작성 시 모집 상태는 항상 모집 중)
        applyRequest(post, postRequestDTO, true);

        // DB 저장
        return toDTO(postRepository.save(post), memberId);
    }

    // 게시글 수정 (작성자만)
    @Override
    public PostDTO updatePost(UUID memberId, Long postId, PostRequestDTO postRequestDTO) {

        Post post = findPost(postId);
        checkWriter(post.getMemberId(), memberId, "게시글");

        // 입력값 확인 후 반영
        applyRequest(post, postRequestDTO, false);

        // DB 저장
        return toDTO(postRepository.saveAndFlush(post), memberId);
    }

    // 게시글 삭제 (작성자만, 댓글도 함께 삭제)
    @Override
    public void deletePost(UUID memberId, Long postId) {

        Post post = findPost(postId);
        checkWriter(post.getMemberId(), memberId, "게시글");

        // comment 가 post 를 FK 로 참조하므로 댓글부터 삭제
        commentRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    // 조회수 +1
    @Override
    public void increaseViewCount(Long postId) {
        if (postRepository.increaseViewCount(postId) == 0) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
    }

    // 댓글 목록 (최신순)
    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getComments(UUID memberId, Long postId) {

        // 게시글 확인
        findPost(postId);

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDescCommentIdDesc(postId);
        Map<UUID, User> authors = findUsers(comments.stream().map(Comment::getMemberId).toList());

        return comments.stream()
                .map(comment -> toCommentDTO(comment, memberId, authors))
                .toList();
    }

    // 댓글 작성
    @Override
    public CommentDTO createComment(UUID memberId, Long postId, CommentRequestDTO commentRequestDTO) {

        // 회원·게시글 확인
        userRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        findPost(postId);

        // 내용 확인
        String content = requireText(commentRequestDTO.getContent(), "댓글 내용", MAX_COMMENT_LENGTH);

        Comment comment = Comment.builder()
                .postId(postId)
                .memberId(memberId)
                .content(content)
                .createdAt(LocalDateTime.now(clock).withNano(0))
                .build();

        // DB 저장
        Comment saved = commentRepository.save(comment);
        return toCommentDTO(saved, memberId, findUsers(List.of(memberId)));
    }

    // 댓글 삭제 (작성자만)
    @Override
    public void deleteComment(UUID memberId, Long postId, Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .filter(c -> c.getPostId().equals(postId))
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        checkWriter(comment.getMemberId(), memberId, "댓글");

        commentRepository.delete(comment);
    }

    // ------------------------------------------------------------------------

    // 요청 값 확인 후 엔티티에 반영
    private void applyRequest(Post post, PostRequestDTO request, boolean creating) {

        if (request == null) {
            throw new IllegalArgumentException("요청 내용이 없습니다.");
        }

        PostBoard board = parseBoard(request.getBoard());
        post.setBoard(board);
        post.setTitle(requireText(request.getTitle(), "제목", MAX_TITLE_LENGTH));
        post.setContent(requireText(request.getContent(), "내용", MAX_CONTENT_LENGTH));

        // 후기·자유 글은 파티 정보를 비운다
        if (board != PostBoard.PARTY) {
            post.setServiceName(null);
            post.setRecruitStatus(null);
            post.setCurrentMembers(null);
            post.setMaxMembers(null);
            post.setTotalPrice(null);
            post.setPricePerMember(null);
            post.setSettlementDay(null);
            return;
        }

        // 파티원 모집 정보 확인
        String serviceName = requireText(request.getServiceName(), "구독 서비스 이름", MAX_SERVICE_NAME_LENGTH);

        Integer maxMembers = request.getMaxMembers();
        if (maxMembers == null || maxMembers < MIN_PARTY_MEMBERS || maxMembers > MAX_PARTY_MEMBERS) {
            throw new IllegalArgumentException("모집 인원은 " + MIN_PARTY_MEMBERS + "~" + MAX_PARTY_MEMBERS + "명이어야 합니다.");
        }

        // 현재 인원은 비우면 작성자 1명
        Integer currentMembers = request.getCurrentMembers() == null ? 1 : request.getCurrentMembers();
        if (currentMembers < 1 || currentMembers > maxMembers) {
            throw new IllegalArgumentException("현재 인원은 1명 이상, 모집 인원 이하여야 합니다.");
        }

        Integer pricePerMember = request.getPricePerMember();
        if (pricePerMember == null || pricePerMember < 0 || pricePerMember > MAX_PRICE) {
            throw new IllegalArgumentException("1인당 분담금은 0~" + MAX_PRICE + "원이어야 합니다.");
        }

        Integer totalPrice = request.getTotalPrice();
        if (totalPrice != null && (totalPrice < 0 || totalPrice > MAX_PRICE)) {
            throw new IllegalArgumentException("전체 요금은 0~" + MAX_PRICE + "원이어야 합니다.");
        }

        Integer settlementDay = request.getSettlementDay();
        if (settlementDay != null && (settlementDay < 1 || settlementDay > 31)) {
            throw new IllegalArgumentException("정산일은 1~31 사이여야 합니다.");
        }

        // 모집 상태: 작성 시 모집 중, 수정 시 보내면 바꾸고 안 보내면 그대로 (후기·자유 → 파티로 바꾸면 모집 중)
        RecruitStatus recruitStatus;
        if (creating || request.getRecruitStatus() == null || request.getRecruitStatus().isBlank()) {
            recruitStatus = (creating || post.getRecruitStatus() == null) ? RecruitStatus.RECRUITING : post.getRecruitStatus();
        } else {
            recruitStatus = parseRecruitStatus(request.getRecruitStatus());
        }

        post.setServiceName(serviceName);
        post.setRecruitStatus(recruitStatus);
        post.setCurrentMembers(currentMembers);
        post.setMaxMembers(maxMembers);
        post.setTotalPrice(totalPrice);
        post.setPricePerMember(pricePerMember);
        post.setSettlementDay(settlementDay);
    }

    // 게시글 조회 (없으면 400)
    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    // 작성자 확인
    private void checkWriter(UUID writerId, UUID memberId, String target) {
        if (writerId == null || !writerId.equals(memberId)) {
            throw new IllegalArgumentException("본인이 쓴 " + target + "만 수정·삭제할 수 있습니다.");
        }
    }

    // 필수 문자열 확인 (앞뒤 공백 제거)
    private String requireText(String value, String label, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(label + "을(를) 입력해 주세요.");
        }
        if (text.length() > maxLength) {
            throw new IllegalArgumentException(label + "은(는) " + maxLength + "자 이하여야 합니다.");
        }
        return text;
    }

    private PostBoard parseBoard(String board) {
        if (board == null || board.isBlank()) {
            throw new IllegalArgumentException("게시판(board)은 필수입니다.");
        }
        try {
            return PostBoard.valueOf(board);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("게시판은 PARTY, REVIEW, FREE 중 하나여야 합니다.");
        }
    }

    private RecruitStatus parseRecruitStatus(String recruitStatus) {
        try {
            return RecruitStatus.valueOf(recruitStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("모집 상태는 RECRUITING, CLOSED 중 하나여야 합니다.");
        }
    }

    // 회원 여러 명 조회 (null 은 건너뜀)
    private Map<UUID, User> findUsers(Collection<UUID> memberIds) {
        Set<UUID> ids = memberIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getMemberId, Function.identity()));
    }

    // 작성자 이름
    private String authorName(UUID memberId, Map<UUID, User> users) {
        User user = memberId == null ? null : users.get(memberId);
        if (user == null) {
            return UNKNOWN_AUTHOR;
        }
        if (user.getDeletedAt() != null) {
            return WITHDRAWN_AUTHOR;
        }
        return user.getName();
    }

    // 엔티티 → DTO (상세·작성·수정)
    private PostDTO toDTO(Post post, UUID memberId) {
        return toDTO(post, memberId, findUsers(List.of(post.getMemberId())),
                commentRepository.countByPostId(post.getPostId()));
    }

    // 엔티티 → DTO
    private PostDTO toDTO(Post post, UUID memberId, Map<UUID, User> authors, long commentCount) {
        return PostDTO.builder()
                .postId(post.getPostId())
                .board(post.getBoard())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(authorName(post.getMemberId(), authors))
                .mine(post.getMemberId().equals(memberId))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .commentCount(commentCount)
                .serviceName(post.getServiceName())
                .recruitStatus(post.getRecruitStatus())
                .currentMembers(post.getCurrentMembers())
                .maxMembers(post.getMaxMembers())
                .totalPrice(post.getTotalPrice())
                .pricePerMember(post.getPricePerMember())
                .settlementDay(post.getSettlementDay())
                .build();
    }

    // 댓글 엔티티 → DTO
    private CommentDTO toCommentDTO(Comment comment, UUID memberId, Map<UUID, User> authors) {
        return CommentDTO.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPostId())
                .authorName(authorName(comment.getMemberId(), authors))
                .mine(comment.getMemberId() != null && comment.getMemberId().equals(memberId))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
