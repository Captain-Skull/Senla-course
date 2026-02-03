package hotel;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceRegistry {

    private final Map<String, Service> services = new HashMap<>();

    private final HotelModel hotelModel;

    @Autowired
    public ServiceRegistry(HotelModel hotelModel) {
        this.hotelModel = hotelModel;
    }

    @PostConstruct
    private void init() {
        refreshFromModel();
    }

    public Service getServiceById(String serviceId) {
        return services.get(serviceId);
    }

    public void refreshFromModel() {
        services.clear();
        if (hotelModel != null) {
            for (Service service : hotelModel.getServicesList()) {
                services.put(service.getId(), service);
            }
        }
    }
}