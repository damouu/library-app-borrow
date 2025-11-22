package com.example.demo.model;

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
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    private Integer id;

    @Column(nullable = false, columnDefinition = "UUID", name = "book_uuid")
    @Getter
    @Setter
    private UUID bookUUID;

    @Column(nullable = false, name = "is_borrowed")
    @Getter
    @Setter
    private boolean currentlyBorrowed;

    @Column(nullable = false, name = "added_date", columnDefinition = "timestamp")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    @JsonIgnore
    private LocalDate addedDate;

    @Column(name = "deleted_at", columnDefinition = "timestamp")
    @Getter
    @Setter
    private LocalDate deleted_at;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chapter_id")
    @JsonIgnore
    private Chapter chapter;

    public Book(Integer id, UUID book_uuid, String title, boolean is_borrowed, LocalDate added_date, LocalDate deleted_at) {
        this.id = id;
        this.bookUUID = book_uuid;
        this.currentlyBorrowed = is_borrowed;
        this.addedDate = added_date;
        this.deleted_at = deleted_at;
    }

    public Book(UUID uuid, String tittle, String genre, int i, String publisher, String author, LocalDate now) {
    }

}
