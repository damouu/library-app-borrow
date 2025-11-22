package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Getter
@Setter
@NoArgsConstructor
@Entity
public class BookMemberCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "memberCard")
    private MemberCard memberCard;

    @Column(columnDefinition = "UUID", name = "borrow_uuid", nullable = false)
    @Getter
    @Setter
    private UUID borrowUUID;

    @Column(name = "borrow_start_date", nullable = false, columnDefinition = "Date")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    private LocalDate borrowStartDate;

    @Column(name = "borrow_end_date", nullable = false, columnDefinition = "Date")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    private LocalDate borrowEndDate;

    @Column(name = "borrow_return_date", columnDefinition = "Date")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    private LocalDate borrowReturnDate;


    @JsonCreator
    public BookMemberCard(LocalDate borrow_start_date, LocalDate borrow_end_date, LocalDate borrow_return_date) {
        this.borrowStartDate = borrow_start_date;
        this.borrowEndDate = borrow_end_date;
        this.borrowReturnDate = borrow_return_date;
    }

}
