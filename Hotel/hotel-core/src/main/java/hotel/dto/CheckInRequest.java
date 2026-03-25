package hotel.dto;

import java.util.List;

public class CheckInRequest {

    private int days;
    private List<GuestRequest> guests;

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public List<GuestRequest> getGuests() {
        return guests;
    }

    public void setGuests(List<GuestRequest> guests) {
        this.guests = guests;
    }
}