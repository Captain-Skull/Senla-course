package enums;

public enum RoomStatus {

    AVAILABLE("Свободен"),
    OCCUPIED("Занят"),
    MAINTENANCE("Ремонтные работы"),
    CLEANING("Уборка");

    private final String status;

    RoomStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
