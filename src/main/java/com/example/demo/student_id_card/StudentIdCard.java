package com.example.demo.student_id_card;

import com.example.demo.book.BookStudent;
import com.example.demo.course.Course;
import com.example.demo.student.Student;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(name = "Student_id_card")
@Table(name = "Student_id_card", uniqueConstraints = @UniqueConstraint(columnNames = {"uuid", "uuid"}, name = "student_card_uuid_unique"))
@NoArgsConstructor
public class StudentIdCard implements Serializable {
    @Id
    @Column(unique = true, updatable = false, nullable = false)
    @SequenceGenerator(name = "student_id_card_sequence", allocationSize = 1, sequenceName = "student_id_card_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_id_card_sequence")
    @Getter(onMethod = @__(@JsonIgnore)) // generate the getter with the specific annotation.
    @Setter
    private Integer id;

    @Column(name = "uuid", columnDefinition = "UUID", nullable = false)
    @NotNull
    @Getter
    @Setter
    private UUID uuid;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    @Getter(onMethod = @__(@JsonIgnore)) // generate the getter with the specific annotation.
    @Setter
    private Student student;


    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.ALL})
    @JoinTable(name = "course_student", joinColumns = {@JoinColumn(name = "student_id_card")}, inverseJoinColumns = {@JoinColumn(name = "course")})
    @Getter(onMethod = @__(@JsonIgnore))
    @Column(name = "uuid", columnDefinition = "UUID", nullable = false)
    @Setter
    protected Set<Course> courses = new HashSet<>();


    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter
    protected Set<BookStudent> books = new HashSet<>();

    @JsonCreator
    public StudentIdCard(@JsonProperty("uuid") UUID uuid) {
        this.uuid = uuid;
    }
    
}
