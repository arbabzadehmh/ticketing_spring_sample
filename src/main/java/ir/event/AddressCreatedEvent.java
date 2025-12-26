package ir.event;

import ir.dto.AddressDto;

public class AddressCreatedEvent {

    public String eventId;      // same or new UUID
    public Long buildingId;
    public Long addressId;
    public AddressDto address;
    public long createdAt;
}
