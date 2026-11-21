package org.routineslim.domain.community.repository;

import org.routineslim.domain.community.entity.PartyMember;
import org.routineslim.domain.community.entity.PartyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

    // 글에 대한 내 참여
    Optional<PartyMember> findByPostIdAndMemberId(Long postId, UUID memberId);

    // 여러 글에 대한 내 참여 (목록에서 글마다 따로 조회하지 않도록)
    List<PartyMember> findByMemberIdAndPostIdIn(UUID memberId, Collection<Long> postIds);

    // 글의 참여 요청 전체 (요청 순)
    List<PartyMember> findByPostIdOrderByCreatedAtAscPartyMemberIdAsc(Long postId);

    // 글에 참여 기록이 있는지 (게시판 변경 막기용)
    boolean existsByPostId(Long postId);

    // 여러 글의 상태별 수 [게시글 번호, 수]
    @Query("select m.postId, count(m) from PartyMember m where m.postId in :postIds and m.status = :status group by m.postId")
    List<Object[]> countByPostIdsAndStatus(@Param("postIds") Collection<Long> postIds,
                                           @Param("status") PartyMemberStatus status);

    // 글의 수락된 파티원 수
    long countByPostIdAndStatus(Long postId, PartyMemberStatus status);

    // 글의 참여 기록 전부 삭제 (게시글 삭제 전)
    @Modifying
    @Query("delete from PartyMember m where m.postId = :postId")
    int deleteByPostId(@Param("postId") Long postId);
}
