public class CaseLineStep implements ILineStep {
    @Override
    public IProductPart buildProductPart() {
        String[] colors = {"Серый", "Черный", "Белый"};
        String[] sizes = {"30x20", "27x18", "25x20"};
        String[] materials = {"пластика", "алюминия"};

        String color = colors[(new java.util.Random().nextInt(colors.length))];
        String size = sizes[(new java.util.Random().nextInt(sizes.length))];
        String material = materials[(new java.util.Random().nextInt(materials.length))];

        Case casePart = new Case(material, size, color);
        System.out.println("Построен корпус: " + casePart.getDescription());
        return casePart;
    }
}
