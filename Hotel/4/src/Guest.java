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

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public int getRoomNumber() {
        return roomNumber;
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

    public void setRoomNumber(int number) {
        this.roomNumber = number;
    }

    public void addService(Service service, LocalDate usageDate) {
        GuestServiceUsage newServiceUsage = new GuestServiceUsage(service, usageDate, this);
        serviceUsages.add(newServiceUsage);
    }

    public void displayServicesSorted(String sortBy, SortDirection direction) {
        List<GuestServiceUsage> sortedList;
        String sortDescription;

        switch (sortBy.toLowerCase()) {
            case "price":
                sortedList = sortServicesByPrice(direction);
                sortDescription = "цене";
                break;
            case "date":
                sortedList = sortServicesByDate(direction);
                sortDescription = "дате";
                break;
            default:
                System.out.println("Неизвестный параметр сортировки: " + sortBy);
                return;
        }

        String directionText = (direction == SortDirection.ASC) ? "возрастанию" : "убыванию";
        System.out.println("Услуги гостя " + getFullName() + " (сортировка по " + sortDescription + ", по " + directionText + "):");

        if (sortedList.isEmpty()) {
            System.out.println("  Нет услуг");
        } else {
            sortedList.forEach(usage -> System.out.println("  " + usage));
        }
    }

    private List<GuestServiceUsage> sortServicesByPrice(SortDirection direction) {
        Comparator<GuestServiceUsage> comparator = Comparator.comparingInt(GuestServiceUsage::getPrice);

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return serviceUsages.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<GuestServiceUsage> sortServicesByDate(SortDirection direction) {
        Comparator<GuestServiceUsage> comparator = Comparator.comparing(GuestServiceUsage::getUsageDate);

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return serviceUsages.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
