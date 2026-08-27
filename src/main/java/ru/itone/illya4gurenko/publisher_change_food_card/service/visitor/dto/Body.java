package ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class Body {
    private final String rawLine;
    private final String fullName;
    private final String account;
    private final Type operationType;
    private final BigDecimal amount;

}
