package org.routineslim.domain.community.repository;

import org.routineslim.domain.community.entity.Post;
import org.routineslim.domain.community.entity.PostBoard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.routineslim.domain.community.entity.PartyMemberStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시판의 게시글 (최신순)
    List<Post> findByBoardOrderByCreatedAtDescPostIdDesc(PostBoard board, Pageable pageable);

    // 전체 게시글 (최신순)
    List<Post> findAllByOrderByCreatedAtDescPostIdDesc(Pageable pageable);

    // 내가 쓴 게시글 (최신순)
    List<Post> findByMemberIdOrderByCreatedAtDescPostIdDesc(UUID memberId, Pageable pageable);

    // 내가 댓글 단 게시글 (최신순, 글마다 1번)
    @Query("select p from Post p where p.postId in (select c.postId from Comment c where c.memberId = :memberId)"
            + " order by p.createdAt desc, p.postId desc")
    List<Post> findCommentedBy(@Param("memberId") UUID memberId, Pageable pageable);

    // 내가 참여(요청)한 파티 글 (최신순)
    @Query("select p from Post p where p.postId in (select m.postId from PartyMember m"
            + " where m.memberId = :memberId and m.status in :statuses) order by p.createdAt desc, p.postId desc")
    List<Post> findJoinedBy(@Param("memberId") UUID memberId,
                            @Param("statuses") Collection<PartyMemberStatus> statuses,
                            Pageable pageable);

    // 조회수 +1 (동시에 여러 명이 봐도 빠지지 않도록 DB 에서 더한다)
    @Modifying
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.postId = :postId")
    int increaseViewCount(@Param("postId") Long postId);
}
