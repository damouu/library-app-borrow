package com.example.demo.repository;

import com.example.demo.model.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Integer>, JpaSpecificationExecutor<Borrow> {

    @Query(value = "SELECT EXISTS (select 1 FROM borrow b WHERE b.member_card_uuid = :memberCard and b.borrow_return_date is null ) AS has_borrows", nativeQuery = true)
    boolean getBookMemberCardByBook(@Param("memberCard") UUID memberCard);

    List<Borrow> getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(UUID borrowUuid, UUID memberCardUuid);

    @Query(value = "SELECT (CURRENT_DATE = latest_borrow_date) AS is_current_date_latest FROM (SELECT borrow_start_date AS latest_borrow_date FROM borrow WHERE member_card_uuid = :memberCardUuid AND borrow_return_date is not null ORDER BY borrow_start_date desc LIMIT 1) AS latest_borrow", nativeQuery = true)
    boolean checkLatestBorrowDate(UUID memberCardUuid);

    boolean existsByMemberCardUuid(@Param("memberCardUuid") UUID memberCardUuid);

    @Modifying
    @Query("UPDATE Borrow b SET b.borrowReturnDate = :returnDate WHERE b IN :borrows")
    void setReturnDateForBorrows(List<Borrow> borrows, LocalDate returnDate);

}
