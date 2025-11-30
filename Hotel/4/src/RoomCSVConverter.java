import enums.RoomStatus;
import enums.RoomType;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

public class RoomCSVConverter implements CSVService.CSVConverter<Room> {
    private HotelModel hotelModel;
    private GuestCSVConverter guestConverter;

    public RoomCSVConverter(HotelModel hotelModel) {
        this.hotelModel = hotelModel;
        this.guestConverter = new GuestCSVConverter(hotelModel);
    }

    @Override
    public String getHeaders() {
        return "id,number,type,price,capacity,status,endDate,daysUnderStatus,currentGuests,previousGuests";
    }

    @Override
    public String toCSV(Room room) {
        String currentGuestsString = room.getGuests().stream()
                .map(this::guestToCSV)
                .collect(Collectors.joining("|"));

        String previousGuestsString = room.getPreviousGuests().stream()
                .map(group -> group.stream()
                        .map(this::guestToCSV)
                        .collect(Collectors.joining("|")))
                .collect(Collectors.joining("||"));

        return String.format("%s,%d,%s,%d,%d,%s,%s,%d,%s,%s",
                room.getId(),
                room.getNumber(),
                room.getType(),
                room.getPrice(),
                room.getCapacity(),
                room.getStatus(),
                room.getEndDate(),
                room.getDaysUnderStatus(),
                currentGuestsString.isEmpty() ? "" : currentGuestsString,
                previousGuestsString.isEmpty() ? "" : previousGuestsString);
    }

    private String guestToCSV(Guest guest) {
        String servicesString = guest.getServiceUsages().stream()
                .map(usage -> usage.getService().getId() + ":" + usage.getUsageDate())
                .collect(Collectors.joining(";;"));

        return String.format("%s;%s;%s;%s",
                guest.getId(),
                guest.getFirstName(),
                guest.getLastName(),
                servicesString.isEmpty() ? "" : servicesString);
    }

    private Guest guestFromCSV(String guestCSV) {
        String[] parts = guestCSV.split(";");
        String id = parts[0];
        String firstName = parts[1];
        String lastName = parts[2];
        String servicesPart = parts.length > 3 ? parts[3] : "";

        Guest guest = new Guest(id, firstName, lastName);

        if (!servicesPart.isEmpty()) {
            String[] serviceEntries = servicesPart.split(";;");
            for (String serviceEntry : serviceEntries) {
                String[] serviceData = serviceEntry.split(":");
                if (serviceData.length == 2) {
                    String serviceId = serviceData[0];
                    LocalDate usageDate = LocalDate.parse(serviceData[1]);

                    Service service = hotelModel.getServiceById(serviceId);
                    if (service != null) {
                        guest.addService(service, usageDate);
                    }
                }
            }
        }

        return guest;
    }

    @Override
    public Room fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        String id = parts[0];
        int number = Integer.parseInt(parts[1]);
        RoomType type = RoomType.valueOf(parts[2]);
        int price = Integer.parseInt(parts[3]);
        int capacity = Integer.parseInt(parts[4]);
        RoomStatus status = RoomStatus.valueOf(parts[5]);
        LocalDate endDate = LocalDate.parse(parts[6]);
        int daysUnderStatus = Integer.parseInt(parts[7]);
        String currentGuestsPart = parts.length > 8 ? parts[8] : "";
        String previousGuestsPart = parts.length > 9 ? parts[9] : "";

        Room room = new Room(id, number, type, price, capacity);
        room.setStatus(status);
        room.setEndDate(endDate);
        room.setDaysUnderStatus(daysUnderStatus);

        if (!currentGuestsPart.isEmpty()) {
            String[] guestCSVLines = currentGuestsPart.split("\\|");
            for (String guestCSV : guestCSVLines) {
                if (!guestCSV.isEmpty()) {
                    Guest guest = guestFromCSV(guestCSV);
                    room.getGuests().add(guest);
                    guest.setRoomNumber(number);
                    hotelModel.getGuests().put(guest.getId(), guest);
                }
            }
        }

        if (!previousGuestsPart.isEmpty()) {
            String[] guestGroups = previousGuestsPart.split("\\|\\|");
            for (String group : guestGroups) {
                if (!group.isEmpty()) {
                    String[] guestCSVLines = group.split("\\|");
                    List<Guest> guestGroup = new ArrayList<>();
                    for (String guestCSV : guestCSVLines) {
                        if (!guestCSV.isEmpty()) {
                            Guest guest = guestFromCSV(guestCSV);
                            guestGroup.add(guest);
                            hotelModel.getPreviousGuests().put(guest.getId(), guest);
                        }
                    }
                    hotelModel.getPreviousGuests(number).add(guestGroup);
                }
            }
        }

        return room;
    }
}