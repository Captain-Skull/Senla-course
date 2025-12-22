package hotel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GuestServiceUsage implements Serializable {
    private static final long serialVersionUID = 00011L;

    private Service service;
    private LocalDate usageDate;
    private Guest guest;

    public GuestServiceUsage(Service service, LocalDate usageDate, Guest guest) {
        this.service = service;
        this.usageDate = usageDate;
        this.guest = guest;
    }

    public Service getService() {
        return service;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return usageDate.format(formatter);
    }

    public Guest getGuest() {
        return guest;
    }

    public int getPrice() {
        return service.getPrice();
    }

    public String getName() {
        return service.getName();
    }

    @Override
    public String toString() {
        return service.getName() + " - " + getFormattedDate() + " - " + service.getPrice() + " руб.";
    }
}