package org.routineslim.domain.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.community.dto.CommentDTO;
import org.routineslim.domain.community.dto.CommentRequestDTO;
import org.routineslim.domain.community.dto.PostDTO;
import org.routineslim.domain.community.dto.PostRequestDTO;
import org.routineslim.domain.community.dto.PartyDecisionRequestDTO;
import org.routineslim.domain.community.dto.PartyRequestDTO;
import org.routineslim.domain.community.service.CommunityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// 커뮤니티 (게시글·댓글)
// 주소는 웹(RoutineSlim-Web/src/api/communityApi.js)이 쓰는 /api/v1/community/posts 에 맞췄다
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/community/posts")
public class CommunityController {

    private final CommunityService communityService;

    // 게시글 목록 (최신순)
    // 예) GET /api/v1/community/posts?board=PARTY&page=0&size=20   (board 를 빼면 전체)
    // 마이 > 커뮤니티 활동: ?scope=MINE (내가 쓴 글) / COMMENTED (댓글 단 글) / JOINED (참여 요청·참여 중인 파티)
    @GetMapping
    public ResponseEntity<List<PostDTO>> getPosts(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestParam(value = "board", required = false) String board,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(communityService.getPosts(memberId, board, scope, page, size));
    }

    // 게시글 상세
    // 예) GET /api/v1/community/posts/1
    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPost(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId
    ) {
        return ResponseEntity.ok(communityService.getPost(memberId, postId));
    }

    // 게시글 작성 → HTTP 201
    // 예) POST /api/v1/community/posts  {"board":"FREE","title":"...","content":"..."}
    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestBody PostRequestDTO postRequestDTO
    ) {
        PostDTO response = communityService.createPost(memberId, postRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 수정 (작성자만)
    // 예) PUT /api/v1/community/posts/1  (작성과 같은 형식, PARTY 는 recruitStatus 로 모집 완료 처리)
    @PutMapping("/{postId}")
    public ResponseEntity<PostDTO> updatePost(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId,
            @RequestBody PostRequestDTO postRequestDTO
    ) {
        return ResponseEntity.ok(communityService.updatePost(memberId, postId, postRequestDTO));
    }

    // 게시글 삭제 (작성자만, 댓글도 함께) → HTTP 204
    // 예) DELETE /api/v1/community/posts/1
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId
    ) {
        communityService.deletePost(memberId, postId);
        return ResponseEntity.noContent().build();
    }

    // 조회수 +1 (상세 화면에 들어갈 때 1번) → HTTP 204
    // 예) POST /api/v1/community/posts/1/views
    @PostMapping("/{postId}/views")
    public ResponseEntity<Void> increaseViewCount(
            @PathVariable("postId") Long postId
    ) {
        communityService.increaseViewCount(postId);
        return ResponseEntity.noContent().build();
    }

    // 댓글 목록 (최신순)
    // 예) GET /api/v1/community/posts/1/comments
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId
    ) {
        return ResponseEntity.ok(communityService.getComments(memberId, postId));
    }

    // 댓글 작성 → HTTP 201
    // 예) POST /api/v1/community/posts/1/comments  {"content":"저 참여할게요!"}
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDTO> createComment(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId,
            @RequestBody CommentRequestDTO commentRequestDTO
    ) {
        CommentDTO response = communityService.createComment(memberId, postId, commentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 댓글 삭제 (작성자만) → HTTP 204
    // 예) DELETE /api/v1/community/posts/1/comments/3
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId
    ) {
        communityService.deleteComment(memberId, postId, commentId);
        return ResponseEntity.noContent().build();
    }

    // ---- 파티 참여 (PARTY 글만) ----

    // 참여 요청 → 바뀐 게시글 (myPartyStatus = REQUESTED)
    // 예) POST /api/v1/community/posts/1/party
    @PostMapping("/{postId}/party")
    public ResponseEntity<PostDTO> requestParty(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId
    ) {
        return ResponseEntity.ok(communityService.requestParty(memberId, postId));
    }

    // 참여 요청 취소 / 파티 나가기 → 바뀐 게시글
    // 예) DELETE /api/v1/community/posts/1/party
    @DeleteMapping("/{postId}/party")
    public ResponseEntity<PostDTO> cancelParty(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId
    ) {
        return ResponseEntity.ok(communityService.cancelParty(memberId, postId));
    }

    // 참여 요청 목록 (작성자만, 요청 순)
    // 예) GET /api/v1/community/posts/1/party/requests
    @GetMapping("/{postId}/party/requests")
    public ResponseEntity<List<PartyRequestDTO>> getPartyRequests(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId
    ) {
        return ResponseEntity.ok(communityService.getPartyRequests(memberId, postId));
    }

    // 참여 요청 수락·거절 (작성자만)
    // 예) PATCH /api/v1/community/posts/1/party/requests/5  {"status":"ACCEPTED"}
    @PatchMapping("/{postId}/party/requests/{requestId}")
    public ResponseEntity<PartyRequestDTO> decidePartyRequest(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("postId") Long postId,
            @PathVariable("requestId") Long requestId,
            @RequestBody PartyDecisionRequestDTO request
    ) {
        return ResponseEntity.ok(communityService.decidePartyRequest(memberId, postId, requestId, request));
    }
}
