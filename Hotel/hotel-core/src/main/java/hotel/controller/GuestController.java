package hotel.controller;

import enums.GuestSort;
import enums.UsageServiceSort;
import enums.SortDirection;
import hotel.Guest;
import hotel.GuestServiceUsage;
import hotel.dto.GuestDto;
import hotel.mapper.DtoMapper;
import hotel.service.GuestService;
import hotel.service.ImportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final DtoMapper dtoMapper;
    private final GuestService guestService;
    private final ImportExportService importExportService;

    @Autowired
    public GuestController(DtoMapper dtoMapper, GuestService guestService, ImportExportService importExportService) {
        this.dtoMapper = dtoMapper;
        this.guestService = guestService;
        this.importExportService = importExportService;
    }

    @GetMapping
    public List<?> getGuests(@RequestParam(required = false) GuestSort sortBy, @RequestParam(required = false) SortDirection direction) {
        if (sortBy != null) {
            return guestService.getSortedGuests(sortBy, direction);
        }
        return dtoMapper.toGuestDtoList(guestService.getAllGuests());
    }

    @GetMapping("/{guestId}")
    public GuestDto getGuestById(@PathVariable String guestId) {
        Guest guest = guestService.getGuestById(guestId);
        return dtoMapper.toGuestDto(guest);
    }

    @GetMapping("/count")
    public int getGuestsCount() {
        return guestService.getGuestsCount();
    }

    @GetMapping("/{guestId}/services")
    public List<GuestServiceUsage> getServiceUsage(@PathVariable String guestId, @RequestParam UsageServiceSort sortBy, @RequestParam SortDirection direction) {
        return guestService.getGuestServiceUsageList(guestId, sortBy, direction);
    }

    @PostMapping("/{guestId}/services")
    public GuestServiceUsage addServiceToGuest(@PathVariable String guestId, @RequestBody Map<String, String> body) {
        GuestServiceUsage usage = guestService.addServiceToGuest(guestId, body.get("serviceId"));
        return usage;
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importGuests(@RequestBody Map<String, String> body) {
        int count = importExportService.importGuests(body.get("filePath"));
        return ResponseEntity.ok(Map.of(
                "message", "Гости импортированы",
                "count", count
        ));
    }

    @PostMapping("/export")
    public ResponseEntity<Map<String, String>> exportGuests(@RequestBody Map<String, String> body) {
        importExportService.exportGuests(body.get("filePath"));
        return ResponseEntity.ok(Map.of(
                "message", "Гости экспортированы"
        ));
    }
}
