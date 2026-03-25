package hotel.mapper;

import hotel.Guest;
import hotel.Room;
import hotel.Service;
import hotel.dto.GuestDto;
import hotel.dto.GuestRequest;
import hotel.dto.RoomDto;
import hotel.dto.ServiceDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DtoMapper {

    public RoomDto toRoomDto(Room room) {
        return new RoomDto(
                room.getNumber(),
                room.getType(),
                room.getPrice(),
                room.getCapacity(),
                room.getStatus(),
                room.getDaysUnderStatus(),
                room.getEndDate()
        );
    }

    public Room toRoom(RoomDto dto) {
        Room room = new Room(dto.getNumber(), dto.getRoomType(), dto.getPrice(), dto.getCapacity());
        return room;
    }

    public Map<Integer, RoomDto> toRoomDtoMap(Map<Integer, Room> rooms) {
        Map<Integer, RoomDto> roomsDto = new HashMap<>();
        rooms.forEach((roomNumber, room) -> roomsDto.put(roomNumber, toRoomDto(room)));
        return roomsDto;
    }

    public Service toService(ServiceDto dto) {
        Service service = new Service(dto.getId(), dto.getName(), dto.getPrice(), dto.getDescription());
        return service;
    }

    public ServiceDto toServiceDto(Service service) {
        return new ServiceDto(service.getId(), service.getName(), service.getPrice(), service.getDescription());
    }

    public List<ServiceDto> toServiceDtoList(List<Service> services) {
        List<ServiceDto> servicesDto = new ArrayList<>();

        for (Service service : services) {
            servicesDto.add(toServiceDto(service));
        }

        return servicesDto;
    }

    public Guest toGuest(GuestDto dto) {
        return new Guest(dto.getId(), dto.getFirstname(), dto.getLastname());
    }

    public Guest requestToGuest(GuestRequest dto) {
        return new Guest(null, dto.getFirstname(), dto.getLastname());
    }

    public GuestDto toGuestDto(Guest guest) {
        return new GuestDto(guest.getId(), guest.getFirstName(), guest.getLastName(), guest.getRoomNumber());
    }

    public List<GuestDto> toGuestDtoList(List<Guest> guests) {
        List<GuestDto> guestsDto = new ArrayList<>();

        for (Guest guest : guests) {
            guestsDto.add(toGuestDto(guest));
        }

        return guestsDto;
    }
}
