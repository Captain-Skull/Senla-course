public class Monitor implements IProductPart {
    private double size;
    private String resolution;

    public Monitor (double size, String resolution) {
        this.size = size;
        this.resolution = resolution;
    }

    @Override
    public String getName() {
        return "Монитор";
    }

    @Override
    public String getDescription() {
        return "Монитор с диагональю " + size + " дюймов и разрешением " + resolution;
    }
}
