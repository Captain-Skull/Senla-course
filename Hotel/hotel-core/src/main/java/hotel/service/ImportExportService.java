package hotel.service;

import enums.RoomStatus;
import exceptions.ImportExportException;
import hotel.RoomCSVConverter;
import hotel.ServiceCSVConverter;
import hotel.GuestCSVConverter;
import hotel.Guest;
import hotel.Service;
import hotel.Room;
import hotel.CSVService;
import hotel.GuestServiceUsage;
import hotel.dto.GuestWithServicesDto;
import hotel.dto.RoomWithGuestsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class ImportExportService {

    private final RoomService roomService;
    private final GuestService guestService;
    private final ServiceService serviceService;
    private final HotelServiceFacade hotelFacade;

    private final RoomCSVConverter roomCSVConverter;
    private final GuestCSVConverter guestCSVConverter;
    private final ServiceCSVConverter serviceCSVConverter;

    @Autowired
    public ImportExportService(RoomService roomService, GuestService guestService, ServiceService serviceService, HotelServiceFacade hotelFacade, RoomCSVConverter roomCSVConverter, GuestCSVConverter guestCSVConverter, ServiceCSVConverter serviceCSVConverter) {
        this.roomService = roomService;
        this.guestService = guestService;
        this.serviceService = serviceService;
        this.hotelFacade = hotelFacade;
        this.roomCSVConverter = roomCSVConverter;
        this.guestCSVConverter = guestCSVConverter;
        this.serviceCSVConverter = serviceCSVConverter;
    }

    @Transactional
    public int importRooms(String filePath) {
        List<RoomWithGuestsDto> importedRooms = CSVService.importFromCSV(filePath, roomCSVConverter);

        for (RoomWithGuestsDto dto : importedRooms) {
            Room room = dto.getRoom();

            if (roomService.isRoomExists(room.getNumber())) {
                roomService.updateRoom(room);
            } else {
                roomService.saveRoom(room);
            }

            if (dto.hasGuests()) {
                List<Guest> guests = dto.getGuests();
                if (room.getStatus() == RoomStatus.AVAILABLE) {
                    hotelFacade.checkIn(guests, room.getNumber(), room.getDaysUnderStatus());
                } else if (room.getStatus() == RoomStatus.OCCUPIED) {
                    if (areGuestGroupsIdentical(guests, guestService.getGuestsByRoom(room.getNumber()))) {
                        for (Guest guest : guests) {
                            guestService.updateGuest(guest);
                        }
                    }
                }
            }
        }

        return importedRooms.size();
    }

    public void exportRooms(String filePath) {
        List<RoomWithGuestsDto> roomsForExport = new ArrayList<>();
        for (Room room : roomService.getAllRooms()) {
            List<Guest> guests = guestService.getGuestsByRoom(room.getNumber());
            roomsForExport.add(new RoomWithGuestsDto(room, guests));
        }

        CSVService.exportToCSV(roomsForExport, filePath, roomCSVConverter);
    }

    @Transactional
    public int importGuests(String filePath) {
        List<GuestWithServicesDto> importedGuests = CSVService.importFromCSV(filePath, guestCSVConverter);
        int importedGuestsCount = importedGuests.size();
        boolean isErrorOccurred = false;
        StringBuilder errorRooms = new StringBuilder();

        Map<Integer, List<GuestWithServicesDto>> dtosByRoom = importedGuests.stream()
                .filter(dto -> dto.getGuest().getRoomNumber() > 0)
                .collect(Collectors.groupingBy(dto -> dto.getGuest().getRoomNumber()));

        for (Map.Entry<Integer, List<GuestWithServicesDto>> entry : dtosByRoom.entrySet()) {
            int roomNumber = entry.getKey();
            List<GuestWithServicesDto> roomDtos = entry.getValue();

            if (!roomService.isRoomExists(roomNumber)) {
                isErrorOccurred = true;
                errorRooms.append(roomNumber).append(" (не существует) ");
                importedGuestsCount -= roomDtos.size();
                continue;
            }

            Room room = roomService.getRoomByNumber(roomNumber);
            List<Guest> guests = roomDtos.stream()
                    .map(GuestWithServicesDto::getGuest)
                    .collect(Collectors.toList());

            if (room.getStatus() == RoomStatus.AVAILABLE) {
                if (!hotelFacade.checkIn(guests, roomNumber, 1).isEmpty()) {
                    saveGuestServices(roomDtos);
                } else {
                    isErrorOccurred = true;
                    errorRooms.append(roomNumber).append(" ");
                    importedGuestsCount -= roomDtos.size();
                }
            } else if (room.getStatus() == RoomStatus.OCCUPIED) {
                List<Guest> currentGuests = guestService.getGuestsByRoom(roomNumber);

                if (areGuestGroupsIdentical(guests, currentGuests)) {
                    for (Guest guest : guests) {
                        guestService.updateGuest(guest);
                    }
                    saveGuestServices(roomDtos);
                } else {
                    isErrorOccurred = true;
                    errorRooms.append(roomNumber).append(" (занята другими) ");
                    importedGuestsCount -= roomDtos.size();
                }
            } else {
                isErrorOccurred = true;
                errorRooms.append(roomNumber).append(" (недоступна) ");
                importedGuestsCount -= roomDtos.size();
            }
        }

        if (isErrorOccurred) {
            throw new ImportExportException("Не удалось расселить постояльцев. Комнаты: " + errorRooms);
        }

        return importedGuestsCount;
    }

    public void exportGuests(String filePath) {
        List<GuestWithServicesDto> guestsWithServices = new ArrayList<>();
        for (Guest guest : guestService.getAllGuests()) {
            List<GuestServiceUsage> usages = guestService.getGuestServices(guest.getId());
            guestsWithServices.add(new GuestWithServicesDto(guest, usages));
        }

        CSVService.exportToCSV(guestsWithServices, filePath, guestCSVConverter);
    }

    @Transactional
    public int importServices(String filePath) {
        List<Service> importedServices = CSVService.importFromCSV(filePath, serviceCSVConverter);
        for (Service service : importedServices) {
            Service existing = serviceService.getServiceById(service.getId());
            if (existing != null) {
                serviceService.updateService(service);
            } else {
                serviceService.saveService(service);
            }
        }

        return importedServices.size();
    }

    public void exportServices(String filePath) {
        List<Service> services = serviceService.getAllServices();
        CSVService.exportToCSV(services, filePath, serviceCSVConverter);
    }

    private boolean areGuestGroupsIdentical(List<Guest> group1, List<Guest> group2) {
        if (group1.size() != group2.size()) {
            return false;
        }

        Set<String> group1Ids = group1.stream()
                .map(Guest::getId)
                .collect(Collectors.toSet());

        Set<String> group2Ids = group2.stream()
                .map(Guest::getId)
                .collect(Collectors.toSet());

        return group1Ids.equals(group2Ids);
    }

    private void saveGuestServices(List<GuestWithServicesDto> dtos) {
        for (GuestWithServicesDto dto : dtos) {
            for (GuestServiceUsage usage : dto.getServiceUsages()) {
                guestService.addServiceToGuest(
                        dto.getGuest().getId(),
                        usage.getService().getId(),
                        usage.getUsageDate()
                );
            }
        }
    }
}
