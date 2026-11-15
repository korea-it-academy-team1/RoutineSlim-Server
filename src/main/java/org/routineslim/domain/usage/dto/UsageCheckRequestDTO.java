package org.routineslim.domain.usage.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageCheckRequestDTO {

    // 최근 사용 정도 (OFTEN, SOMETIMES, RARELY, NEVER)
    private String usageLevel;
}
