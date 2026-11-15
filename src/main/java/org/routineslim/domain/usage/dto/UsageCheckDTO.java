package org.routineslim.domain.usage.dto;

import lombok.*;
import org.routineslim.domain.usage.entity.UsageLevel;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageCheckDTO {

    // 구독 아이디
    private UUID subscriptionId;

    // 최근 사용 정도 (OFTEN, SOMETIMES, RARELY, NEVER)
    private UsageLevel usageLevel;

    // 체크한 날
    private LocalDate checkedOn;
}
