package org.routineslim.domain.community.repository;

import org.routineslim.domain.community.entity.Post;
import org.routineslim.domain.community.entity.PostBoard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시판의 게시글 (최신순)
    List<Post> findByBoardOrderByCreatedAtDescPostIdDesc(PostBoard board, Pageable pageable);

    // 전체 게시글 (최신순)
    List<Post> findAllByOrderByCreatedAtDescPostIdDesc(Pageable pageable);

    // 조회수 +1 (동시에 여러 명이 봐도 빠지지 않도록 DB 에서 더한다)
    @Modifying
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.postId = :postId")
    int increaseViewCount(@Param("postId") Long postId);
}
