package org.routineslim.domain.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    // 댓글 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    // 게시글 번호
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // 작성자 회원 식별자 (V6 이전에 쓴 댓글은 null)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "member_id",
            columnDefinition = "binary(16)")
    private UUID memberId;

    // 내용
    @Column(name = "content",
            nullable = false,
            columnDefinition = "TEXT")
    private String content;

    // 작성일 (저장 직후 응답에도 쓰도록 서비스에서 넣는다)
    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;

    // 수정일 (DB 가 ON UPDATE 로 넣는다)
    @Column(name = "updated_at",
            insertable = false,
            updatable = false)
    private LocalDateTime updatedAt;
}
