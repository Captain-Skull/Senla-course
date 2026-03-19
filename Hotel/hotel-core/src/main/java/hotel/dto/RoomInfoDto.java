package hotel.dto;

import java.util.List;

public class RoomInfoDto {

    private int number;
    private String type;
    private int price;
    private int capacity;
    private String status;

    private List<String> guests;
    private String guestsAccessMessage;

    public RoomInfoDto() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getGuests() {
        return guests;
    }

    public void setGuests(List<String> guests) {
        this.guests = guests;
    }

    public String getGuestsAccessMessage() {
        return guestsAccessMessage;
    }

    public void setGuestsAccessMessage(String guestsAccessMessage) {
        this.guestsAccessMessage = guestsAccessMessage;
    }
}