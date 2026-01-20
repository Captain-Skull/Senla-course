package hotel;

import enums.RoomStatus;
import enums.RoomType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {

    private static final long serialVersionUID = 0003L;

    private int number;
    private RoomType type;
    private int price;
    private int capacity;
    private RoomStatus status;
    private List<Guest> guests;
    private LocalDate endDate;
    private int daysUnderStatus;

    public Room(int number, RoomType type, int price, int capacity) {
        this.number = number;
        this.type = type;
        this.price = price;
        this.capacity = capacity;
        this.status = RoomStatus.AVAILABLE;
        this.guests = new ArrayList<>();
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

    public String getDescription() {
        StringBuilder description = new StringBuilder("Номер " + number + " тип: " + type + "\nСтоимость: " + price + " вместимость: " + capacity + "\nСтатус: " + status.getStatus());

        if (!guests.isEmpty()) {
            System.out.println("Список гостей");
            for (Guest guest : guests) {
                description.append("\nГость: ").append(guest.getInformation());
            }
        }
        return description.toString();
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getDaysUnderStatus() {
        return daysUnderStatus;
    }

    public boolean checkIn(List<Guest> newGuests, LocalDate checkInDate, int days) {
        if (status != RoomStatus.AVAILABLE) {
            return false;
        }

        if (newGuests.size() > capacity) {
            return false;
        }

        for (Guest guest : newGuests) {
            this.guests.add(guest);
            guest.setRoomNumber(number);
        }

        setStatusDates(checkInDate, days);

        this.status = RoomStatus.OCCUPIED;

        return true;
    }

    public boolean checkOut() {
        if (status != RoomStatus.OCCUPIED || guests.isEmpty()) {
            return false;
        }

        for (Guest guest : guests) {
            guest.setRoomNumber(0);
        }

        this.guests.clear();
        this.status = RoomStatus.AVAILABLE;
        setCleaning(endDate);
        return true;
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

    private void setStatusDates(LocalDate startDate, int days) {
        this.daysUnderStatus = days;
        this.endDate = startDate.plusDays(days);
    }
}
