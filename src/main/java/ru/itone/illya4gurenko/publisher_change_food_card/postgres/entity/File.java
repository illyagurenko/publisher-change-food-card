package ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "file", schema = "pom")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ins_time", nullable = false)
    private OffsetDateTime insTime;

    @Column(name = "filename", length = 40)
    private String filename;

    @Column(name = "fullpath", length = 120)
    private String fullpath;

    @Column(name = "sender", length = 20)
    private String sender;

    @Column(name = "file_comment", length = 100)
    private String fileComment;

    @Column(name = "upd_time")
    private OffsetDateTime updTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_status", length = 10)
    private FileStatus fileStatus;

    @Column(name = "uli_date", length = 3)
    private String uliDate;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    private List<Unit> units;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    private List<UnitError> unitErrors;
}