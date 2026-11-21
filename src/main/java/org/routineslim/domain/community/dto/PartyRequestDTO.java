package org.routineslim.domain.community.dto;

import lombok.*;
import org.routineslim.domain.community.entity.PartyMemberStatus;

import java.time.LocalDateTime;

// 파티 참여 요청 1건 (작성자가 보는 요청 목록)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyRequestDTO {

    // 파티 참여 번호 (수락·거절 주소에 쓴다)
    private Long requestId;

    // 게시글 번호
    private Long postId;

    // 요청한 회원 이름 (탈퇴한 회원이면 '탈퇴한 회원')
    private String memberName;

    // 상태 (REQUESTED, ACCEPTED, REJECTED)
    private PartyMemberStatus status;

    // 요청일시
    private LocalDateTime createdAt;
}
