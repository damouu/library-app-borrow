package com.example.demo.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BorrowSummaryRepository extends JpaRepository<BorrowSummaryView, UUID> {

    @Query(value = """
            SELECT bmc.borrow_uuid       AS borrow_uuid,
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
            GROUP BY bmc.borrow_uuid
            ORDER BY borrow_return_date DESC
            LIMIT 5;
            """, nativeQuery = true)
    List<BorrowSummaryView> findBorrowSummaries(@Param("memberCardUuid") UUID memberCardUuid);
}
