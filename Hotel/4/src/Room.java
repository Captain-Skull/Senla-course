import enums.RoomStatus;
import enums.RoomType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private int number;
    private RoomType type;
    private int price;
    private int capacity;
    private RoomStatus status;
    private List<Guest> guests;
    private List<List<Guest>> previousGuests;
    private LocalDate startDate;
    private LocalDate endDate;
    private int daysUnderStatus;

    public Room(int number, RoomType type, int price, int capacity) {
        this.number = number;
        this.type = type;
        this.price = price;
        this.capacity = capacity;
        this.status = RoomStatus.AVAILABLE;
        this.guests = new ArrayList<>();
        this.previousGuests = new ArrayList<>();
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now();
        this.daysUnderStatus = 0;
    }

    public int getNumber() {
        return number;
    }

    public int getPrice() {
        return price;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public RoomType getType() {
        return type;
    }

    public String getDescription() {
        StringBuilder description = new StringBuilder("Номер " + number + " тип: " + type + "\nСтоимость: " + price + " статус: " + status.getStatus());

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

    public List<List<Guest>> getPreviousGuests() {
        return previousGuests;
    }

    public void checkIn(List<Guest> newGuests, LocalDate checkInDate, int days) {
        if (status != RoomStatus.AVAILABLE) {
            System.out.println("Номер " + number + " не доступен для заселения. Текущий статус номера: " + status.getStatus());

            return;
        }

        if (newGuests.size() > capacity) {
            System.out.println("В номере " + number + " Заселено максимальное количество людей");
            return;
        }

        for (Guest guest : newGuests) {
            this.guests.add(guest);
            guest.setRoomNumber(number);
            System.out.println("Гость " + guest.getFullName() + " заселен в номер " + number);
        }

        this.startDate = checkInDate;
        this.daysUnderStatus = days;
        this.endDate = calculateCheckoutDate(checkInDate, days);

        this.status = RoomStatus.OCCUPIED;

    }

    public boolean checkOut() {
        if (status != RoomStatus.OCCUPIED || guests.isEmpty()) {
            System.out.println("Номер " + number + " не заселен.");
            return false;
        }

        for (Guest guest : guests) {;
            guest.setRoomNumber(0);
            System.out.println("Гость " + guest.getFullName() + " выехал из номера " + number);
        }

        System.out.println("Жители комнаты " + number + " заплатили за проживание " + calculateCost() + "руб");

        addPreviousGuests(new ArrayList<>(guests));
        this.guests.clear();
        this.status = RoomStatus.AVAILABLE;
        setCleaning(endDate);
        return true;
    }

    public int calculateCost() {
        return daysUnderStatus * price;
    }

    public void setCleaning(LocalDate today) {
        if (status == RoomStatus.OCCUPIED) {
            System.out.println("Невозможно начать обслуживание, номер заселен.");

            return;
        }

        this.startDate = today;
        this.endDate = today.plusDays(1);
        this.daysUnderStatus = 1;

        this.status = RoomStatus.CLEANING;
        System.out.println("В номере " + number + " началась уборка");
    }

    public void setUnderMaintenance(LocalDate today, int days) {
        if (status == RoomStatus.OCCUPIED) {
            System.out.println("Невозможно начать ремонт, номер заселен.");

            return;
        }

        this.startDate = today;
        this.endDate = today.plusDays(days);
        this.daysUnderStatus = days;

        this.status = RoomStatus.MAINTENANCE;
        System.out.println("В номере " + number + " начались ремонтные работы");
    }

    public void setAvailable() {
        this.status = RoomStatus.AVAILABLE;
        this.daysUnderStatus = 0;
        System.out.println("Номер " + number + " доступен для заселения");
    }

    public void setPrice(int newPrice) {
        if (status == RoomStatus.OCCUPIED) {
            System.out.println("Невозможно изменить цену пока номер заселен");

            return;
        }

        this.price = newPrice;
        System.out.println("Цена номера " + number + " изменена на " + newPrice + " за сутки");
    }

    private LocalDate calculateCheckoutDate(LocalDate checkInDate, int days) {
        return checkInDate.plusDays(days);
    }

    private void addPreviousGuests(List<Guest> guests) {
        if (previousGuests.size() == 3) {
            previousGuests.removeLast();
        }

        previousGuests.addFirst(guests);
    }
}
