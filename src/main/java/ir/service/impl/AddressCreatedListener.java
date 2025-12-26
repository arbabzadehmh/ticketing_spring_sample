package ir.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.event.AddressCreatedEvent;
import ir.model.entity.Building;
import ir.repository.BuildingRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AddressCreatedListener {

    private final BuildingRepository buildingRepository;
    private final ObjectMapper objectMapper;

    public AddressCreatedListener(BuildingRepository buildingRepository, ObjectMapper objectMapper) {
        this.buildingRepository = buildingRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topic.address-created}", groupId = "building-service-updater")
    public void listen(String msg) {
        try {
            AddressCreatedEvent ev = objectMapper.readValue(msg, AddressCreatedEvent.class);

            // idempotency: check if building already has this addressId
            Building building = buildingRepository.findById(ev.buildingId).orElse(null);
            if (building == null) return;

            if (building.getAddressId() != null && building.getAddressId().equals(ev.addressId)) {
                return; // already applied
            }

            building.setAddressId(ev.addressId);
            buildingRepository.save(building);
        } catch (Exception ex) {
            // log, metrics, consider putting on DLQ
        }
    }
}
