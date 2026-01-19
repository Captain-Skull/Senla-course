package hotel;

import java.io.Serializable;

public class RoomGuestHistory implements Serializable {
    private static final long serialVersionUID = 0006L;

    private String id;
    private String firstname;
    private String lastname;
    private int roomNumber;
    private int guestGroupId;

    public RoomGuestHistory() {}

    public RoomGuestHistory(String id, String firstname, String lastname,
                            int roomNumber, int guestGroupId) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roomNumber = roomNumber;
        this.guestGroupId = guestGroupId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getFullName() { return firstname + " " + lastname; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public int getGuestGroupId() { return guestGroupId; }
    public void setGuestGroupId(int guestGroupId) { this.guestGroupId = guestGroupId; }
}