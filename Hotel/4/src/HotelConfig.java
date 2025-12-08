import java.io.*;
import java.util.Properties;

public class HotelConfig implements Serializable {
    private static final long serialVersionUID = 1000L;

    private static final String CONFIG_FILE = "hotel.properties";
    private static HotelConfig instance;

    private boolean allowRoomStatusChange = true;
    private int roomHistorySize = 3;

    private HotelConfig() {
        loadConfig();
    }

    public static HotelConfig getInstance() {
        if (instance == null) {
            instance = new HotelConfig();
        }
        return instance;
    }

    private void loadConfig() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);

        try {
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);

                    allowRoomStatusChange = Boolean.parseBoolean(
                            props.getProperty("room.status.change.enabled", "true"));

                    roomHistorySize = Integer.parseInt(
                            props.getProperty("room.history.size", "3"));

                    System.out.println("✅ Конфигурация загружена из " + CONFIG_FILE);
                }
            } else {
                saveDefaultConfig();
                System.out.println("📄 Создан файл конфигурации: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.out.println("⚠️ Не удалось загрузить конфигурацию: " + e.getMessage());
            System.out.println("Используются настройки по умолчанию");
        }
    }

    private void saveDefaultConfig() {
        Properties props = new Properties();
        props.setProperty("room.status.change.enabled", "true");
        props.setProperty("room.history.size", "3");
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Hotel Management System Configuration");
        } catch (IOException e) {
            System.err.println("Не удалось сохранить конфигурацию: " + e.getMessage());
        }
    }

    public boolean isAllowRoomStatusChange() {
        return allowRoomStatusChange;
    }

    public int getRoomHistorySize() {
        return roomHistorySize;
    }

    @Override
    public String toString() {
        return "Конфигурация отеля:\n" +
                "  Изменение статуса комнат: " + (allowRoomStatusChange ? "разрешено" : "запрещено") + "\n" +
                "  Размер истории постояльцев: " + roomHistorySize;
    }
}