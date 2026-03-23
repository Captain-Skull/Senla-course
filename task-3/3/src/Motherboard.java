public class Motherboard implements IProductPart {
    private String model;
    private String chipset;

    public Motherboard(String model, String chipset) {
        this.model = model;
        this.chipset = chipset;
    }

    @Override
    public String getName() {
        return "Материнская плата";
    }

    @Override
    public String getDescription() {
        return "Материнская плата " + model + " с чипсетом " + chipset;
    }
}
