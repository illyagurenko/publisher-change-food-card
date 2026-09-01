package ru.itone.illya4gurenko.publisher_change_food_card.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.itone.illya4gurenko.publisher_change_food_card.config.ConstantsUtils;
import ru.itone.illya4gurenko.publisher_change_food_card.oracle.entity.GruVistaTab;
import ru.itone.illya4gurenko.publisher_change_food_card.oracle.repository.GruVistaTabRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.ProcType;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Repository
@RequiredArgsConstructor
public class GruDao {
    private final GruVistaTabRepository gruVistaTabRepository;

    // GRU.VISTA.TAB
    // сохранение строк в бд и генерация 18-значного числа, перевод enum
    public GruVistaTab save(String account, BigDecimal amount, Type operation, Long pomId, Long fileId, ProcType focType, LocalDateTime focTs){
        GruVistaTab entity = new GruVistaTab();
        entity.setSystemAccount(account);
        entity.setCurrency(ConstantsUtils.GRU_CURRENCY);
        entity.setXalfa(amount);
        entity.setOperation(operation);
        entity.setTimeStamp(LocalDateTime.now());
        entity.setPomId(pomId);
        entity.setUterrario(generateRandom18Digits());
        entity.setOldTBal(null);
        entity.setNewTBal(null);
        entity.setAddInfo(operationTranslate(operation));
        entity.setFileId(fileId);
        entity.setFocStatus(ConstantsUtils.GRU_FOC_STATUS_WAIT);
        entity.setFocType(focType);
        entity.setFocTS(focTs);
        return gruVistaTabRepository.save(entity);
    }

    private long generateRandom18Digits() {
        return ThreadLocalRandom.current().nextLong(100_000_000_000_000_000L, 1_000_000_000_000_000_000L);
    }

    private String operationTranslate(Type type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case ZR -> ConstantsUtils.ADD_INFO_ZR;
            case CR -> ConstantsUtils.ADD_INFO_CR;
            case DR -> ConstantsUtils.ADD_INFO_DR;
        };
    }
}
