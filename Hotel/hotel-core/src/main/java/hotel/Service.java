package hotel;

import java.io.Serializable;

public class Service implements Serializable {

    private static final long serialVersionUID = 0004L;

    private String id;
    private String name;
    private int price;
    private String description;

    public Service(String id, String name, int price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getFullDescription() {
        return "Услуга: " + name + " (id: " + id + ") цена: " + price + "\nОписание: " + description;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
