package ir.controller;

import ir.config.SecurityConfig;
import ir.controller.api.BuildingApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.dto.BuildingTableDto;
import ir.service.BuildingService;
import ir.service.impl.CustomUserDetailsService;
import ir.service.impl.EntityLockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(BuildingApi.class)
@Import(SecurityConfig.class)
public class BuildingApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;


    @MockitoBean
    BuildingService service;


    @MockitoBean
    MessageSource messageSource;


    @MockitoBean
    EntityLockService lockService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;


    @Test
    @WithMockUser
    void shouldReturnBuildings() throws Exception {

        Page<BuildingTableDto> page =
                new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(service.findAllForTable(
                any(Pageable.class),
                any()
        )).thenReturn(page);


        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        get("/rest/buildings/get-all")
                                .secure(true)
                )
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(authorities = "BUILDING_DELETE")
    void shouldDeleteBuilding() throws Exception {


        when(messageSource.getMessage(
                eq("buildings.delete.success"),
                isNull(),
                any(Locale.class)
        )).thenReturn("Deleted");


        mockMvc.perform(
                        delete("/rest/buildings/1")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Deleted"));


        verify(service)
                .deleteById(1L);
    }


    @Test
    @WithMockUser
    void shouldRejectDeleteWithoutAuthority() throws Exception {

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        delete("/rest/buildings/1")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(authorities = "BUILDING_CREATE")
    void shouldSaveBuilding() throws Exception {

        when(messageSource.getMessage(
                eq("buildings.create.success"),
                isNull(),
                any(Locale.class)
        )).thenReturn("Created");

        mockMvc.perform(
                        post("/rest/buildings")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequestJson())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Created"));

        verify(service)
                .save(any(), any());
    }


    @Test
    @WithMockUser(authorities = "BUILDING_CREATE")
    void shouldReturnValidationErrorsWhenSaveRequestInvalid() throws Exception {

        String invalidJson = """
            {
              "building": {
                "title": ""
              },
              "addressDto": {}
            }
            """;

        mockMvc.perform(
                        post("/rest/buildings")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                                .secure(true)
                )
                .andExpect(status().isBadRequest());

        verify(service, never())
                .save(any(), any());
    }


    @Test
    @WithMockUser
    void shouldRejectSaveWithoutAuthority() throws Exception {

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        post("/rest/buildings")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequestJson())
                                .secure(true)
                )
                .andExpect(status().isForbidden());

        verify(service, never())
                .save(any(), any());
    }


    @Test
    @WithMockUser(authorities = "BUILDING_EDIT")
    void shouldEditBuilding() throws Exception {

        when(messageSource.getMessage(
                eq("buildings.edit.success"),
                isNull(),
                any(Locale.class)
        )).thenReturn("Edited");

        mockMvc.perform(
                        put("/rest/buildings")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequestJson())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Edited"));

        verify(service)
                .edit(any(), any());
    }

    @Test
    @WithMockUser(authorities = "BUILDING_EDIT")
    void shouldReturnValidationErrorsWhenEditRequestInvalid() throws Exception {

        String invalidJson = """
            {
              "building": {
                "title": ""
              },
              "addressDto": {}
            }
            """;

        mockMvc.perform(
                        put("/rest/buildings")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                                .secure(true)
                )
                .andExpect(status().isBadRequest());

        verify(service, never())
                .edit(any(), any());
    }

    @Test
    @WithMockUser
    void shouldRejectEditWithoutAuthority() throws Exception {

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        put("/rest/buildings")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequestJson())
                                .secure(true)
                )
                .andExpect(status().isForbidden());

        verify(service, never())
                .edit(any(), any());
    }

    @Test
    @WithMockUser(
            username = "ali",
            authorities = "BUILDING_EDIT"
    )
    void shouldStartEdit() throws Exception {

        mockMvc.perform(
                        post("/rest/buildings/1/edit-start")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("locked"));

        verify(lockService)
                .lock("building", 1L, "ali");
    }

    @Test
    @WithMockUser
    void shouldRejectStartEditWithoutAuthority() throws Exception {

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        post("/rest/buildings/1/edit-start")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isForbidden());

        verify(lockService, never())
                .lock(anyString(), anyLong(), anyString());
    }

    @Test
    @WithMockUser(
            username = "ali",
            authorities = "BUILDING_EDIT"
    )
    void shouldStopEdit() throws Exception {

        mockMvc.perform(
                        post("/rest/buildings/1/edit-stop")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(lockService)
                .unlock("building", 1L, "ali");
    }

    @Test
    @WithMockUser
    void shouldRejectStopEditWithoutAuthority() throws Exception {

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        post("/rest/buildings/1/edit-stop")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isForbidden());

        verify(lockService, never())
                .unlock(anyString(), anyLong(), anyString());
    }

    private String validRequestJson() {
        return """
            {
              "building": {
                "id": 1,
                "title": "BuildingA",
                "phoneNumbers": [
                  "09121234567"
                ]
              },
              "addressDto": {
                "id": 1,
                "country": "Iran",
                "state": "Tehran",
                "city": "Tehran",
                "village": "Village",
                "region": "Region1",
                "street": "Valiasr Street",
                "platesNumber": "12",
                "floor": "3",
                "unit": "15",
                "postalCode": "1234567890"
              }
            }
            """;
    }
}
