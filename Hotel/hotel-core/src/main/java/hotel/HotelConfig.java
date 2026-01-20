package hotel;

import annotations.Component;
import annotations.ConfigClass;
import annotations.ConfigProperty;
import annotations.Singleton;
import annotations.PropertyType;
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

    private transient boolean loadedFromFile = false;

    public HotelConfig() {
        try {
            ConfigLoader.loadConfig(this);
            loadedFromFile = true;
        } catch (Exception e) {
            System.err.println("⚠️ Config loading failed: " + e.getMessage() +
                    ", using defaults");
            loadedFromFile = false;
        }
    }

    public static HotelConfig getInstance() {
        if (instance == null) {
            instance = new HotelConfig();
        }
        return instance;
    }

    @Override
    public String toString() {
        return "HotelConfig{" +
                "hotelName='" + hotelName + '\'' +
                ", allowRoomStatusChange=" + allowRoomStatusChange +
                ", roomHistorySize=" + roomHistorySize +
                ", loadedFromFile=" + loadedFromFile +
                '}';
    }

    public boolean isAllowRoomStatusChange() {
        return allowRoomStatusChange;
    }

    public int getRoomHistorySize() {
        return roomHistorySize;
    }

    public String getHotelName() {
        return hotelName;
    }
}