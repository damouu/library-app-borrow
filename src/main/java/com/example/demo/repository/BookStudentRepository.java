package com.example.demo.repository;

import com.example.demo.model.BookMemberCard;
import com.example.demo.model.MemberCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookStudentRepository extends JpaRepository<BookMemberCard, Integer>, JpaSpecificationExecutor<BookMemberCard> {

    @Query("SELECT b FROM BookMemberCard b WHERE b.book.id = :id")
    Optional<BookMemberCard> findBookStudentByID(int id);

    @Query("SELECT b FROM BookMemberCard b WHERE b.borrow_uuid = :uuid")
    Optional<List<BookMemberCard>> findBookStudentByBorrow_uuid(UUID uuid);


    @Query("SELECT b FROM BookMemberCard b WHERE b.book.id = :id  and b.borrow_return_date = null ")
    Optional<BookMemberCard> findBookStudentByIDUpdate(int id);


    @Query(value = "SELECT EXISTS (select 1 from book_member_card inner join book on book.id = book_member_card.book_id where book.book_uuid = :book_uuid AND book_member_card.borrow_return_date is null ) AS is_borrowed", nativeQuery = true)
    boolean existsByBookIsBorrowed(UUID book_uuid);

    @Query(value = "SELECT EXISTS (select 1 FROM book_member_card b WHERE b.member_card = :memberCard and b.borrow_return_date is null ) AS has_borrows", nativeQuery = true)
    boolean getBookMemberCardByBook(@Param("memberCard") MemberCard memberCard);

}
