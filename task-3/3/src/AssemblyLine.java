public class AssemblyLine implements IAssemblyLine {
    private IProductPart  casePart;
    private IProductPart motherboard;
    private IProductPart monitor;

    public AssemblyLine(IProductPart casePart, IProductPart motherboard, IProductPart monitor) {
        this.casePart = casePart;
        this.motherboard = motherboard;
        this.monitor = monitor;
    }

    @Override
    public IProduct assembleProduct(IProduct product) {
        System.out.println("Заготовка получена, приступаем к сборке...");

        System.out.println("Сборка корпуса");
        product.installFirstPart(casePart);

        System.out.println("Установка материнской платы");
        product.installSecondPart(motherboard);

        System.out.println("Установка монитора");
        product.installThirdPart(monitor);

        System.out.println("Сборка завершена");
        return product;
    }
}
