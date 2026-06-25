package ir.controller;

import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.BuildingController;
import ir.controller.web.GlobalModelAttribute;
import ir.dto.BuildingTableDto;
import ir.service.BuildingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BuildingController.class)
public class BuildingControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;


    @MockitoBean
    BuildingService buildingService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;


    @Test
    @WithMockUser
    void shouldReturnBuildingView() throws Exception {

        Page<BuildingTableDto> page =
                new PageImpl<>(List.of());


        when(buildingService.findAllForTable(
                any(Pageable.class),
                isNull()
        )).thenReturn(page);


        mockMvc.perform(get("/buildings"))
                .andExpect(status().isOk())
                .andExpect(view().name("building"))
                .andExpect(model().attributeExists("buildings"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }


    @Test
    @WithMockUser
    void shouldReturnFragment() throws Exception {

        when(buildingService.findAllForTable(any(), isNull()))
                .thenReturn(Page.empty());


        mockMvc.perform(
                        get("/buildings")
                                .param("fragment", "true")
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name(
                                "fragments/building-fragments/buildings-table :: buildings-table"
                        )
                );
    }
}
