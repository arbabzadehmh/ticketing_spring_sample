package ir.controller;

import ir.controller.web.BuildingController;
import ir.dto.BuildingTableDto;
import ir.service.BuildingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BuildingControllerTest {

    @Mock
    private BuildingService buildingService;

    @InjectMocks
    private BuildingController controller;


    @Test
    void shouldReturnBuildingPage() {

        Page<BuildingTableDto> page =
                new PageImpl<>(List.of(
                        new BuildingTableDto(
                                1L,
                                "Building A",
                                List.of("09123456789"),
                                List.of("Section 1"),
                                List.of(1L),
                                "Tehran, Street 1",
                                0L
                        )
                ));

        when(buildingService.findAllForTable(
                any(Pageable.class),
                isNull()
        )).thenReturn(page);

        Model model = new ExtendedModelMap();

        String view = controller.buildingsList(
                0,
                10,
                false,
                model
        );


        assertEquals("building", view);

        assertEquals(page, model.getAttribute("buildings"));

        verify(buildingService)
                .findAllForTable(any(Pageable.class), isNull());
    }


    @Test
    void shouldReturnFragmentWhenRequested() {

        Page<BuildingTableDto> page =
                new PageImpl<>(Collections.emptyList());

        when(buildingService.findAllForTable(any(), isNull()))
                .thenReturn(page);


        String view = controller.buildingsList(
                0,
                10,
                true,
                new ExtendedModelMap()
        );


        assertEquals(
                "fragments/building-fragments/buildings-table :: buildings-table",
                view
        );
    }


    @Test
    void shouldUseDefaultSizeWhenSizeIsInvalid() {

        Page<BuildingTableDto> page =
                new PageImpl<>(List.of());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        when(buildingService.findAllForTable(any(), isNull()))
                .thenReturn(page);


        controller.buildingsList(
                0,
                -5,
                false,
                new ExtendedModelMap()
        );


        verify(buildingService)
                .findAllForTable(captor.capture(), isNull());


        assertEquals(
                10,
                captor.getValue().getPageSize()
        );
    }
}
