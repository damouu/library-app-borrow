package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    private Integer id;

    @Column(name = "member_card_uuid", columnDefinition = "UUID", nullable = false)
    @Getter
    @Setter
    private UUID memberCardUUID;

    @Column(name = "created_at", columnDefinition = "timestamp", nullable = false)
    @Getter
    @Setter
    private LocalDateTime createdAT;

    @Column(name = "valid_until", columnDefinition = "date", nullable = false)
    @Getter
    @Setter
    private LocalDateTime validUntil;

    @Column(name = "deleted_at", columnDefinition = "timestamp")
    @Getter
    @Setter
    private LocalDateTime deleted_at;


    @JsonCreator
    public MemberCard(@JsonProperty("uuid") UUID uuid) {
        this.memberCardUUID = uuid;
    }
}
