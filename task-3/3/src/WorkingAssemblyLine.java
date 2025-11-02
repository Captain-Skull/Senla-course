public class WorkingAssemblyLine {
    public static void main(String[] args) {
        System.out.println("Конвейер запущен");

        ILineStep caseStep = new CaseLineStep();
        Case casePart = (Case) caseStep.buildProductPart();

        ILineStep motherboardStep = new MotherboardLineStep();
        IProductPart motherboard = motherboardStep.buildProductPart();

        ILineStep monitorStep = new MonitorLineStep(casePart);
        IProductPart monitor = monitorStep.buildProductPart();

        IProduct laptop = new Laptop();

        AssemblyLine assemblyLine = new AssemblyLine(casePart, motherboard, monitor);

        laptop = assemblyLine.assembleProduct(laptop);
        System.out.println(laptop.getProductInfo());

        System.out.println("Конвейер остановлен");
    }
}
