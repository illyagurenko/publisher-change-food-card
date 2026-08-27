package ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class Header {
    private final String rawLine;
    private final ProcType procType;
    private final LocalDateTime focTimestamp; // если INTIME
}
