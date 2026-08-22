package ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unit_error")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnitError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "error_seq")
    private Integer errorSeq;

    @Column(name = "error_code", length = 3)
    private String errorCode;

    @Column(name = "error_field", length = 2000)
    private String errorField;

    @Column(name = "error_msg", length = 1000)
    private String errorMsg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;
}