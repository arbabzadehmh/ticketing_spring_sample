package ir.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.event.AddressCreatedEvent;
import ir.model.entity.Building;
import ir.repository.BuildingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressCreatedListenerTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AddressCreatedListener listener;


    @Test
    void listen_shouldUpdateBuildingAddress() throws Exception {

        String msg = "json";

        AddressCreatedEvent event = new AddressCreatedEvent();
        event.buildingId = 1L;
        event.addressId = 100L;

        Building building = new Building();
        building.setId(1L);

        when(objectMapper.readValue(msg, AddressCreatedEvent.class))
                .thenReturn(event);

        when(buildingRepository.findById(1L))
                .thenReturn(Optional.of(building));

        listener.listen(msg);

        assertEquals(100L, building.getAddressId());

        verify(buildingRepository).save(building);
    }


    @Test
    void listen_shouldDoNothingWhenBuildingNotFound() throws Exception {

        String msg = "json";

        AddressCreatedEvent event = new AddressCreatedEvent();
        event.buildingId = 1L;
        event.addressId = 100L;

        when(objectMapper.readValue(msg, AddressCreatedEvent.class))
                .thenReturn(event);

        when(buildingRepository.findById(1L))
                .thenReturn(Optional.empty());

        listener.listen(msg);

        verify(buildingRepository, never()).save(any());
    }


    @Test
    void listen_shouldIgnoreDuplicateAddressEvent() throws Exception {

        String msg = "json";

        AddressCreatedEvent event = new AddressCreatedEvent();
        event.buildingId = 1L;
        event.addressId = 100L;

        Building building = new Building();
        building.setId(1L);
        building.setAddressId(100L);

        when(objectMapper.readValue(msg, AddressCreatedEvent.class))
                .thenReturn(event);

        when(buildingRepository.findById(1L))
                .thenReturn(Optional.of(building));

        listener.listen(msg);

        verify(buildingRepository, never()).save(any());
    }


    @Test
    void listen_shouldHandleInvalidMessage() throws Exception {

        String msg = "invalid-json";

        when(objectMapper.readValue(msg, AddressCreatedEvent.class))
                .thenThrow(new RuntimeException("Invalid JSON"));

        assertDoesNotThrow(() -> listener.listen(msg));

        verify(buildingRepository, never()).findById(any());
        verify(buildingRepository, never()).save(any());
    }

}
