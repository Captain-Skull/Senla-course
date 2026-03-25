package hotel.service;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.time.LocalDate;

@Component
public class HotelState implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String STATE_FILE = System.getProperty("user.home") + "/hotel_state.ser";;

    private LocalDate currentDay;

    public HotelState() {
        this.currentDay = LocalDate.now();
    }

    public LocalDate getCurrentDay() {
        return currentDay;
    }

    public LocalDate nextDay() {
        currentDay = currentDay.plusDays(1);
        save();
        return currentDay;
    }

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(STATE_FILE))) {
            oos.writeObject(currentDay);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения состояния", e);
        }
    }

    public void load() {
        File file = new File(STATE_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            this.currentDay = (LocalDate) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Ошибка загрузки состояния", e);
        }
    }
}
