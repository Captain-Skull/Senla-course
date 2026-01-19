package contexts;

import enums.*;
import hotel.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomManagementContext extends BaseContext{

    public RoomManagementContext(Controller controller) {
        List<String> actions = Arrays.asList(
                "Общее число свободных номеров",
                "Список номеров",
                "Список свободных номеров",
                "Список номеров, которые будут свободны по дате",
                "Посмотреть 3-х последних постояльцев номера",
                "Посмотреть детали номера",
                "Цены услуг и номеров",
                "Добавить номер",
                "Изменить цену номера",
                "Изменить статус номера",
                "Заселить нового постояльца",
                "Выселить постояльцев из комнаты"
        );

        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        switch (operationIndex) {
            case "1":
                controller.displayAvailableRoomsCount();
                break;
            case "2":
                roomsListSortBy(true);
                break;
            case "3":
                roomsListSortBy(false);
                break;
            case "4":
                roomsAvailableByDate();
                break;
            case "5":
                lastGuests();
                break;
            case "6":
                roomInformation();
                break;
            case "7":
                roomsAndServicesSortBy();
                break;
            case "8":
                addNewRoomNumber();
                break;
            case "9":
                changeRoomPrice();
                break;
            case "10":
                changeRoomStatus();
                break;
            case "11":
                checkinGuests();
                break;
            case "12":
                checkoutGuest();
                break;
            default:
                noSuchIndex();
                String input = scanner.nextLine();
                handleInput(input);
                break;
        }
    }

    private void roomsListSortBy(boolean displayAllRooms) {
        System.out.println("Сортировать по:\n1.Цене\n2.Вместимости\n3.Типу номера");
        String sortChoice = scanner.nextLine();

        switch (sortChoice) {
            case "1":
                roomsListDirection(displayAllRooms, RoomSort.PRICE);
                break;
            case "2":
                roomsListDirection(displayAllRooms, RoomSort.CAPACITY);
                break;
            case "3":
                roomsListDirection(displayAllRooms, RoomSort.TYPE);
                break;
            default:
                noSuchIndex();
                roomsListSortBy(displayAllRooms);
                break;
        }
    }

    private void roomsListDirection(boolean displayAllRooms, RoomSort sortBy) {
        if (displayAllRooms) {
            handleSortingFlow(
                    direction -> controller.displaySortedRooms(sortBy, direction)
            );
        } else {
            handleSortingFlow(
                    direction -> controller.displaySortedAvailableRooms(sortBy, direction)
            );
        }

    }

    private void roomsAvailableByDate() {
        int days = readPositiveInt("Через сколько дней вы хотите увидеть свободные номера?");
        controller.displayAvailableRoomsByDate(days);
    }

    private void lastGuests() {
        int roomNumber = readPositiveInt("Введите номер комнаты");
        controller.displayPreviousGuests(roomNumber);
    }

    private void roomInformation() {
        int roomNumber = readPositiveInt("Введите номер комнаты");
        controller.displayRoomInformation(roomNumber);
    }

    private void roomsAndServicesSortBy() {
        System.out.println("Сортировать по:\n1.Разделу\n2.Цене");
        String sortChoice = scanner.nextLine();

        switch (sortChoice) {
            case "1":
                roomsAndServicesDirection(IdPriceSort.TYPE);
                break;
            case "2":
                roomsAndServicesDirection(IdPriceSort.PRICE);
                break;
            default:
                noSuchIndex();
                roomsAndServicesSortBy();
                break;
        }
    }

    private void roomsAndServicesDirection(IdPriceSort sortBy) {
        handleSortingFlow(
                direction -> controller.displayPricesOfRoomsAndServices(sortBy, direction)
        );
    }

    private void addNewRoomNumber() {
        int roomNumber = readPositiveInt("Введите новый номер");
        if (controller.isRoomExists(roomNumber)) {
            System.out.println("Такой номер уже существует. Попробуйте еще раз");
            addNewRoomNumber();
        } else {
            addNewRoomType(roomNumber);
        }
    }

    private void addNewRoomType(int roomNumber) {
        System.out.println("Тип номера:\n1. Эконом\n2. Стандарт\n3. Люкс\n4. Призедентский");
        String typeChoice = scanner.nextLine();
        RoomType type;

        switch (typeChoice) {
            case "1":
                type = RoomType.ECONOM;
                break;
            case "2":
                type = RoomType.STANDARD;
                break;
            case "3":
                type = RoomType.LUXURY;
                break;
            case "4":
                type = RoomType.PRESIDENTIAL;
                break;
            default:
                noSuchIndex();
                addNewRoomType(roomNumber);
                return;
        }

        addNewRoomPrice(roomNumber, type);
    }

    private void addNewRoomPrice(int roomNumber, RoomType roomType) {
        int price = readPositiveInt("Введите цену для нового номера");
        addNewRoomCapacity(roomNumber, roomType, price);
    }

    private void addNewRoomCapacity(int roomNumber, RoomType roomType, int price) {
        int capacity = readPositiveInt("Введите вместимость нового номера");
        controller.addNewRoom(roomNumber, roomType, price, capacity);
    }

    private void changeRoomPrice() {
        int roomNumber = readPositiveInt("Введите номер комнаты");
        newRoomPrice(roomNumber);
    }

    private void newRoomPrice(int roomNumber) {
        int roomPrice = readPositiveInt("Введите новую цену для комнаты " + roomNumber);
        controller.setRoomPrice(roomNumber, roomPrice);
    }

    private void changeRoomStatus() {
        int roomNumber = readPositiveInt("Введите номер комнаты");
        newRoomStatus(roomNumber);
    }

    private void newRoomStatus(int roomNumber) {
        System.out.println("Выберите новый статус для номера:\n1.Свободен\n2.Уборка\n3.Ремонтные работы");
        String statusIndex = scanner.nextLine();

        switch (statusIndex) {
            case "1":
                controller.setRoomAvailable(roomNumber);
                break;
            case "2":
                controller.setRoomCleaning(roomNumber);
                break;
            case "3":
                roomMaintenanceDays(roomNumber);
                break;
            default:
                noSuchIndex();
                newRoomStatus(roomNumber);
                break;
        }
    }

    private void roomMaintenanceDays(int roomNumber) {
        int days = readPositiveInt("Сколько дней потребуется для ремонтных работ? ");
        controller.setRoomUnderMaintenance(roomNumber, days);
    }

    private void checkinGuests() {
        int roomNumber = readPositiveInt("Введите номер комнаты, в которую хотите заселить новых гостей");
        checkInGuestsCount(roomNumber);
    }

    private void checkInGuestsCount(int roomNumber) {
        int newGuestsCount = readPositiveInt("Введите сколько человек вы хотите заселить");
        if (controller.isEnoughCapacity(roomNumber, newGuestsCount)) {
            checkInGuestsNames(roomNumber, newGuestsCount);
        } else {
            System.out.println("В комнате недостаточно места. Введите меньшее количество гостей для заселения");
            checkInGuestsCount(roomNumber);
        }
    }

    private void checkInGuestsNames(int roomNumber, int newGuestsCount) {
        System.out.println("Введите данные " + newGuestsCount + " гостей:");

        List<GuestDraft> newGuests = new ArrayList<>();

        for (int i = 1; i <= newGuestsCount; i++) {
            System.out.println("--- Гость " + i + " ---");

            System.out.println("Имя: ");
            String firstname = scanner.nextLine();
            System.out.println("Фамилия: ");
            String lastname = scanner.nextLine();

            newGuests.add(new GuestDraft(firstname, lastname));
        }

        checkInGuestsDays(roomNumber, newGuests);
    }

    void checkInGuestsDays(int roomNumber, List<GuestDraft> newGuests) {
        int days = readPositiveInt("На сколько дней снять номер? ");
        controller.checkInGuests(roomNumber, newGuests, days);
    }


    private void checkoutGuest() {
        int roomNumber = readPositiveInt("Введите номер комнаты, из которой хотите выселить постояльцев");
        controller.checkoutGuest(roomNumber);
    }

    private void handleSortingFlow(SortAction action) {
        String sortChoice = handleDirectionInput();

        switch (sortChoice) {
            case "1": action.execute(SortDirection.ASC); break;
            case "2": action.execute(SortDirection.DESC); break;
            default:
                noSuchIndex();
                handleSortingFlow(action);
                break;
        }
    }


    private String handleDirectionInput() {
        System.out.println("Сортировка по:\n1. Возрастанию\n2. Убыванию");
        return scanner.nextLine();
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int number = Integer.parseInt(input);
                if (number >= 0) {
                    return number;
                } else {
                    System.out.println("Ошибка: число должно быть неотрицательным!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число!");
            }
        }
    }
}