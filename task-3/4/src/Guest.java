import java.util.ArrayList;
import java.util.List;

public class Guest {
    private String id;
    private String firstname;
    private String lastname;
    private int roomNumber;
    private List<Service> services;

    public Guest(String id, String firstname, String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roomNumber = -1;
        this.services = new ArrayList<>();
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

    public String getInformation() {
        StringBuilder information = new StringBuilder(getFullName());
        if (!services.isEmpty()) {
            information.append(" Услуги: ");
            services.forEach(service -> {
                information.append("\n").append(service.getName());
            });
        }

        return information.toString();
    }

    public void setRoomNumber(int number) {
        this.roomNumber = number;
    }

    public void addService(Service service) {
        services.add(service);
    }
}
