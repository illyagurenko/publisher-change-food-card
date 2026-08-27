package ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto;

/**
 * Перечисление типов операций клиента.
 */
public enum Type {
    /** Дебетовая операция*/
    DR,

    /** Кредитовая операция*/
    CR,

    /**Зачисление*/
    ZR
}
