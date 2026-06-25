package ir.controller;

import ir.controller.api.AddressApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.dto.AddressDto;
import ir.service.AddressClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(AddressApi.class)
public class AddressApiTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AddressClient addressClient;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @Test
    @WithMockUser
    void shouldReturnAddresses() throws Exception {

        AddressDto addressDto =
                AddressDto
                        .builder()
                        .id(1L)
                        .country("Iran")
                        .state("Tehran")
                        .city("Ab ali")
                        .village("-")
                        .region("2")
                        .street("Hafez")
                        .platesNumber("3")
                        .floor("1ST")
                        .unit("10")
                        .postalCode("1981958111")
                        .build();



        List<AddressDto> addresses =
                List.of(addressDto);

        when(addressClient.findAll())
                .thenReturn(addresses);

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(get("/rest/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].country").value("Iran"))
                .andExpect(jsonPath("$[0].state").value("Tehran"))
                .andExpect(jsonPath("$[0].city").value("Ab ali"));

        verify(addressClient).findAll();
    }
}
