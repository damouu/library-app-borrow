package com.example.demo.repository;

import com.example.demo.model.MemberCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberCardRepository extends JpaRepository<MemberCard, Integer> {

    @Query("SELECT b FROM member_card b WHERE b.deleted_at is null and b.member_card_uuid = :uuid")
    Optional<MemberCard> findMemberCardByUuid(UUID uuid);
}
