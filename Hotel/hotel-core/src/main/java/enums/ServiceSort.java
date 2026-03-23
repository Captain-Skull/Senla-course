package enums;

public enum ServiceSort {

    ID("идентификатору"),
    PRICE("цене");

    private final String sorBy;

    ServiceSort(String sortBy) {
        this.sorBy = sortBy;
    }

    public String getSortBy() {
        return sorBy;
    }
}
