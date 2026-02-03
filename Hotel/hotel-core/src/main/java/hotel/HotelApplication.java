package hotel;

import config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class HotelApplication {

    private final Controller controller;

    private final HotelConfig hotelConfig;

    @Autowired
    public HotelApplication(Controller controller, HotelConfig hotelConfig) {
        this.controller = controller;
        this.hotelConfig = hotelConfig;
    }

    public void start() {
        System.out.println(hotelConfig);
        controller.start();
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        context.registerShutdownHook();

        HotelApplication app = context.getBean(HotelApplication.class);
        app.start();
    }
}