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

    @Query(value = """
            SELECT bmc.borrow_uuid       AS borrow_uuid,
                   bmc.borrow_end_date         as borrow_end_date,
                   bmc.borrow_start_date       as borrow_start_date,
                   MAX(bmc.borrow_return_date) AS borrow_return_date,
                   json_agg(
                       jsonb_build_object(
                           'book_uuid', b.book_uuid,
                           'title', c.title,
                           'second_title', c.second_title,
                           'chapter_number', c.chapter_number,
                           'total_pages', c.total_pages,
                           'cover_artwork_url', c.cover_artwork_url
                       ) ORDER BY c.title
                   ) AS books
            FROM student.public.book_member_card bmc
                     JOIN student.public.book b ON b.id = bmc.book_id
                     JOIN student.public.member_card mc ON mc.id = bmc.member_card
                     JOIN student.public.chapter c ON c.id = b.chapter_id
            WHERE mc.member_card_uuid = :memberCardUuid
            GROUP BY bmc.borrow_uuid, bmc.borrow_end_date, bmc.borrow_start_date
            """, countQuery = """
            SELECT COUNT(DISTINCT bmc.borrow_uuid)
            FROM student.public.book_member_card bmc
            JOIN student.public.member_card mc ON mc.id = bmc.member_card
            WHERE mc.member_card_uuid = :memberCardUuid
            """, nativeQuery = true)
        //パジネーションのオップシオンがクエリーに追加されます。
    Page<BorrowSummaryView> findBorrowSummaries(@Param("memberCardUuid") UUID memberCardUuid, Pageable pageable);
}
