package org.routineslim.domain.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    // 게시글 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    // 작성자 회원 식별자
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "member_id",
            columnDefinition = "binary(16)",
            nullable = false)
    private UUID memberId;

    // 게시판 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "board",
            nullable = false,
            length = 10)
    private PostBoard board;

    // 제목
    @Column(name = "title",
            nullable = false,
            length = 200)
    private String title;

    // 내용
    @Column(name = "content",
            nullable = false,
            columnDefinition = "TEXT")
    private String content;

    // ---- 파티원 모집(PARTY) 글만 값이 있다. 후기·자유 글은 null ----

    // 모집하는 구독 서비스 이름
    @Column(name = "service_name", length = 100)
    private String serviceName;

    // 모집 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "recruit_status", length = 10)
    private RecruitStatus recruitStatus;

    // 현재 인원 (작성자 포함)
    @Column(name = "current_members", columnDefinition = "int unsigned")
    private Integer currentMembers;

    // 모집 인원 (작성자 포함)
    @Column(name = "max_members", columnDefinition = "int unsigned")
    private Integer maxMembers;

    // 서비스 전체 월 요금 (원)
    @Column(name = "total_price", columnDefinition = "int unsigned")
    private Integer totalPrice;

    // 1인당 월 분담금 (원)
    @Column(name = "price_per_member", columnDefinition = "int unsigned")
    private Integer pricePerMember;

    // 매월 정산일
    @Column(name = "settlement_day", columnDefinition = "int unsigned")
    private Integer settlementDay;

    // 조회수 (증가는 PostRepository.increaseViewCount 로만)
    @Builder.Default
    @Column(name = "view_count",
            nullable = false,
            columnDefinition = "int unsigned")
    private Integer viewCount = 0;

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
