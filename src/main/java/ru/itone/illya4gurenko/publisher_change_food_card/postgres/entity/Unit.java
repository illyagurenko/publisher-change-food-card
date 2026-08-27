package ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "unit", schema = "pom")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Column(name = "ins_time", nullable = false)
    private LocalDateTime insTime;

    @Column(name = "pom_type", length = 3)
    private String pomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private FileStatus status;

    @Column(name = "unit_value", length = 2000)
    private String unitValue;

    @Column(name = "upd_time")
    private LocalDateTime updTime;

    @Column(name = "add_value", length = 100)
    private String addValue;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL)
    List<UnitError> unitErrors;
}