public class Room {
    private int number;
    private RoomType type;
    private int price;
    private RoomStatus status;
    private Guest guest;

    public Room(int number, RoomType type, int price) {
        this.number = number;
        this.type = type;
        this.price = price;
        this.status = RoomStatus.AVAILABLE;
        this.guest = null;
    }

    public int getNumber() {
        return number;
    }
    public String getDescription() {
        String description = "Номер " + number + " тип: " + type + "\nСтоимость: " + price + " статус: " + status.getStatus();
        if (guest != null) {
            description += "\nГость: " + guest.getInformation();
        }
        return description;
    }

    public boolean checkIn(Guest newGuest) {
        if (status != RoomStatus.AVAILABLE) {
            System.out.println("Номер " + number + " не доступен для заселения. Текущий статус номера: " + status.getStatus());

            return false;
        }

        this.guest = newGuest;
        this.status = RoomStatus.OCCUPIED;
        newGuest.setRoomNumber(number);
        System.out.println("Гость " + newGuest.getFullName() + " заселен в номер " + number);
        return true;
    }

    public boolean checkOut() {
        if (status != RoomStatus.OCCUPIED || guest == null) {
            System.out.println("Номер " + number + " не заселен.");
            return false;
        }

        System.out.println("Гость " + guest.getFullName() + " выехал из номера " + number);

        this.guest = null;
        this.status = RoomStatus.CLEANING;
        return true;
    }

    public void setCleaning() {
        if (status == RoomStatus.OCCUPIED) {
            System.out.println("Невозможно начать обслуживание, номер заселен.");

            return;
        }

        this.status = RoomStatus.CLEANING;
        System.out.println("В номере " + number + " началась уборка");
    }

    public void setUnderMaintenance() {
        if (status == RoomStatus.OCCUPIED) {
            System.out.println("Невозможно начать ремонт, номер заселен.");

            return;
        }

        this.status = RoomStatus.MAINTENANCE;
        System.out.println("В номере " + number + " начались ремонтные работы");
    }

    public void setAvailable() {
        this.status = RoomStatus.AVAILABLE;
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
}
