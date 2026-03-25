public class Laptop implements IProduct {
    private IProductPart casePart;
    private IProductPart motherboard;
    private IProductPart monitor;

    @Override
    public void installFirstPart(IProductPart part) {
        this.casePart = part;
        System.out.println("Корпус успешно установлен: " + part.getDescription());
    }

    @Override
    public void installSecondPart(IProductPart part) {
        this.motherboard = part;
        System.out.println("Материнская плата успешно установлена: " + part.getDescription());
    }

    @Override
    public void installThirdPart(IProductPart part) {
        this.monitor = part;
        System.out.println("Монитор успешно установлен: " + part.getDescription());
    }

    @Override
    public String getProductInfo() {
        return "Собран ноутбук:\n" +
            casePart.getDescription() + "\n" +
            motherboard.getDescription() + "\n" +
            monitor.getDescription();
    }
}
