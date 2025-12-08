import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StatePersistenceService {
    private static final String STATE_FILE = "hotel_state.ser";

    public static void saveHotelModel(HotelModel hotelModel) {
        try {
            saveToFile(hotelModel, STATE_FILE);

            System.out.println("✅ Состояние программы сохранено");

        } catch (IOException e) {
            System.err.println("❌ Ошибка при сохранении: " + e.getMessage());
        }
    }

    public static HotelModel loadHotelModel() {
        try {
            HotelModel model = loadFromFile(STATE_FILE);

            if (model != null) {
                System.out.println("✅ Состояние программы загружено");
                return model;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Ошибка при загрузке: " + e.getMessage());
        }

        System.out.println("⚠️ Не удалось загрузить сохраненное состояние");
        return null;
    }

    private static void saveToFile(HotelModel model, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filename)))) {
            oos.writeObject(model);
        }
    }

    private static HotelModel loadFromFile(String filename)
            throws IOException, ClassNotFoundException {
        Path path = Paths.get(filename);
        if (!Files.exists(path)) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(filename)))) {
            return (HotelModel) ois.readObject();
        }
    }

    public static boolean hasSavedState() {
        return Files.exists(Paths.get(STATE_FILE));
    }
}