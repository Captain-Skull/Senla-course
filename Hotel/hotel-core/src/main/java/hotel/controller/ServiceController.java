package hotel.controller;

import enums.ServiceSort;
import enums.SortDirection;
import hotel.Service;
import hotel.dto.ServiceDto;
import hotel.mapper.DtoMapper;
import hotel.service.ImportExportService;
import hotel.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final DtoMapper dtoMapper;
    private final ServiceService serviceService;
    private final ImportExportService importExportService;

    @Autowired
    public ServiceController(DtoMapper dtoMapper, ServiceService serviceService, ImportExportService importExportService) {
        this.dtoMapper = dtoMapper;
        this.serviceService = serviceService;
        this.importExportService = importExportService;
    }

    @GetMapping
    public List<?> getServices(@RequestParam(required = false) ServiceSort sortBy, @RequestParam(required = false) SortDirection direction) {
        if (sortBy != null) {
            return dtoMapper.toServiceDtoList(serviceService.getSortedServices(sortBy, direction));
        }
        return dtoMapper.toServiceDtoList(serviceService.getAllServices());
    }

    @PostMapping
    public ServiceDto addNewService(@RequestBody ServiceDto serviceDto) {
        Service service = dtoMapper.toService(serviceDto);
        Service saved = serviceService.saveService(service);
        return dtoMapper.toServiceDto(saved);
    }

    @PatchMapping("/{serviceId}/price")
    public ServiceDto setServicePrice(@PathVariable String serviceId, @RequestBody Map<String, Integer> body) {
        serviceService.updateServicePrice(serviceId, body.get("price"));
        return dtoMapper.toServiceDto(serviceService.getServiceById(serviceId));
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importServices(
            @RequestBody Map<String, String> body) {
        int count = importExportService.importServices(body.get("filePath"));
        return ResponseEntity.ok(Map.of(
                "message", "Услуги импортированы",
                "count", count
        ));
    }

    @PostMapping("/export")
    public ResponseEntity<Map<String, String>> exportServices(
            @RequestBody Map<String, String> body) {
        importExportService.exportServices(body.get("filePath"));
        return ResponseEntity.ok(Map.of(
                "message", "Услуги экспортированы"
        ));
    }
}
