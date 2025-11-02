public abstract class Flower {
    private String name;
    private int price;
    private String color;

    public Flower(String name, int price, String color) {
        this.name = name;
        this.price = price;
        this.color = color;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return color + " " + name;
    };
}
