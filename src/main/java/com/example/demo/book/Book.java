package com.example.demo.book;

import com.example.demo.chapter.Chapter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(name = "book")
@Table(name = "book", uniqueConstraints = {@UniqueConstraint(name = "book_uuid", columnNames = "book_uuid")})
@NoArgsConstructor
public class Book {
    @Id
    @SequenceGenerator(name = "book_sequence", allocationSize = 1, sequenceName = "book_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_sequence")
    @Column(updatable = false, nullable = false)
    @Getter(onMethod = @__(@JsonIgnore)) // generate the getter with the specific annotation.
    @Setter
    private Integer id;

    @Column(nullable = false, columnDefinition = "UUID", name = "book_uuid")
    @Getter
    @Setter
    private UUID book_uuid;

    @Column(nullable = false, name = "is_borrowed")
    @Getter
    @Setter
    private boolean is_borrowed;

    @Column(nullable = false, name = "added_date", columnDefinition = "DATE")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Getter(onMethod = @__(@JsonIgnore)) // generate the getter with the specific annotation.
    @Setter
    private LocalDate added_date;

    @Column(name = "deleted_at", columnDefinition = "DATE")
    @Getter
    @Setter
    private LocalDate deleted_at;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", referencedColumnName = "id")
    private Chapter chapter_id;

    public Book(Integer id, UUID book_uuid, String title, boolean is_borrowed, LocalDate added_date, LocalDate deleted_at, Chapter chapter) {
        this.id = id;
        this.book_uuid = book_uuid;
        this.is_borrowed = is_borrowed;
        this.added_date = added_date;
        this.deleted_at = deleted_at;
        this.chapter_id = chapter;
    }
}
