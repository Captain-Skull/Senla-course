package hotel.dto;

import hotel.Guest;
import hotel.Room;

import java.util.List;

public class RoomWithGuestsDto {

    private final Room room;
    private final List<Guest> guests;

    public RoomWithGuestsDto(Room room, List<Guest> guests) {
        this.room = room;
        this.guests = guests != null ? guests : List.of();
    }

    public Room getRoom() {
        return room;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public boolean hasGuests() {
        return !guests.isEmpty();
    }

    public int getNumber() {
        return room.getNumber();
    }
}