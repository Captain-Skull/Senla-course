package contexts;

import enums.GuestSort;
import enums.ServiceSort;
import enums.SortDirection;
import hotel.Controller;

import java.util.Arrays;
import java.util.List;

public class GuestManagementContext extends BaseContext {

    private static final List<String> actions = Arrays.asList(
            "Общее число постояльцев",
            "Список постояльцев и их номеров",
            "Список услуг постояльца",
            "Добавить услугу постояльцу"
    );

    public GuestManagementContext(Controller controller) {
        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        switch (operationIndex) {
            case "1":
                controller.displayGuestsCount();
                break;
            case "2":
                guestsListSortBy();
                break;
            case "3":
                servicesListGuestId();
                break;
            case "4":
                addServiceToGuest();
            default:
                noSuchIndex();
                String input = scanner.nextLine();
                handleInput(input);
        }
    }

    private void guestsListSortBy() {
        System.out.println("Сортировать по:\n1. Алфавиту\n2. Дате выезда");
        String sortChoice = scanner.nextLine();
        switch (sortChoice) {
            case "1":
                guestsListDirection(GuestSort.NAME);
                break;
            case "2":
                guestsListDirection(GuestSort.CHECKOUT_DATE);
                break;
            default:
                noSuchIndex();
                guestsListSortBy();
                break;
        }
    }

    private void guestsListDirection(GuestSort sortBy) {
        handleSortingFlow(
                direction -> controller.displayGuests(sortBy, direction)
        );
    }

    private void servicesListGuestId() {
        System.out.println("Введите id гостя, список услуг которого хотите посмотреть");
        String guestId = scanner.nextLine();

        if (controller.isGuestIdValid(guestId)) {
            servicesListSortBy(guestId);
        } else {
            System.out.println("Гостя с таким id не существует");
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

    private void addServiceToGuest() {
        System.out.println("Введите id гостя, которому хотите добавить услугу");
        String guestId = scanner.nextLine();

        if (controller.isGuestIdValid(guestId)) {
            addServiceToGuestServiceId(guestId);
        } else {
            System.out.println("Гостя с таким id не существует");
            addServiceToGuest();
        }
    }

    private void addServiceToGuestServiceId(String guestId) {
        System.out.println("Введите id услуги");
        String serviceId = scanner.nextLine();

        controller.addServiceToGuest(guestId, serviceId);
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
}
