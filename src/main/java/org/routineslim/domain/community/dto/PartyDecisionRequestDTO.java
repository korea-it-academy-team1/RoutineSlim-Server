package org.routineslim.domain.community.dto;

import lombok.*;

// 참여 요청 수락·거절
// 예) {"status": "ACCEPTED"}  또는  {"status": "REJECTED"}
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyDecisionRequestDTO {

    // ACCEPTED, REJECTED
    private String status;
}
