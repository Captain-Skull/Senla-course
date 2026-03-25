public class Case implements IProductPart {
    private String material;
    private String size;
    private String color;

    public Case (String material, String size, String color) {
        this.material = material;
        this.size = size;
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    @Override
    public String getName() {
        return "Корпус";
    }

    @Override
    public String getDescription() {
        return color + " корпус из " + material + ", размер: " + size;
    }
}
