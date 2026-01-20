package enums;

public enum SortDirection {

    ASC("возрастанию"),
    DESC("убыванию");

    private final String direction;

    SortDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }
}
