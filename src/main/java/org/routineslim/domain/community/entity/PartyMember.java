package org.routineslim.domain.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "party_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        // 글당 회원 1건
                        name = "uk_party_member_post_member",
                        columnNames = {"post_id", "member_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyMember {

    // 파티 참여 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "party_member_id")
    private Long partyMemberId;

    // 파티원 모집 게시글 번호
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // 참여를 요청한 회원
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "member_id",
            columnDefinition = "binary(16)",
            nullable = false)
    private UUID memberId;

    // 참여 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status",
            nullable = false,
            length = 10)
    private PartyMemberStatus status;

    // 요청일시 (저장 직후 응답에도 쓰도록 서비스에서 넣는다)
    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;
}
