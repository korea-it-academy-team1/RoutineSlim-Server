package org.routineslim.domain.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBudgetRequestDTO {

    // 월 예산 (원). null 이면 예산 해제
    private Integer monthlyBudget;
}
