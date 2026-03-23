package hotel.dto;

import enums.RoomStatus;
import enums.RoomType;

import java.time.LocalDate;

public class RoomDto {

    private int number;
    private RoomType roomType;
    private int price;
    private int capacity;
    private RoomStatus status;
    private int daysUnderStatus;
    private LocalDate endDate;

    public RoomDto() {
    }

    public RoomDto(int number, RoomType roomType, int price, int capacity,
                   RoomStatus status, int daysUnderStatus, LocalDate endDate) {
        this.number = number;
        this.roomType = roomType;
        this.price = price;
        this.capacity = capacity;
        this.status = status;
        this.daysUnderStatus = daysUnderStatus;
        this.endDate = endDate;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public int getDaysUnderStatus() {
        return daysUnderStatus;
    }

    public void setDaysUnderStatus(int daysUnderStatus) {
        this.daysUnderStatus = daysUnderStatus;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}