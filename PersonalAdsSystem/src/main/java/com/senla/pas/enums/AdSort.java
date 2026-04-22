package com.senla.pas.enums;

public enum AdSort {
    DATE("createdAt", false),
    PRICE("price", false),
    TITLE("title", false),
    RATING("rating", true);

    private final String fieldName;
    private final boolean isUserField;

    AdSort(String fieldName, boolean isUserField) {
        this.fieldName = fieldName;
        this.isUserField = isUserField;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isUserField() {
        return isUserField;
    }
}
