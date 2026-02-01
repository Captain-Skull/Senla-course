package hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "guest_service_usage")
public class GuestServiceUsage implements Serializable {

    private static final long serialVersionUID = 00011L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    public GuestServiceUsage() {  }

    public GuestServiceUsage(Service service, LocalDate usageDate, Guest guest) {
        this.service = service;
        this.usageDate = usageDate;
        this.guest = guest;
    }

    public int getId() {
        return id;
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

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return service.getName() + " - " + getFormattedDate() + " - " + service.getPrice() + " руб.";
    }
}