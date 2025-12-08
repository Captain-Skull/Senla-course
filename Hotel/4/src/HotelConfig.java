import annotations.*;
import config.ConfigLoader;

import java.io.Serializable;

@ConfigClass(configFileName = "hotel.properties")
@Component
@Singleton
public class HotelConfig implements Serializable {
    private static final long serialVersionUID = 1000L;
    private static HotelConfig instance;

    @ConfigProperty(propertyName = "room.status.change.enabled", type = PropertyType.BOOLEAN)
    private boolean allowRoomStatusChange = true;

    @ConfigProperty(propertyName = "room.history.size", type = PropertyType.INTEGER)
    private int roomHistorySize = 3;

    @ConfigProperty(propertyName = "hotel.name")
    private String hotelName = "Гостиница";

    private HotelConfig() {
        ConfigLoader.loadConfig(this);
    }

    public static HotelConfig getInstance() {
        if (instance == null) {
            instance = new HotelConfig();
        }
        return instance;
    }

    public boolean isAllowRoomStatusChange() { return allowRoomStatusChange; }
    public int getRoomHistorySize() { return roomHistorySize; }
    public String getHotelName() { return hotelName; }
}