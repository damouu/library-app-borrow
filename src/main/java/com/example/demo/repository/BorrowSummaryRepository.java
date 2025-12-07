package com.example.demo.repository;

import com.example.demo.view.BorrowSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BorrowSummaryRepository extends JpaRepository<BorrowSummaryView, UUID> {

    @Query(value = "select borrow_uuid, borrow_start_date, borrow_end_date, MAX(borrow_return_date) as borrow_return_date, json_agg(json_build_object('chapterUUID', chapter_uuid)) as borrow_details from borrow  where member_card_uuid = :memberCardUuid group by borrow_uuid, borrow_end_date, borrow_start_date ", countQuery = "SELECT COUNT(DISTINCT borrow_uuid) FROM borrow WHERE member_card_uuid = :memberCardUuid", nativeQuery = true)
    Page<BorrowSummaryView> findBorrowSummaries(@Param("memberCardUuid") UUID memberCardUuid, Pageable pageable);
}