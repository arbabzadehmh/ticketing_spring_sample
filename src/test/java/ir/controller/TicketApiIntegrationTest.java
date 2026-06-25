package ir.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.config.SecurityConfig;
import ir.controller.api.TicketApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.dto.TicketCreateDto;
import ir.dto.TicketEditDto;
import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.repository.MessageRepository;
import ir.service.MessageService;
import ir.service.SectionService;
import ir.service.TicketService;
import ir.service.UserService;
import ir.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(TicketApi.class)
@Import(SecurityConfig.class)
public class TicketApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TicketService ticketService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllTicketsForAdmin() throws Exception {

        Page<Ticket> page =
                new PageImpl<>(List.of(new Ticket()),
                        PageRequest.of(0, 10),
                        1
                );

        when(ticketService.findAll(
                any(),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/tickets")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(ticketService)
                .findAll(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void shouldGetCustomerTickets() throws Exception {

        Page<Ticket> page =
                new PageImpl<>(List.of(new Ticket()),
                        PageRequest.of(0, 10),
                        1
                );

        when(ticketService.findByCustomerUsername(
                any(),
                eq("customer"),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/tickets")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(ticketService)
                .findByCustomerUsername(
                        any(),
                        eq("customer"),
                        any(Pageable.class));
    }

    @Test
    @WithMockUser(
            username = "customer",
            authorities = "TICKET_CREATE")
    void shouldSaveTicket() throws Exception {

        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("Hi");
        dto.setContent("This is the content");

        when(messageSource.getMessage(
                eq("tickets.create.success"),
                any(),
                any()))
                .thenReturn("created");

        mockMvc.perform(
                        post("/rest/tickets")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("created"));

        verify(ticketService)
                .save(any(TicketCreateDto.class));
    }

    @Test
    @WithMockUser(authorities = "TICKET_EDIT")
    void shouldUpdateTicket() throws Exception {

        TicketEditDto dto = new TicketEditDto();

        when(messageSource.getMessage(
                eq("tickets.edit.success"),
                any(),
                any()))
                .thenReturn("updated");

        mockMvc.perform(
                        put("/rest/tickets/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("updated"));

        verify(ticketService)
                .update(eq(1L), any(TicketEditDto.class));
    }

    @Test
    @WithMockUser
    void shouldMarkTicketAsRead() throws Exception {

        mockMvc.perform(
                        put("/rest/tickets/1/mark-as-read")
                                .with(csrf())
                                .principal(() -> "user")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(ticketService)
                .markAsRead(eq(1L), any(Principal.class));
    }

    @Test
    @WithMockUser(authorities = "TICKET_DELETE")
    void shouldDeleteTicket() throws Exception {

        when(messageSource.getMessage(
                eq("tickets.delete.success"),
                any(),
                any()))
                .thenReturn("deleted");

        mockMvc.perform(
                        delete("/rest/tickets/1")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("deleted"));

        verify(ticketService)
                .deleteById(1L);
    }

    @Test
    void shouldReturn401WhenAnonymousCreatesTicket() throws Exception {

        mockMvc.perform(
                        post("/rest/tickets")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .secure(true)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldReturn403WhenUserHasNoCreatePermission() throws Exception {

        mockMvc.perform(
                        post("/rest/tickets")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }
}
