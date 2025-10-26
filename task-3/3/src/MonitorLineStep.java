public class MonitorLineStep implements ILineStep {
    private Case laptopCase;

    public MonitorLineStep(Case laptopCase) {
        this.laptopCase = laptopCase;
    }

    private double calculateMonitorSize() {
        String size = laptopCase.getSize();
        String[] parts = size.split("x");

        int length = Integer.parseInt(parts[0]);
        int width = Integer.parseInt(parts[1]);

        double diagonalSm = Math.sqrt(length * length + width * width);
        double diagonalInches = diagonalSm / 2.54;
        double monitorDiagonal = Math.round(diagonalInches * 0.95 * 10.0) / 10.0;

        return monitorDiagonal;
    }

    @Override
    public IProductPart buildProductPart() {
        String[] resolutions = {"1920x1080", "2560x1440", "3840x2160"};
        String resolution = resolutions[(new java.util.Random().nextInt(resolutions.length))];

        double diagonal = calculateMonitorSize();

        Monitor monitor = new Monitor(diagonal, resolution);
        System.out.println("Построен монитор: " + monitor.getDescription());

        return monitor;
    }
}
