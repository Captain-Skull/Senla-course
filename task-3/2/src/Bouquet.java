import java.util.ArrayList;
import java.util.List;

public class Bouquet {
    List<Flower> flowers;
    private String wrapping;
    private int wrappingPrice;

    public Bouquet(String wrapping, int wrappingPrice) {
        this.flowers = new ArrayList<>();
        this.wrapping = wrapping;
        this.wrappingPrice = wrappingPrice;
    }

    public void addFlower(Flower flower) {
        flowers.add(flower);
    }

    public int calculateBouquetPrice() {
        int total = wrappingPrice;
        for (Flower flower : flowers) {
            total += flower.getPrice();
        }

        return total;
    }

    public void displayBouquet() {
        System.out.println("Ваш букет");
        System.out.println("Упаковка: " + wrapping + ". (Цена: " + wrappingPrice + ")");
        System.out.println("Всего в букете " + flowers.toArray().length + " цветов: ");
        for (Flower flower : flowers) {
            System.out.println(flower.getDescription() + " по цене " + flower.getPrice());
        }
        System.out.println("Общая стоимость: " + calculateBouquetPrice());
    }
}
