public class FlowerShop {
    public static void main (String[] args) {
        Bouquet bouquet = new Bouquet("Праздничная обертка", 150);

        bouquet.addFlower(new Rose(100, "Белая"));
        bouquet.addFlower(new Rose(200, "Красная"));
        bouquet.addFlower(new Gladiolus(300, "Фиолетовый"));
        bouquet.addFlower(new Lily(150, "Розовая"));
        bouquet.addFlower(new Orchid(170, "Розовая"));
        bouquet.addFlower(new Tulip(180, "Желтый"));
        bouquet.addFlower(new Tulip(180, "Красный"));

        bouquet.displayBouquet();
    }
}
