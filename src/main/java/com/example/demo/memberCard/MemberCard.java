package com.example.demo.memberCard;

import com.example.demo.book.BookMemberCard;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(name = "member_card")
@Table(name = "member_card", uniqueConstraints = @UniqueConstraint(columnNames = {"member_card_uuid", "member_card_uuid"}, name = "member_card_uuid_unique"))
@NoArgsConstructor
public class MemberCard implements Serializable {
    @Id
    @Column(unique = true, updatable = false, nullable = false)
    @SequenceGenerator(name = "memberCard_sequence", allocationSize = 1, sequenceName = "memberCard_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memberCard_sequence")
    @Getter(onMethod = @__(@JsonIgnore)) // generate the getter with the specific annotation.
    @Setter
    private Integer id;

    @Column(name = "uuid", columnDefinition = "UUID", nullable = false)
    @NotNull
    @Getter
    @Setter
    private UUID uuid;

    @Column(name = "created_at")
    @Getter
    @Setter
    private LocalDateTime created_at;

    @Column(name = "valid_until")
    @Getter
    @Setter
    private LocalDateTime valid_until;

    @Column(name = "deleted_at", columnDefinition = "DATE")
    @Getter
    @Setter
    private LocalDateTime deleted_at;


    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    protected Set<BookMemberCard> books = new HashSet<>();

    @JsonCreator
    public MemberCard(@JsonProperty("uuid") UUID uuid) {
        this.uuid = uuid;
    }
}
