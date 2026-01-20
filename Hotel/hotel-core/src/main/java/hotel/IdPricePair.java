package hotel;

public class IdPricePair {

    private String id;
    private int price;

    public IdPricePair(String id, int price) {
        this.id = id;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return id + " - " + price;
    }
}
