package hotel.dto;

public class GuestDto {

    private String id;
    private String firstname;
    private String lastname;
    private Integer roomNumber;

    public GuestDto() {  }

    public GuestDto(String id, String firstname, String lastname, Integer roomNumber) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roomNumber = roomNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }
}
