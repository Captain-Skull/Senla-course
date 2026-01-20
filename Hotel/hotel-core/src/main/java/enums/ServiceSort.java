package enums;

public enum ServiceSort {

    PRICE("цене"),
    DATE("дате");

    private final String sorBy;

    ServiceSort(String sortBy) {
        this.sorBy = sortBy;
    }

    public String getSortBy() {
        return sorBy;
    }
}
