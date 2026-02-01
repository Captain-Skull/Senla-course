package hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "guests")
public class Guest implements Serializable {

    private static final long serialVersionUID = 0002L;

    @Id
    @Column(name = "id", insertable = false)
    private String id;
    @Column(name = "firstname", nullable = false)
    private String firstname;
    @Column(name = "lastname", nullable = false)
    private String lastname;
    @Column(name = "room_number", nullable = false)
    private int roomNumber;

    public Guest() {  }

    public Guest(String id, String firstname, String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roomNumber = -1;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstname;
    }

    public String getLastName() {
        return lastname;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getInformation() {
        return "ID: " + id + ", Имя: " + firstname + " " + lastname;
    }

    public void setRoomNumber(int number) {
        this.roomNumber = number;
    }

    public void setId(String id) {
        this.id = id;
    }
}
