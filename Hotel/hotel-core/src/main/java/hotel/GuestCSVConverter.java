package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.Singleton;
import hotel.dto.GuestWithServicesDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Singleton
public class GuestCSVConverter implements CSVService.CSVConverter<GuestWithServicesDto> {

    @Inject
    private ServiceRegistry serviceRegistry;

    public GuestCSVConverter() {
    }

    @Override
    public String getHeaders() {
        return "id,firstName,lastName,roomNumber,services";
    }

    @Override
    public String toCSV(GuestWithServicesDto dto) {
        Guest guest = dto.getGuest();

        String servicesString = dto.getServiceUsages().stream()
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
    public GuestWithServicesDto fromCSV(String csvLine) {
        String[] parts = csvLine.split(",", -1);
        String id = parts[0];
        String firstName = parts[1];
        String lastName = parts[2];
        int roomNumber = Integer.parseInt(parts[3]);
        String servicesPart = parts.length > 4 ? parts[4] : "";

        Guest guest = new Guest(id, firstName, lastName);
        guest.setRoomNumber(roomNumber);

        List<GuestServiceUsage> usages = new ArrayList<>();
        if (!servicesPart.isEmpty()) {
            String[] serviceEntries = servicesPart.split(";");
            for (String serviceEntry : serviceEntries) {
                String[] serviceData = serviceEntry.split(":");
                if (serviceData.length == 2) {
                    String serviceId = serviceData[0];
                    LocalDate usageDate = LocalDate.parse(serviceData[1]);

                    Service service = serviceRegistry.getServiceById(serviceId);
                    if (service != null) {
                        GuestServiceUsage usage = new GuestServiceUsage(service, usageDate, guest);
                        usages.add(usage);
                    }
                }
            }
        }

        return new GuestWithServicesDto(guest, usages);
    }
}