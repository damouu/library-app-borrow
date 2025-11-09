package com.example.demo.repository;

import com.example.demo.model.Book;
import com.example.demo.model.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    @Query("SELECT b FROM book b WHERE b.deleted_at is null and b.book_uuid = :uuid")
    Optional<Book> findByUuid(UUID uuid);


    Page<Book> findAll(Specification<Book> specification, Pageable pageable);

    @Query("SELECT b FROM book b WHERE b.chapter = :chapter and b.deleted_at is null and b.is_borrowed is false order by b.added_date desc ")
    List<Book> findRecentBooksByChapter(@Param("chapter") Chapter chapter, Pageable pageable);

}
