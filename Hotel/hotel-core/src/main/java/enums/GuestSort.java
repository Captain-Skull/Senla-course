package enums;

public enum GuestSort {

    NAME("имени"),
    CHECKOUT_DATE("дате освобождения номера");

    private final String sortBy;

    GuestSort(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortBy() {
        return sortBy;
    }
}
