package org.routineslim.domain.community.dto;

import lombok.*;

// 댓글 작성 요청
// 예) {"content":"저 참여할게요!"}
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDTO {

    // 내용 (1~1000자)
    private String content;
}
