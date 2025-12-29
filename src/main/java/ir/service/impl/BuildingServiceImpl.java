package ir.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import ir.controller.exception.AddressEmptyException;
import ir.controller.exception.DuplicateBuildingException;
import ir.controller.exception.DuplicateSectionException;
import ir.dto.AddressDto;
import ir.dto.BuildingTableDto;
import ir.event.AddressCreateRequestEvent;
import ir.model.entity.*;
import ir.repository.BuildingRepository;
import ir.repository.OutboxRepository;
import ir.repository.SectionRepository;
import ir.service.AddressClient;
import ir.service.BuildingService;
import ir.service.SectionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

import static org.springframework.util.StringUtils.hasText;


@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final AddressClient addressClient;
    private final AddressFormatter addressFormatter;
    private final SectionRepository sectionRepository;
    private final SectionService sectionService;

    public BuildingServiceImpl(BuildingRepository buildingRepository, OutboxRepository outboxRepo, ObjectMapper objectMapper, AddressClient addressClient, AddressFormatter addressFormatter, SectionRepository sectionRepository, SectionService sectionService) {
        this.buildingRepository = buildingRepository;
        this.outboxRepository = outboxRepo;
        this.objectMapper = objectMapper;
        this.addressClient = addressClient;
        this.addressFormatter = addressFormatter;
        this.sectionRepository = sectionRepository;
        this.sectionService = sectionService;
    }

    @Transactional
    @Override
    public Building save(Building building, AddressDto addressDto) {

        if (buildingRepository.existsByTitle(building.getTitle())) {
            throw new DuplicateBuildingException();
        }

        if(building.getAddressId() == null && addressDto == null) {
            throw new AddressEmptyException();
        }

        if (building.getAddressId() != null) {
            return saveWithExistingAddress(building);
        } else {
            return saveWithNewAddress(building, addressDto);
        }
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        Building building = buildingRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Building not found"));

        if (building.getSectionList() != null) {
            building.getSectionList().forEach(section -> section.setBuilding(null));
            sectionRepository.saveAll(building.getSectionList());
            building.setSectionList(new ArrayList<>());
        }

        building.setDeleted(true);
        buildingRepository.save(building);
    }

    private Building saveWithExistingAddress(Building building) {

        Building savedBuilding = Building.builder()
                .title(building.getTitle())
                .phoneNumbers(building.getPhoneNumbers())
                .addressId(building.getAddressId())
                .build();

        savedBuilding = buildingRepository.save(savedBuilding);



        final Building finalSavedBuilding = savedBuilding;

        if (building.getSectionList() != null && !building.getSectionList().isEmpty()) {

            List<Long> sectionIds = building.getSectionList()
                    .stream()
                    .map(Section::getId)
                    .toList();

            //  سکشن‌ها را managed بگیر
            List<Section> managedSections = sectionRepository.findAllById(sectionIds);

            //  اتصال دوطرفه
            managedSections.forEach(section -> section.setBuilding(finalSavedBuilding));

            //  ست کن روی building
            savedBuilding.setSectionList(managedSections);

            sectionService.saveAll(managedSections);
        }

        return savedBuilding;
    }

//    private Building saveWithNewAddress(Building building, AddressDto addressDto) {
//
//        try{
//            Building savedBuilding = Building.builder()
//                    .title(building.getTitle())
//                    .phoneNumbers(building.getPhoneNumbers())
//                    .sectionList(building.getSectionList())
//                    .build();
//
//    savedBuilding = buildingRepository.save(savedBuilding);

//            // prepare AddressCreateRequestEvent
//            AddressCreateRequestEvent ev = new AddressCreateRequestEvent();
//            ev.eventId = UUID.randomUUID().toString();
//            ev.buildingId = savedBuilding.getId();
//            ev.address = addressDto;
//            ev.createdAt = Instant.now().toEpochMilli();
//
//            String payload = objectMapper.writeValueAsString(ev);
//
//            OutboxEvent out = new OutboxEvent();
//            out.setAggregateType("BUILDING");
//            out.setAggregateId(savedBuilding.getId());
//            out.setEventType("ADDRESS_CREATE_REQUEST");
//            out.setPayload(payload);
//            out.setPublished(false);
//            outboxRepository.save(out);
//
//            return savedBuilding;
//        } catch (JsonProcessingException e){
//            throw new RuntimeException("Failed to serialize event", e);
//        }
//    }

    @SneakyThrows
    private Building saveWithNewAddress(Building building, AddressDto addressDto) {

        if (addressDto == null) {
            throw new AddressEmptyException();
        }

        Building savedBuilding = Building.builder()
                .title(building.getTitle())
                .phoneNumbers(building.getPhoneNumbers())
                .build();

        savedBuilding = buildingRepository.save(savedBuilding);
        final Building finalSavedBuilding = savedBuilding;


        if (building.getSectionList() != null && !building.getSectionList().isEmpty()) {

            List<Long> sectionIds = building.getSectionList()
                    .stream()
                    .map(Section::getId)
                    .toList();

            //  سکشن‌ها را managed بگیر
            List<Section> managedSections = sectionRepository.findAllById(sectionIds);

            //  اتصال دوطرفه
            managedSections.forEach(section -> section.setBuilding(finalSavedBuilding));

            //  ست کن روی building
            savedBuilding.setSectionList(managedSections);

            sectionService.saveAll(managedSections);
        }

        // prepare AddressCreateRequestEvent
        AddressCreateRequestEvent ev = new AddressCreateRequestEvent();
        ev.eventId = UUID.randomUUID().toString();
        ev.buildingId = savedBuilding.getId();
        ev.address = addressDto;
        ev.createdAt = Instant.now().toEpochMilli();

        String payload = objectMapper.writeValueAsString(ev);

        OutboxEvent out = new OutboxEvent();
        out.setAggregateType("BUILDING");
        out.setAggregateId(savedBuilding.getId());
        out.setEventType("ADDRESS_CREATE_REQUEST");
        out.setPayload(payload);
        out.setPublished(false);
        outboxRepository.save(out);

        return savedBuilding;

    }

    @Override
    public Page<BuildingTableDto> findAllForTable(
            Pageable pageable,
            String searchTitle
    ) {

        Page<Building> page = hasText(searchTitle)
                ? buildingRepository.findByTitleContainingIgnoreCase(searchTitle, pageable)
                : buildingRepository.findAll(pageable);

        Map<Long, AddressDto> addressMap = fetchAddresses(page.getContent());

        return page.map(b -> toTableDto(b, addressMap));
    }

    private Map<Long, AddressDto> fetchAddresses(List<Building> buildings) {

        List<Long> addressIds = buildings.stream()
                .map(Building::getAddressId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return addressIds.isEmpty()
                ? Map.of()
                : addressClient.findByIds(addressIds);
    }

    private BuildingTableDto toTableDto(
            Building b,
            Map<Long, AddressDto> addressMap
    ) {
        return new BuildingTableDto(
                b.getId(),
                b.getTitle(),
                b.getPhoneNumbers(),
                b.getSectionList()
                        .stream()
                        .map(Section::getTitle)
                        .toList(),
                addressFormatter.format(
                        addressMap.get(b.getAddressId())
                )
        );
    }


    @Override
    public Page<Building> findAll(Pageable pageable) {
        return buildingRepository.findAll(pageable);
    }

    @Override
    public List<Building> findAll() {
        return buildingRepository.findAll();
    }

    @Override
    public Page<Building> findByTitleContaining(String title, Pageable pageable) {
        return buildingRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
}
