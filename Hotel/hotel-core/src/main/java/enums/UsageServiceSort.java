package enums;

public enum UsageServiceSort {

    PRICE("цене"),
    DATE("дате");

    private final String sorBy;

    UsageServiceSort(String sortBy) {
        this.sorBy = sortBy;
    }

    public String getSortBy() {
        return sorBy;
    }
}
