package hotel.dto;

import hotel.Guest;
import hotel.GuestServiceUsage;

import java.util.List;

public class GuestWithServicesDto {

    private final Guest guest;
    private final List<GuestServiceUsage> serviceUsages;

    public GuestWithServicesDto(Guest guest, List<GuestServiceUsage> serviceUsages) {
        this.guest = guest;
        this.serviceUsages = serviceUsages != null ? serviceUsages : List.of();
    }

    public Guest getGuest() {
        return guest;
    }

    public List<GuestServiceUsage> getServiceUsages() {
        return serviceUsages;
    }

    public boolean hasServices() {
        return !serviceUsages.isEmpty();
    }
}