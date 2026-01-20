package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.Singleton;
import enums.RoomStatus;
import enums.RoomType;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
@Singleton
public class RoomCSVConverter implements CSVService.CSVConverter<Room> {

    @Inject
    private ServiceRegistry serviceRegistry;

    public RoomCSVConverter() {
    }

    @Override
    public String getHeaders() {
        return "number,type,price,capacity,status,endDate,daysUnderStatus,currentGuests,previousGuests";
    }

    @Override
    public String toCSV(Room room) {
        String currentGuestsString = room.getGuests().stream()
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
        String servicesString = guest.getServiceUsages().stream()
                .map(usage -> usage.getService().getId() + ":" + usage.getUsageDate())
                .collect(Collectors.joining(";;"));

        return String.format("%s;%s;%s;%s",
                guest.getId(),
                guest.getFirstName(),
                guest.getLastName(),
                servicesString.isEmpty() ? "" : servicesString);
    }

    @Override
    public Room fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
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

        if (!currentGuestsPart.isEmpty()) {
            String[] guestCSVLines = currentGuestsPart.split("\\|");
            for (String guestCSV : guestCSVLines) {
                if (!guestCSV.isEmpty()) {
                    Guest guest = guestFromCSV(guestCSV);
                    room.getGuests().add(guest);
                    guest.setRoomNumber(number);
                }
            }
        }

        return room;
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

                    Service service = serviceRegistry.getServiceById(serviceId);
                    if (service != null) {
                        guest.addService(service, usageDate);
                    }
                }
            }
        }

        return guest;
    }
}