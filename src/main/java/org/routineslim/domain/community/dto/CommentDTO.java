package org.routineslim.domain.community.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    // 댓글 번호
    private Long commentId;

    // 게시글 번호
    private Long postId;

    // 작성자 이름 (작성자를 모르는 예전 댓글은 '알 수 없음')
    private String authorName;

    // 내가 쓴 댓글인지 (삭제 메뉴 노출)
    private boolean mine;

    // 내용
    private String content;

    // 작성일
    private LocalDateTime createdAt;
}
