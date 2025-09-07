package com.example.demo.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookStudentRepository extends JpaRepository<BookStudent, Integer>, JpaSpecificationExecutor<BookStudent> {

    @Query("SELECT b FROM BookStudent b WHERE b.book.id = :id")
    Optional<BookStudent> findBookStudentByID(int id);

}
