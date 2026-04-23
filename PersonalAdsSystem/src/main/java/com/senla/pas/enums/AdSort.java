package com.senla.pas.enums;

import lombok.Getter;

@Getter
public enum AdSort {
    DATE("createdAt", false),
    PRICE("price", false),
    TITLE("title", false),
    RATING("averageRating", true);

    private final String fieldName;
    private final boolean isUserField;

    AdSort(String fieldName, boolean isUserField) {
        this.fieldName = fieldName;
        this.isUserField = isUserField;
    }

}
