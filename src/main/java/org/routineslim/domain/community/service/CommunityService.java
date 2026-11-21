package org.routineslim.domain.community.service;

import org.routineslim.domain.community.dto.CommentDTO;
import org.routineslim.domain.community.dto.CommentRequestDTO;
import org.routineslim.domain.community.dto.PostDTO;
import org.routineslim.domain.community.dto.PostRequestDTO;
import org.routineslim.domain.community.dto.PartyDecisionRequestDTO;
import org.routineslim.domain.community.dto.PartyRequestDTO;

import java.util.List;
import java.util.UUID;

public interface CommunityService {

    // 게시글 목록 (board 가 null 이면 전체, 최신순)
    // scope: null 전체 / MINE 내가 쓴 글 / COMMENTED 댓글 단 글 / JOINED 참여(요청)한 파티. scope 가 있으면 board 는 무시
    List<PostDTO> getPosts(UUID memberId, String board, String scope, int page, int size);

    // 게시글 상세
    PostDTO getPost(UUID memberId, Long postId);

    // 게시글 작성
    PostDTO createPost(UUID memberId, PostRequestDTO postRequestDTO);

    // 게시글 수정 (작성자만)
    PostDTO updatePost(UUID memberId, Long postId, PostRequestDTO postRequestDTO);

    // 게시글 삭제 (작성자만, 댓글도 함께 삭제)
    void deletePost(UUID memberId, Long postId);

    // 조회수 +1
    void increaseViewCount(Long postId);

    // 댓글 목록 (최신순)
    List<CommentDTO> getComments(UUID memberId, Long postId);

    // 댓글 작성
    CommentDTO createComment(UUID memberId, Long postId, CommentRequestDTO commentRequestDTO);

    // 댓글 삭제 (작성자만)
    void deleteComment(UUID memberId, Long postId, Long commentId);

    // 파티 참여 요청
    PostDTO requestParty(UUID memberId, Long postId);

    // 파티 참여 요청 취소 / 파티 나가기
    PostDTO cancelParty(UUID memberId, Long postId);

    // 파티 참여 요청 목록 (작성자만)
    List<PartyRequestDTO> getPartyRequests(UUID memberId, Long postId);

    // 파티 참여 요청 수락·거절 (작성자만)
    PartyRequestDTO decidePartyRequest(UUID memberId, Long postId, Long requestId, PartyDecisionRequestDTO request);
}
