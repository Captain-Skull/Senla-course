import enums.SortDirection;

import java.util.List;

public class HotelTest {
    public static void main(String[] args) {
        System.out.println("Электронный журнал отеля");

        Hotel hotel = new Hotel("Красный Кит");

        System.out.println("Вас приветствует отель " + hotel.getName());

        Guest Ivan = hotel.initializeGuest("Иван", "Грозный");
        Guest Maria = hotel.initializeGuest("Мария", "Коновалова");
        Guest Alisa = hotel.initializeGuest("Алиса", "Встранечудесная");
        Guest Alexander = hotel.initializeGuest("Александр", "Никитин");

        hotel.checkIn(List.of(new Guest[]{Ivan}), 101, 10);
        hotel.checkIn(List.of(new Guest[]{Maria, Alisa}), 202, 10);
        hotel.addServiceToGuest(Maria, "S5");
        hotel.checkIn(List.of(new Guest[]{Alexander}), 204, 5);
        hotel.addServiceToGuest(Alexander, "S4");
        hotel.addServiceToGuest(Alexander, "S6");

        hotel.nextDay();
        hotel.nextDay();
        hotel.nextDay();

        hotel.setRoomUnderMaintenance(103, 5);
        hotel.setRoomUnderMaintenance(105, 6);
        hotel.setRoomCleaning(104);

        hotel.setRoomPrice(501, 16000);
        hotel.setServicePrice("S1", 175);

        hotel.nextDay();

        Guest Michael = hotel.initializeGuest("Михаил", "Жорданов");
        hotel.checkIn(List.of(new Guest[]{Michael}), 204, 8);

        hotel.nextDay();
        hotel.nextDay();

        hotel.checkIn(List.of(new Guest[]{Michael}), 204, 8);

        Guest Sonya = hotel.initializeGuest("Софья", "Мудрая");
        hotel.checkIn(List.of(new Guest[]{Sonya}), 501, 14);
        hotel.addServiceToGuest(Sonya, "S1");
        hotel.addServiceToGuest(Sonya, "S4");
        hotel.addServiceToGuest(Sonya, "S5");
        hotel.addServiceToGuest(Sonya, "S6");


        System.out.println("============Сортировки и другие данные==============");

        System.out.println("Количество доступных комнат: " + hotel.getAvailableRoomsCount());
        System.out.println("Количество гостей: " + hotel.getGuestsCount());

        hotel.displayGuestServicesSorted(Sonya, "price", SortDirection.DESC);

        hotel.displaySortedRooms("type", SortDirection.ASC);

        hotel.displaySortedAvailableRooms("type", SortDirection.DESC);

        hotel.displayGuests("date", SortDirection.ASC);

        hotel.displayRoomInformation(202);

        hotel.displayAvailableRoomsByDate(hotel.getCurrentDay().plusDays(6));

        hotel.displayPreviousGuests(204);

        hotel.displayPricesOfRoomsAndServices("price", SortDirection.DESC);
    }
}
