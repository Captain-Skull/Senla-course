package hotel;

import enums.RoomStatus;
import enums.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "rooms")
public class Room implements Serializable {

    private static final long serialVersionUID = 0003L;

    @Id
    @Column(name = "number", nullable = false)
    private int number;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType type;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false)
    private RoomStatus status;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "days_under_status", nullable = false)
    private int daysUnderStatus;

    public Room() {  }

    public Room(int number, RoomType type, int price, int capacity) {
        this.number = number;
        this.type = type;
        this.price = price;
        this.capacity = capacity;
        this.status = RoomStatus.AVAILABLE;
        this.endDate = LocalDate.now();
        this.daysUnderStatus = 0;
    }

    public int getNumber() {
        return number;
    }

    public int getPrice() {
        return price;
    }

    public int getCapacity() {
        return capacity;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public RoomType getType() {
        return type;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getDaysUnderStatus() {
        return daysUnderStatus;
    }

    public int calculateCost() {
        return daysUnderStatus * price;
    }

    public boolean setCleaning(LocalDate today) {
        if (status == RoomStatus.OCCUPIED) {
            return false;
        }

        setStatusDates(today, 1);

        this.status = RoomStatus.CLEANING;
        return true;
    }

    public boolean setUnderMaintenance(LocalDate today, int days) {
        if (status == RoomStatus.OCCUPIED) {
            return false;
        }

        setStatusDates(today, days);

        this.status = RoomStatus.MAINTENANCE;
        return true;
    }

    public boolean setAvailable() {
        if (status == RoomStatus.OCCUPIED) {
            return false;
        }
        this.status = RoomStatus.AVAILABLE;
        setDaysUnderStatus(0);
        return true;
    }

    public void setPrice(int newPrice) {
        if (status == RoomStatus.OCCUPIED) {
            System.out.println("Невозможно изменить цену пока номер заселен");

            return;
        }

        this.price = newPrice;
        System.out.println("Цена номера " + number + " изменена на " + newPrice + " за сутки");
    }

    public void setDaysUnderStatus(int days) {
        this.daysUnderStatus = days;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean canCheckIn(int guestsCount) {
        return status == RoomStatus.AVAILABLE && guestsCount <= capacity;
    }

    public boolean canCheckOut() {
        return status == RoomStatus.OCCUPIED;
    }

    public void markAsOccupied(LocalDate checkInDate, int days) {
        this.status = RoomStatus.OCCUPIED;
        this.daysUnderStatus = days;
        this.endDate = checkInDate.plusDays(days);
    }

    public void markAsAvailable() {
        this.status = RoomStatus.AVAILABLE;
        this.daysUnderStatus = 0;
        this.endDate = null;
    }

    private void setStatusDates(LocalDate startDate, int days) {
        this.daysUnderStatus = days;
        this.endDate = startDate.plusDays(days);
    }
}
