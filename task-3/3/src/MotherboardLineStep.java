public class MotherboardLineStep implements ILineStep {
    @Override
    public IProductPart buildProductPart() {
        String[] models = {"ASUS ROG", "MSI Gaming", "Gigabyte AORUS"};
        String[] chipsets = {"Intel Z790", "AMD B850", "Intel H770", "Intel Z890", "AMD X870E"};

        String model = models[(new java.util.Random()).nextInt(models.length)];
        String chipset = chipsets[(new java.util.Random()).nextInt(chipsets.length)];

        Motherboard motherboard = new Motherboard(model, chipset);
        System.out.println("Построена материнская плата: " + motherboard.getDescription());
        return motherboard;
    }
}
