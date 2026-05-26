package com.senla.pas.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdSort {
    DATE("createdAt", false),
    PRICE("price", false),
    TITLE("title", false),
    RATING("averageRating", true);

    private final String fieldName;
    private final boolean isUserField;
}
