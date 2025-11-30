public class HotelApplication {
    public static void main(String[] args) {
        HotelModel model = new HotelModel("Гранд Отель");
        HotelView view = new HotelView();
        Controller controller = new Controller(model, view);

        controller.start();
    }
}
