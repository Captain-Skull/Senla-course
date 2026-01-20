package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.PostConstruct;
import annotations.Singleton;

import java.util.HashMap;
import java.util.Map;

@Component
@Singleton
public class ServiceRegistry {

    private final Map<String, Service> services = new HashMap<>();

    @Inject
    private HotelModel hotelModel;

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