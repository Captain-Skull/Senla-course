package contexts;

import enums.IdPriceSort;
import enums.ServiceSort;
import enums.SortDirection;
import hotel.Controller;

import java.util.Arrays;
import java.util.List;

public class ServiceManagementContext extends BaseContext {

    private static final List<String> actions = Arrays.asList(
            "Цены услуг и номеров",
            "Список услуг постояльца",
            "Изменить цену услуги",
            "Добавить новую услугу"
    );

    public ServiceManagementContext(Controller controller) {
        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        switch (operationIndex) {
            case "1":
                roomsAndServicesSortBy();
                break;
            case "2":
                servicesListGuestId();
                break;
            case "3":
                changeServicePrice();
                break;
            case "4":
                addNewService();
            default:
                noSuchIndex();
                String input = scanner.nextLine();
                handleInput(input);
                break;
        }
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

    private void servicesListGuestId() {
        System.out.println("Введите id гостя, список услуг которого хотите посмотреть");
        String guestId = scanner.nextLine();

        if (controller.isGuestIdValid(guestId)) {
            servicesListSortBy(guestId);
        } else {
            System.out.println("Гостя с таким id не существует. Попробуйте еще раз");
            servicesListGuestId();
        }
    }

    private void servicesListSortBy(String guestId) {
        System.out.println("Сортировать по:\n1. Цене\n2. Дате");
        String sortChoice = scanner.nextLine();

        switch (sortChoice) {
            case "1":
                servicesListDirection(guestId, ServiceSort.PRICE);
                break;
            case "2":
                servicesListDirection(guestId, ServiceSort.DATE);
                break;
            default:
                noSuchIndex();
                servicesListSortBy(guestId);
                break;
        }
    }

    private void servicesListDirection(String guestId, ServiceSort sortBy) {
        handleSortingFlow(
                direction -> controller.displayGuestServices(guestId, sortBy, direction)
        );
    }

    private void changeServicePrice() {
        System.out.println("Введите id услуги");
        String serviceId = scanner.nextLine();
        newServicePrice(serviceId);
    }

    private void newServicePrice(String serviceId) {
        int servicePrice = readPositiveInt("Введите новую цену для услуги");
        controller.setServicePrice(serviceId, servicePrice);
    }

    private void addNewService() {
        System.out.println("Введите название для новой услуги");
        String name = scanner.nextLine();
        addNewServicePrice(name);
    }

    private void addNewServicePrice(String name) {
        int price = readPositiveInt("Введите цену для новой услуги");
        addNewServiceDescription(name, price);
    }

    private void addNewServiceDescription(String name, int price) {
        System.out.println("Введите описание новой услуги");
        String description = scanner.nextLine();
        controller.addNewService(name, price, description);
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
