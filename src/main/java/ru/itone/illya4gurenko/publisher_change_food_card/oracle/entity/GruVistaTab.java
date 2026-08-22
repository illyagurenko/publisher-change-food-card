package ru.itone.illya4gurenko.publisher_change_food_card.oracle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @Column(name = "ID", length = 12)
    private Long id;

    @Column(name = "SYSTEMACCOUNT", length = 32)
    private String systemAccount;

    @Column(name = "CURRENCY", length = 3)
    private String currency;

    @Column(name = "XALFA", precision = 23, scale = 3)
    private BigDecimal xalfa;

    @Column(name = "OPERATION", length = 3)
    private String operation;

    @Column(name = "TIME_STAMP")
    private LocalDateTime timeStamp;

    @Column(name = "POM_ID", length = 12)
    private Integer pomId;

    @Column(name = "UTERRARIO", length = 18)
    private Integer uterrario;

    @Column(name = "OLDTBAL", precision = 23, scale = 3)
    private BigDecimal oldTBal;

    @Column(name = "NEWTBAL", precision = 23, scale = 3)
    private BigDecimal newTBall;

    @Column(name = "ADD_INFO", length = 100)
    private String addInfo;

    @Column(name = "FILE_ID", length = 12)
    private Integer fileId;

    @Column(name = "FOC_STATUS", length = 64)
    private String focStatus;

    @Column(name = "FOC_TS")
    private LocalDateTime focTS;

    @Column(name = "FOC_Type", length = 10)
    private String focType;
}