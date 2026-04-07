package hotel.controller;

import enums.RoomSort;
import enums.SortDirection;
import hotel.Guest;
import hotel.Room;
import hotel.RoomGuestHistory;
import hotel.dto.CheckInRequest;
import hotel.dto.RoomDto;
import hotel.dto.RoomInfoDto;
import hotel.dto.ImportResult;
import hotel.mapper.DtoMapper;
import hotel.service.GuestService;
import hotel.service.HotelServiceFacade;
import hotel.service.ImportExportService;
import hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final GuestService guestService;
    private final DtoMapper dtoMapper;
    private final HotelServiceFacade hotelFacade;
    private final ImportExportService importExportService;


    @Autowired
    public RoomController(RoomService roomService, DtoMapper dtoMapper, HotelServiceFacade hotelFacade, GuestService guestService, ImportExportService importExportService) {
        this.roomService = roomService;
        this.dtoMapper = dtoMapper;
        this.hotelFacade = hotelFacade;
        this.guestService = guestService;
        this.importExportService = importExportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROOM_READ')")
    public List<?> getAllRooms(@RequestParam(required = false) RoomSort sortBy, @RequestParam(required = false) SortDirection direction) {
        List<?> rooms;
        if (sortBy != null) {
            rooms = hotelFacade.getSortedRooms(sortBy, direction);
        } else {
            rooms = roomService.getAllRooms();
        }

        return rooms;
    }

    @GetMapping("/available")
    @PreAuthorize("hasAuthority('ROOM_READ')")
    public List<?> getAvailableRooms(@RequestParam(required = false) RoomSort sortBy, @RequestParam(required = false) SortDirection direction) {
        List<?> rooms = sortBy != null ? hotelFacade.getSortedAvailableRooms(sortBy, direction) : roomService.getAvailableRooms();

        return rooms;
    }

    @GetMapping("/available/by-date")
    @PreAuthorize("hasAuthority('ROOM_READ')")
    public Map<Integer, RoomDto> getAvailableRoomsByDate(@RequestParam int days) {
        Map<Integer, Room> rooms = roomService.getAvailableRoomsByDate(days);
        return dtoMapper.toRoomDtoMap(rooms);
    }

    @GetMapping("/available/count")
    @PreAuthorize("hasAuthority('ROOM_READ')")
    public int getAvailableRoomsCount() {
        return roomService.getAvailableRooms().size();
    }

    @GetMapping("/information/{roomNumber}")
    @PreAuthorize("hasAuthority('ROOM_READ')")
    public RoomInfoDto getRoomInformation(@PathVariable int roomNumber) {
        return hotelFacade.getRoomInformation(roomNumber);
    }

    @PatchMapping("/{roomNumber}/price")
    @PreAuthorize("hasAuthority('ROOM_UPDATE')")
    public RoomDto setRoomPrice(@PathVariable int roomNumber, @RequestBody Map<String, Integer> body) {
        Room room = roomService.updateRoomPrice(roomNumber, body.get("price"));
        return dtoMapper.toRoomDto(room);
    }

    @PatchMapping("/{roomNumber}/status/available")
    @PreAuthorize("hasAuthority('ROOM_UPDATE')")
    public RoomDto setRoomAvailable(@PathVariable int roomNumber) {
        Room room = roomService.setRoomAvailable(roomNumber);
        return dtoMapper.toRoomDto(room);
    }

    @PatchMapping("/{roomNumber}/status/cleaning")
    @PreAuthorize("hasAuthority('ROOM_UPDATE')")
    public RoomDto setRoomCleaning(@PathVariable int roomNumber) {
        Room room = roomService.setRoomCleaning(roomNumber);
        return dtoMapper.toRoomDto(room);
    }

    @PatchMapping("/{roomNumber}/status/maintenance")
    @PreAuthorize("hasAuthority('ROOM_UPDATE')")
    public RoomDto setRoomUnderMaintenance(@PathVariable int roomNumber, @RequestParam int days) {
        Room room = roomService.setRoomUnderMaintenance(roomNumber, days);
        return dtoMapper.toRoomDto(room);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROOM_CREATE')")
    public ResponseEntity<RoomDto> addNewRoom(@RequestBody RoomDto roomDto) {
        Room room = dtoMapper.toRoom(roomDto);
        Room saved = roomService.saveRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toRoomDto(saved));
    }

    @GetMapping("/{roomNumber}/history")
    @PreAuthorize("hasAuthority('ROOM_HISTORY_READ')")
    public List<List<RoomGuestHistory>> getRoomHistory(@PathVariable int roomNumber) {
        List<List<RoomGuestHistory>> guestGroups = roomService.getRoomHistory(roomNumber);
        return guestGroups;
    }

    @PostMapping("/{roomNumber}/checkIn")
    @PreAuthorize("hasAuthority('CHECKIN')")
    public ResponseEntity<Map<String, Object>> checkIn(@PathVariable int roomNumber, @RequestBody CheckInRequest request) {
        List<Guest> checkedIn = hotelFacade.checkInRequest(request.getGuests(), roomNumber, request.getDays());
        boolean success = !checkedIn.isEmpty();

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("roomNumber", roomNumber);
        response.put("guests", dtoMapper.toGuestDtoList(checkedIn));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roomNumber}/checkOut")
    @PreAuthorize("hasAuthority('CHECKOUT')")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable int roomNumber) {
        Room room = roomService.getRoomByNumber(roomNumber);
        List<Guest> guests = guestService.getGuestsByRoom(roomNumber);
        int totalCost = room.calculateCost();

        boolean success = hotelFacade.checkOut(roomNumber);

        if (!success) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Не удалось выселить из комнаты " + roomNumber
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "roomNumber", roomNumber,
                "totalCost", totalCost,
                "guestsCount", guests.size(),
                "guests", dtoMapper.toGuestDtoList(guests)
        ));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('IMPORT')")
    public ResponseEntity<ImportResult> importRooms(@RequestBody Map<String, String> body) {
        ImportResult result = importExportService.importRooms(body.get("filePath"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('EXPORT')")
    public ResponseEntity<Map<String, String>> exportRooms(@RequestBody Map<String, String> body) {
        importExportService.exportRooms(body.get("filePath"));
        return ResponseEntity.ok(Map.of(
                "message", "Комнаты экспортированы"
        ));
    }
}
