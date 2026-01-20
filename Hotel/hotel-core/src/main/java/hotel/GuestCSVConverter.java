package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.Singleton;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
@Singleton
public class GuestCSVConverter implements CSVService.CSVConverter<Guest> {

    @Inject
    private ServiceRegistry serviceRegistry;

    public GuestCSVConverter() {
    }

    @Override
    public String getHeaders() {
        return "id,firstName,lastName,roomNumber,services";
    }

    @Override
    public String toCSV(Guest guest) {
        String servicesString = guest.getServiceUsages().stream()
                .map(usage -> usage.getService().getId() + ":" + usage.getUsageDate())
                .collect(Collectors.joining(";"));

        return String.format("%s,%s,%s,%d,%s",
                guest.getId(),
                guest.getFirstName(),
                guest.getLastName(),
                guest.getRoomNumber(),
                servicesString.isEmpty() ? "" : servicesString);
    }

    @Override
    public Guest fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        String id = parts[0];
        String firstName = parts[1];
        String lastName = parts[2];
        int roomNumber = Integer.parseInt(parts[3]);
        String servicesPart = parts.length > 4 ? parts[4] : "";

        Guest guest = new Guest(id, firstName, lastName);
        guest.setRoomNumber(roomNumber);

        if (!servicesPart.isEmpty()) {
            String[] serviceEntries = servicesPart.split(";");
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