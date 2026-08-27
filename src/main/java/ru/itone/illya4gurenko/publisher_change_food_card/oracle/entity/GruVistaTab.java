package ru.itone.illya4gurenko.publisher_change_food_card.oracle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.ProcType;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "GRU_VISTA_TAB", schema = "GRU")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GruVistaTab {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GRU_VISTA_GEN")
    @SequenceGenerator(
            name = "GRU_VISTA_GEN",
            sequenceName = "GRU_VISTA_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "SYSTEMACCOUNT", length = 32)
    private String systemAccount;

    @Column(name = "CURRENCY", length = 3)
    private String currency;

    @Column(name = "XALFA", precision = 23, scale = 3)
    private BigDecimal xalfa;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATION", length = 2)
    private Type operation;

    @Column(name = "TIME_STAMP")
    private LocalDateTime timeStamp;

    @Column(name = "POM_ID")
    private Long pomId;

    @Column(name = "UTERRARIO")
    private Long uterrario;

    @Column(name = "OLDTBAL", precision = 23, scale = 3)
    private BigDecimal oldTBal;

    @Column(name = "NEWTBAL", precision = 23, scale = 3)
    private BigDecimal newTBal;

    @Column(name = "ADD_INFO", length = 100)
    private String addInfo;

    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "FOC_STATUS", length = 64)
    private String focStatus;

    @Column(name = "FOC_TS")
    private LocalDateTime focTS;

    @Enumerated(EnumType.STRING)
    @Column(name = "FOC_TYPE", length = 10)
    private ProcType focType;
}