package org.routineslim.domain.community.repository;

import org.routineslim.domain.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 댓글 (최신순)
    List<Comment> findByPostIdOrderByCreatedAtDescCommentIdDesc(Long postId);

    // 게시글의 댓글 수
    long countByPostId(Long postId);

    // 여러 게시글의 댓글 수 [게시글 번호, 댓글 수] (목록에서 게시글마다 따로 세지 않도록)
    @Query("select c.postId, count(c) from Comment c where c.postId in :postIds group by c.postId")
    List<Object[]> countByPostIds(@Param("postIds") Collection<Long> postIds);

    // 게시글의 댓글 전부 삭제 (게시글 삭제 전)
    @Modifying
    @Query("delete from Comment c where c.postId = :postId")
    int deleteByPostId(@Param("postId") Long postId);
}
