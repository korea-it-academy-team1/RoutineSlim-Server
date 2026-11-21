package org.routineslim.domain.community.dto;

import lombok.*;
import org.routineslim.domain.community.entity.PostBoard;
import org.routineslim.domain.community.entity.RecruitStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {

    // 게시글 번호
    private Long postId;

    // 게시판 (PARTY, REVIEW, FREE)
    private PostBoard board;

    // 제목
    private String title;

    // 내용
    private String content;

    // 작성자 이름 (탈퇴한 회원이면 '탈퇴한 회원')
    private String authorName;

    // 내가 쓴 글인지 (수정·삭제 메뉴 노출)
    private boolean mine;

    // 작성일
    private LocalDateTime createdAt;

    // 수정일 (수정한 적 없으면 null)
    private LocalDateTime updatedAt;

    // 조회수
    private Integer viewCount;

    // 댓글 수
    private Long commentCount;

    // ---- 파티원 모집(PARTY) 글만 값이 있다. 후기·자유 글은 null ----

    // 모집하는 구독 서비스 이름
    private String serviceName;

    // 모집 상태 (RECRUITING, CLOSED)
    private RecruitStatus recruitStatus;

    // 현재 인원 (작성자 포함)
    private Integer currentMembers;

    // 모집 인원 (작성자 포함)
    private Integer maxMembers;

    // 서비스 전체 월 요금
    private Integer totalPrice;

    // 1인당 월 분담금
    private Integer pricePerMember;

    // 매월 정산일
    private Integer settlementDay;
}
