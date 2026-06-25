package ir.controller;

import ir.controller.api.BuildingApi;
import ir.dto.BuildingTableDto;
import ir.service.BuildingService;
import ir.service.impl.EntityLockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BuildingApiTest {

    @Mock
    BuildingService service;


    @Mock
    MessageSource messageSource;


    @Mock
    EntityLockService lockService;


    @InjectMocks
    BuildingApi api;


    @Test
    void shouldReturnBuildings() {

        Page<BuildingTableDto> page =
                new PageImpl<>(List.of());


        when(service.findAllForTable(
                any(Pageable.class),
                any()
        )).thenReturn(page);


        ResponseEntity<?> response =
                api.getAllBuildingsForTable(
                        0,
                        10,
                        null
                );


        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                page,
                response.getBody()
        );
    }
}
