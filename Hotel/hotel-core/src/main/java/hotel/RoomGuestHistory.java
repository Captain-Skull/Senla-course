package hotel;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "room_guest_history")
public class RoomGuestHistory implements Serializable {

    private static final long serialVersionUID = 0006L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "guest_id", nullable = false)
    private String guestId;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "room_number", nullable = false)
    private int roomNumber;

    @Column(name = "group_id", nullable = false)
    private int groupId;

    public RoomGuestHistory() {  }

    public RoomGuestHistory(String guestId, String firstname, String lastname,
                            int roomNumber, int groupId) {
        this.guestId = guestId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roomNumber = roomNumber;
        this.groupId = groupId;
    }

    public static RoomGuestHistory fromGuest(Guest guest, int roomNumber, int groupId) {
        return new RoomGuestHistory(
                guest.getId(),
                guest.getFirstName(),
                guest.getLastName(),
                roomNumber,
                groupId
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public int getRoomNumber() {
        return roomNumber;
    }
    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
}