package hotel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import java.io.Serializable;

@Entity
@Table(name = "guests")
public class Guest implements Serializable {

    private static final long serialVersionUID = 2L;

    @Id
    @Column(name = "id", insertable = false)
    private String id;
    @Column(name = "firstname", nullable = false)
    private String firstname;
    @Column(name = "lastname", nullable = false)
    private String lastname;
    @Column(name = "room_number")
    private Integer roomNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Guest() {
    }

    public Guest(String id, String firstname, String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roomNumber = null;
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

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public String getInformation() {
        return "ID: " + id + ", Имя: " + firstname + " " + lastname;
    }

    public User getUser() {
        return user;
    }

    public void setRoomNumber(int number) {
        this.roomNumber = number;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
