package ir.event;

import ir.dto.AddressDto;

public class AddressCreateRequestEvent {

    public String eventId;      // UUID for idempotency
    public Long buildingId;
    public AddressDto address;
    public long createdAt;
}
