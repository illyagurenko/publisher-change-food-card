package ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Trailer{
    private final String rawLine;
    private final int declaredCount;
}
