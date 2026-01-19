package hotel;

import annotations.Component;
import annotations.Singleton;
import enums.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Singleton
public class HotelView {
    public void nextDay(LocalDate currentDay) {
        System.out.println("Наступил новый день: " + getFormattedDate(currentDay));
    }

    public void displayAvailableRoomCount(int availableRoomsCount) {
        System.out.println("Количество доступных комнат: " + availableRoomsCount);
    }

    public void displayGuestsCount(int guestsCount) {
        System.out.println("Количество гостей: " + guestsCount);
    }

    public void displayRoomInformation(String information) {
        System.out.println(information);
    }

    public void displayGuestServicesSorted(List<GuestServiceUsage> guestServiceUsageList, Guest guest, ServiceSort sortBy, SortDirection direction) {
        String sortDescription = sortBy.getSortBy();
        String directionText = direction.getDirection();

        System.out.println("Услуги гостя " + guest.getFullName() + " (сортировка по " + sortDescription + ", по " + directionText + "):");

        if (guestServiceUsageList.isEmpty()) {
            System.out.println("  Нет услуг");
        } else {
            guestServiceUsageList.forEach(usage -> System.out.println("  " + usage));
        }
    }

    public void displaySortedAvailableRooms(Map<Integer, Room> sortedAvailableRooms, RoomSort sortBy, SortDirection direction) {
        String directionText = direction.getDirection();
        System.out.println("=============Доступные комнаты отсортированные по " + sortBy + " по " + directionText);
        displayRooms(sortedAvailableRooms);
    }

    public void displaySortedRooms(Map<Integer, Room> sortedRooms, RoomSort sortBy, SortDirection direction) {
        String directionText = direction.getDirection();
        System.out.println("===========Все комнаты отсортированные по " + sortBy + " по " + directionText);
        displayRooms(sortedRooms);
    }

    public void displayGuests(List<GuestData> sortedGuests, GuestSort sortBy, SortDirection direction) {
        String sortByText = sortBy.getSortBy();
        String directionText = direction.getDirection();

        System.out.println("=============Сортировка гостей по " + sortByText + " по " + directionText);

        sortedGuests.forEach(guestData-> {
           System.out.println(guestData.guestId() + " | " + guestData.fullName() + " Номер: " + guestData.roomNumber() + ". Дата выезда: " + guestData.checkoutDate());
        });

        System.out.println("=======================");
    }

    public void displayAvailableRoomsByDate(Map<Integer, Room> availableRoomsByDate, LocalDate date) {
        System.out.println("===========Список доступных комнат на " + getFormattedDate(date));
        for (Room room : availableRoomsByDate.values()) {
            System.out.println(room.getDescription());
        }
        System.out.println("================");
    }

    public void displayPreviousGuests(List<List<RoomGuestHistory>> previousGuests, int roomNumber) {
        System.out.println("=======Список последних трех постояльцев номера " + roomNumber + "=========");

        for (int i = 0; i < previousGuests.size(); i++) {
            StringBuilder text = new StringBuilder();
            text.append(i+1).append(".");
            for (RoomGuestHistory g : previousGuests.get(i)) {
                text.append(" ").append(g.getFullName());
            }
            System.out.println(text.toString());
        }
        System.out.println("===============");
    }

    public void displayPricesOfRoomsAndServices(List<IdPricePair> roomsAndServices)  {
        System.out.println("=======Цены комнат и услуг============");
        for (IdPricePair roomAndService : roomsAndServices) {
            System.out.println(roomAndService.getDescription());
        }
        System.out.println("===============");
    }

    public void displayNewPriceForRoom(int roomNumber, int price) {
        System.out.println("Цена комнаты " + roomNumber + " изменена на " + price);
    }

    public void displayRoomAvailable(boolean success, int roomNumber) {
        if (success) {
            System.out.println("Номер " + roomNumber + " доступен для заселения");
        } else {
            System.out.println("Невозможно изменить статус. Номер заселен");
        }
    }

    public void displayCleaningStarted(boolean success, int roomNumber) {
        if (success) {
            System.out.println("В номере " + roomNumber + " началась уборка");
        } else {
            System.out.println("Невозможно начать обслуживание, номер заселен.");
        }
    }

    public void displayMaintenanceStarted(boolean success, int roomNumber) {
        if (success) {
            System.out.println("В номере " + roomNumber + " начались ремонтные работы");
        } else {
            System.out.println("Невозможно начать ремонт, номер заселен.");
        }
    }

    public void displayCheckout(boolean success, int roomNumber, List<Guest> guests, int totalCost) {
        if (success) {
            for (Guest guest : guests) {
                System.out.println("Гость " + guest.getFullName() + " выехал из номера " + roomNumber);
            }
            System.out.println("Жители комнаты " + roomNumber + " заплатили за проживание " + totalCost + " руб.");
        } else {
            System.out.println("Номер " + roomNumber + " не заселен.");
        }
    }

    public void displayNewRoomAddition(int roomNumber) {
        System.out.println("Добавлен новый номер: " + roomNumber);
    }

    public void displayCheckIn(boolean success, List<Guest> guests, int roomNumber) {
        if (success) {
            for (Guest guest : guests) {
                System.out.println("Гость " + guest.getFullName() + " заселен в номер " + roomNumber);
            }
        } else {
            System.out.println("Не получилось заселить гостей в номер " + roomNumber);
        }
    }

    public void displayNewPriceForService(String serviceId, int price) {
        System.out.println("Цена услуги " + serviceId + " изменена на " + price);
    }

    public void displayNewServiceAddition(String name) {
        System.out.println("Была добавлена новая услуга: " + name);
    }

    public void displayAdditionServiceToGuest(String fullname, String serviceId) {
        System.out.println("Гостю " + fullname + " была добавлена услуга с id: " + serviceId);
    }

    public void displayExportSuccess(String entityType, String filePath) {
        System.out.println(entityType + " успешно экспортированы в файл: " + filePath);
    }

    public void displayImportSuccess(String entityType, String filePath, int count) {
        System.out.println(entityType + " успешно импортированы из файла: " + filePath);
        System.out.println("   Загружено записей: " + count);
    }

    public void displayError(String message) {
        System.out.println("Ошибка: " + message);
        System.out.println("Пожалуйста, попробуйте снова.");
    }

    private String getFormattedDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }

    private void displayRooms(Map<Integer, Room> roomsToDisplay) {
        roomsToDisplay.values().forEach(room -> {
            System.out.println(room.getDescription());
        });
    }

    public void displayMessage(String s) {
        System.out.println(s);
    }
}
