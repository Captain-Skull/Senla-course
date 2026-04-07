package hotel.dto;

public class FailedImportItem {

    private int roomNumber;
    private String reason;

    public FailedImportItem() {
    }

    public FailedImportItem(int roomNumber, String reason) {
        this.roomNumber = roomNumber;
        this.reason = reason;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

