import enums.ServiceSort;
import enums.SortDirection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Guest {
    private String id;
    private String firstname;
    private String lastname;
    private int roomNumber;
    private List<GuestServiceUsage> serviceUsages;

    public Guest(String id, String firstname, String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roomNumber = -1;
        this.serviceUsages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstname;
    }

    public String getLastName() {
        return lastname;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public List<GuestServiceUsage> getServiceUsages() {
        return serviceUsages;
    }

    public String getDescription() {
        return getFullName() + " Номер: " + roomNumber;
    }

    public String getInformation() {
        StringBuilder information = new StringBuilder(getFullName());
        information.append(" Номер: ");
        information.append(roomNumber);
        if (!serviceUsages.isEmpty()) {
            information.append("\nУслуги: ");
            serviceUsages.forEach(serviceUsage -> {
                information.append("\n").append(serviceUsage.getName());
            });
        }

        return information.toString();
    }

    public List<GuestServiceUsage> getServicesSorted(ServiceSort sortBy, SortDirection direction) {
        return sortServices(sortBy, direction);
    }

    public void setRoomNumber(int number) {
        this.roomNumber = number;
    }

    public void addService(Service service, LocalDate usageDate) {
        GuestServiceUsage newServiceUsage = new GuestServiceUsage(service, usageDate, this);
        serviceUsages.add(newServiceUsage);
    }

    private List<GuestServiceUsage> sortServices(ServiceSort sortBy, SortDirection direction) {
        Comparator<GuestServiceUsage> comparator = switch (sortBy) {
            case ServiceSort.PRICE -> Comparator.comparingInt(GuestServiceUsage::getPrice);
            case ServiceSort.DATE -> Comparator.comparing(GuestServiceUsage::getUsageDate);
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return serviceUsages.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
