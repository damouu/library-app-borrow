package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(name = "series")
@Table(name = "series", uniqueConstraints = {@UniqueConstraint(name = "series_uuid", columnNames = "series_uuid")})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Series {
    @Id
    @SequenceGenerator(name = "series_sequence", allocationSize = 1, sequenceName = "series_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "series_sequence")
    @Column(updatable = false, nullable = false)
    @JsonIgnore
    private Integer id;

    @Column(nullable = false, columnDefinition = "UUID", name = "series_uuid")
    private UUID seriesUUID;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "genre", nullable = false)
    private String genre;

    @Column(name = "cover_artwork_URL", nullable = false)
    private String coverArtworkURL;

    @Column(name = "illustrator", nullable = false)
    private String illustrator;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "last_print_publication_date", columnDefinition = "date")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate lastPrintPublicationDate;

    @Column(name = "first_print_publication_date", nullable = false, columnDefinition = "date")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @NotNull
    private LocalDate firstPrintPublicationDate;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "deleted_at", columnDefinition = "timestamp")
    private LocalDate deletedAT;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @NotNull
    @JsonIgnore
    private LocalDate createdAt;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Chapter> chapters;

}
