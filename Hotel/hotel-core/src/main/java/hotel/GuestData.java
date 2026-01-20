package hotel;

import java.time.LocalDate;

public record GuestData(String guestId, String fullName, int roomNumber, LocalDate checkoutDate) {  }
