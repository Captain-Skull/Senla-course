package hotel;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class HotelConfig implements Serializable {

    private static final long serialVersionUID = 1000L;

    @Value("${room.status.change.enabled:true}")
    private boolean allowRoomStatusChange;

    @Value("${room.history.size:3}")
    private int roomHistorySize;

    @Value("${hotel.name:Гостиница}")
    private String hotelName;

    private transient boolean loadedFromFile = false;

    public HotelConfig() {  }

    @PostConstruct
    public void init() {
        loadedFromFile = true;
        System.out.println("✅ HotelConfig загружен: " + this);
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