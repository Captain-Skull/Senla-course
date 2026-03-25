package hotel;

import enums.RoomStatus;
import enums.RoomType;
import hotel.dto.RoomWithGuestsDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomCSVConverter implements CSVService.CSVConverter<RoomWithGuestsDto> {

    public RoomCSVConverter() {
    }

    @Override
    public String getHeaders() {
        return "number,type,price,capacity,status,endDate,daysUnderStatus,currentGuests,previousGuests";
    }

    @Override
    public String toCSV(RoomWithGuestsDto dto) {
        Room room = dto.getRoom();

        String currentGuestsString = dto.getGuests().stream()
                .map(this::guestToCSV)
                .collect(Collectors.joining("|"));

        return String.format("%d,%s,%d,%d,%s,%s,%d,%s",
                room.getNumber(),
                room.getType(),
                room.getPrice(),
                room.getCapacity(),
                room.getStatus(),
                room.getEndDate(),
                room.getDaysUnderStatus(),
                currentGuestsString.isEmpty() ? "" : currentGuestsString);
    }

    private String guestToCSV(Guest guest) {
        return String.format("%s;%s;%s",
                guest.getId(),
                guest.getFirstName(),
                guest.getLastName());
    }

    @Override
    public RoomWithGuestsDto fromCSV(String csvLine) {
        String[] parts = csvLine.split(",", -1);
        int number = Integer.parseInt(parts[0]);
        RoomType type = RoomType.valueOf(parts[1]);
        int price = Integer.parseInt(parts[2]);
        int capacity = Integer.parseInt(parts[3]);
        RoomStatus status = RoomStatus.valueOf(parts[4]);
        LocalDate endDate = LocalDate.parse(parts[5]);
        int daysUnderStatus = Integer.parseInt(parts[6]);
        String currentGuestsPart = parts.length > 7 ? parts[7] : "";

        Room room = new Room(number, type, price, capacity);
        room.setStatus(status);
        room.setEndDate(endDate);
        room.setDaysUnderStatus(daysUnderStatus);

        List<Guest> guests = new ArrayList<>();
        if (!currentGuestsPart.isEmpty()) {
            String[] guestCSVLines = currentGuestsPart.split("\\|");
            for (String guestCSV : guestCSVLines) {
                if (!guestCSV.isEmpty()) {
                    Guest guest = guestFromCSV(guestCSV, number);
                    guests.add(guest);
                }
            }
        }

        return new RoomWithGuestsDto(room, guests);
    }

    private Guest guestFromCSV(String guestCSV, int roomNumber) {
        String[] parts = guestCSV.split(";");
        String id = parts[0];
        String firstName = parts[1];
        String lastName = parts[2];

        Guest guest = new Guest(id, firstName, lastName);
        guest.setRoomNumber(roomNumber);

        return guest;
    }
}