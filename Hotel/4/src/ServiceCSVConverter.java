public class ServiceCSVConverter implements CSVService.CSVConverter<Service> {
    @Override
    public String getHeaders() {
        return "id,name,price,description";
    }

    @Override
    public String toCSV(Service service) {
        return String.format("%s,%s,%d,%s",
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getDescription());
    }

    @Override
    public Service fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        String id = parts[0];
        String name = parts[1];
        int price = Integer.parseInt(parts[2]);
        String description = parts[3];

        return new Service(id, name, price, description);
    }
}