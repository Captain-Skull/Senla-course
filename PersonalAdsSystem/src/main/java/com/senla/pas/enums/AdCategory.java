package com.senla.pas.enums;

public enum AdCategory {
    ELECTRONICS("Электроника"),
    TRANSPORT("Транспорт"),
    REAL_ESTATE("Недвижимость"),
    CLOTHING("Одежда и обувь"),
    HOME_AND_GARDEN("Дом и сад"),
    SERVICES("Услуги"),
    HOBBIES("Хобби и отдых"),
    PETS("Животные"),
    JOBS("Работа"),
    OTHER("Другое");

    private final String displayName;

    AdCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}