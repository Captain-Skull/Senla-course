public class HotelTest {
    public static void main(String[] args) {
        System.out.println("Электронный журнал отеля");

        Hotel hotel = new Hotel("Красный Кит");

        System.out.println("Вас приветствует отель " + hotel.getName());

        hotel.displayAllRooms();
        hotel.displayAllServices();

        Guest Ivan = hotel.initializeGuest("Иван", "Грозный");
        Guest Maria = hotel.initializeGuest("Мария", "Коновалова");
        Guest Alexander = hotel.initializeGuest("Александр", "Никитин");

        hotel.checkIn(Ivan, 101);
        hotel.checkIn(Maria, 202);
        hotel.addServiceToGuest(Maria, "S5");
        hotel.checkIn(Alexander, 204);
        hotel.addServiceToGuest(Alexander, "S4");
        hotel.addServiceToGuest(Alexander, "S6");

        hotel.setRoomUnderMaintenance(103);
        hotel.setRoomUnderMaintenance(105);
        hotel.setRoomCleaning(104);

        hotel.setRoomPrice(501, 16000);
        hotel.setServicePrice("S1", 175);

        hotel.setRoomAvailable(105);

        hotel.checkOut(Alexander);
        Guest Michael = hotel.initializeGuest("Михаил", "Жорданов");
        hotel.checkIn(Michael, 204);
        hotel.setRoomAvailable(204);
        hotel.checkIn(Michael, 204);

        Guest Sonya = hotel.initializeGuest("Софья", "Мудрая");
        hotel.checkIn(Sonya, 501);
        hotel.addServiceToGuest(Sonya, "S1");
        hotel.addServiceToGuest(Sonya, "S4");
        hotel.addServiceToGuest(Sonya, "S5");
        hotel.addServiceToGuest(Sonya, "S6");

        hotel.displayAllRooms();
        hotel.displayAllServices();
    }
}
