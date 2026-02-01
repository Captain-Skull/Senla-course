package hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "services")
public class Service implements Serializable {

    private static final long serialVersionUID = 0004L;

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "price", nullable = false)
    private int price;
    @Column(name = "description", nullable = false)
    private String description;

    public Service() {  }

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

    public void setId(String id) {
        this.id = id;
    }
}
