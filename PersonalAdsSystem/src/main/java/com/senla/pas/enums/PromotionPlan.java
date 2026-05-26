package com.senla.pas.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PromotionPlan {
    DAY(1, 100, "1 день"),
    THREE_DAYS(3, 270, "3 дня"),
    WEEK(7, 500, "1 неделя"),
    MONTH(30, 1500, "1 месяц");

    private final int days;
    private final int price;
    private final String description;
}
