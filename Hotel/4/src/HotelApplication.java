
import annotations.Inject;
import di.ApplicationContext;
import di.Injector;

public class HotelApplication {
    @Inject
    private HotelModel hotelModel;

    @Inject
    private HotelView hotelView;

    @Inject
    private Controller controller;

    @Inject
    private HotelConfig hotelConfig;

    public HotelApplication() {
    }

    public void start() {
        System.out.println(hotelConfig);

        // Запускаем контроллер
        controller.start();
    }

    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.getInstance();

        initializeApplication(context);

        HotelApplication app = context.getBean(HotelApplication.class);

        Injector.injectDependencies(app, context);

        app.start();
    }

    private static void initializeApplication(ApplicationContext context) {
        HotelConfig config = HotelConfig.getInstance();
        HotelModel model = new HotelModel();
        HotelView view = new HotelView();
        Controller controller = new Controller();
        HotelApplication app = new HotelApplication();

        // Регистрируем компоненты
        context.registerBean(HotelConfig.class, config);
        context.registerBean(HotelModel.class, model);
        context.registerBean(HotelView.class, view);
        context.registerBean(Controller.class, controller);
        context.registerBean(HotelApplication.class, app);
    }
}