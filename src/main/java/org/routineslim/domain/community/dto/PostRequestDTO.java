package org.routineslim.domain.community.dto;

import lombok.*;

// 게시글 작성·수정 요청
// 예) 파티원 모집
//   {"board":"PARTY","title":"넷플릭스 파티원 구해요","content":"...","serviceName":"넷플릭스",
//    "currentMembers":1,"maxMembers":4,"totalPrice":17000,"pricePerMember":4250,"settlementDay":15}
// 예) 후기·자유 : {"board":"FREE","title":"...","content":"..."}
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDTO {

    // 게시판 (PARTY, REVIEW, FREE)
    private String board;

    // 제목 (1~200자)
    private String title;

    // 내용 (1~5000자)
    private String content;

    // ---- 파티원 모집(PARTY) 일 때만. 다른 게시판이면 무시한다 ----

    // 구독 서비스 이름 (필수)
    private String serviceName;

    // 모집 상태 (RECRUITING, CLOSED). 작성 시에는 무시하고 RECRUITING, 수정 시 비우면 그대로
    private String recruitStatus;

    // 현재 인원 (작성자 포함, 1 이상 모집 인원 이하)
    private Integer currentMembers;

    // 모집 인원 (작성자 포함, 2~10명)
    private Integer maxMembers;

    // 서비스 전체 월 요금 (선택)
    private Integer totalPrice;

    // 1인당 월 분담금 (필수)
    private Integer pricePerMember;

    // 매월 정산일 (선택, 1~31)
    private Integer settlementDay;
}
