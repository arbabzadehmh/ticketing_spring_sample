package ir.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.controller.exception.AddressEmptyException;
import ir.controller.exception.DuplicateBuildingException;
import ir.controller.exception.EntityLockedException;
import ir.dto.AddressDto;
import ir.dto.BuildingTableDto;
import ir.model.entity.Building;
import ir.model.entity.OutboxEvent;
import ir.repository.BuildingRepository;
import ir.repository.OutboxRepository;
import ir.repository.SectionRepository;
import ir.service.AddressClient;
import ir.service.SectionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BuildingServiceImplTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SectionService sectionService;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AddressClient addressClient;

    @Mock
    private AddressFormatter addressFormatter;

    @Mock
    private EntityLockService entityLockService;

    @InjectMocks
    private BuildingServiceImpl buildingService;

    @Test
    void save_shouldThrowDuplicateBuildingException() {

        Building building = new Building();
        building.setTitle("Main Building");

        when(buildingRepository.existsByTitle("Main Building"))
                .thenReturn(true);

        assertThrows(
                DuplicateBuildingException.class,
                () -> buildingService.save(building, null)
        );
    }

    @Test
    void save_shouldThrowAddressEmptyException() {

        Building building = new Building();
        building.setTitle("Main");

        when(buildingRepository.existsByTitle("Main"))
                .thenReturn(false);

        assertThrows(
                AddressEmptyException.class,
                () -> buildingService.save(building, null)
        );
    }

    @Test
    void save_shouldSaveWithExistingAddress() {

        Building building = new Building();

        building.setTitle("Main");
        building.setAddressId(10L);

        when(buildingRepository.existsByTitle("Main"))
                .thenReturn(false);

        when(buildingRepository.save(any(Building.class)))
                .thenAnswer(i -> i.getArgument(0));

        Building result =
                buildingService.save(building, null);

        assertEquals(
                10L,
                result.getAddressId()
        );

        verify(buildingRepository)
                .save(any(Building.class));
    }

    @Test
    void save_shouldCreateOutboxEventForNewAddress() throws Exception {

        Building building = new Building();
        building.setTitle("Main");

        AddressDto dto = new AddressDto();

        when(buildingRepository.existsByTitle("Main"))
                .thenReturn(false);

        when(buildingRepository.save(any(Building.class)))
                .thenAnswer(invocation -> {

                    Building b =
                            invocation.getArgument(0);

                    b.setId(1L);

                    return b;
                });

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        buildingService.save(building, dto);

        verify(outboxRepository)
                .save(any(OutboxEvent.class));
    }

    @Test
    void edit_shouldThrowWhenBuildingNotFound() {

        Building building = new Building();

        when(buildingRepository.findById(1L))
                .thenReturn(Optional.empty());

        building.setId(1L);

        assertThrows(
                EntityNotFoundException.class,
                () -> buildingService.edit(
                        building,
                        new AddressDto()
                )
        );
    }

    @Test
    void edit_shouldThrowOptimisticLockException() {

        Building existing = new Building();
        existing.setVersion(1L);

        Building incoming = new Building();
        incoming.setId(1L);
        incoming.setVersion(2L);

        when(buildingRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        assertThrows(
                OptimisticLockException.class,
                () -> buildingService.edit(
                        incoming,
                        new AddressDto()
                )
        );
    }

    @Test
    void edit_shouldThrowDuplicateBuildingException() {

        Building existing = new Building();

        existing.setId(1L);
        existing.setVersion(1L);

        Building incoming = new Building();

        incoming.setId(1L);
        incoming.setVersion(1L);
        incoming.setTitle("Main");

        when(buildingRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(buildingRepository
                .existsByTitleAndIdNot(
                        "Main",
                        1L
                ))
                .thenReturn(true);

        assertThrows(
                DuplicateBuildingException.class,
                () -> buildingService.edit(
                        incoming,
                        new AddressDto()
                )
        );
    }

    @Test
    void deleteById_shouldThrowWhenLocked() {

        when(entityLockService
                .getLockOwner("building", 1L))
                .thenReturn("admin");

        assertThrows(
                EntityLockedException.class,
                () -> buildingService.deleteById(1L)
        );

        verify(buildingRepository, never())
                .findById(anyLong());
    }

    @Test
    void deleteById_shouldThrowWhenBuildingNotFound() {

        when(entityLockService
                .getLockOwner("building", 1L))
                .thenReturn(null);

        when(buildingRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> buildingService.deleteById(1L)
        );
    }

    @Test
    void deleteById_shouldMarkBuildingDeleted() {

        Building building = new Building();

        when(entityLockService
                .getLockOwner("building", 1L))
                .thenReturn(null);

        when(buildingRepository.findById(1L))
                .thenReturn(Optional.of(building));

        buildingService.deleteById(1L);

        assertTrue(
                building.isDeleted()
        );

        verify(buildingRepository)
                .save(building);
    }

    @Test
    void findAllForTable_shouldSearchByTitle() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Building building = new Building();
        building.setId(1L);
        building.setTitle("Main Building");
        building.setAddressId(100L);
        building.setPhoneNumbers(List.of("123"));
        building.setSectionList(new ArrayList<>());
        building.setVersion(1L);

        Page<Building> page =
                new PageImpl<>(List.of(building));

        AddressDto address = new AddressDto();

        when(buildingRepository
                .findByTitleContainingIgnoreCase("main", pageable))
                .thenReturn(page);

        when(addressClient.findByIds(List.of(100L)))
                .thenReturn(Map.of(100L, address));

        when(addressFormatter.format(address))
                .thenReturn("Address");

        Page<BuildingTableDto> result =
                buildingService.findAllForTable(pageable, "main");

        assertEquals(1, result.getTotalElements());

    }
}
