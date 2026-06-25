package ir.controller;

import ir.config.SecurityConfig;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.controller.web.TicketController;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(TicketController.class)
@Import(SecurityConfig.class)
public class TicketControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TicketService ticketService;

    @MockitoBean
    MessageService messageService;

    @MockitoBean
    SectionService sectionService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    MessageRepository MessageRepository;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAllTicketsForAdmin() throws Exception {

        Ticket ticket = new Ticket();
        User user = new User();
        user.setUsername("ali");

        ticket.setCustomer(user);

        Page<Ticket> page =
                new PageImpl<>(List.of(ticket));

        when(ticketService.findAll(any(Pageable.class)))
                .thenReturn(page);

        when(sectionService.findAllForFilter())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/tickets")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("ticket"))
                .andExpect(model().attributeExists("tickets"))
                .andExpect(model().attributeExists("sectionsForFilter"))
                .andExpect(model().attributeExists("ticketStatuses"));

        verify(ticketService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void shouldShowCustomerTickets() throws Exception {

        User user = new User();

        Ticket ticket = new Ticket();
        user.setUsername("ali");

        ticket.setCustomer(user);

        Page<Ticket> page =
                new PageImpl<>(List.of(ticket));

        when(userService.findByUsername("customer"))
                .thenReturn(user);

        when(ticketService.findByCustomer(
                eq(user),
                any(Pageable.class)))
                .thenReturn(page);

        when(sectionService.findAllForFilter())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/tickets")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("ticket"));

        verify(ticketService)
                .findByCustomer(eq(user), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnFragment() throws Exception {

        Ticket ticket = new Ticket();
        User user = new User();
        user.setUsername("ali");

        ticket.setCustomer(user);

        Page<Ticket> page =
                new PageImpl<>(List.of(ticket));

        when(ticketService.findAll(any(Pageable.class)))
                .thenReturn(page);

        when(sectionService.findAllForFilter())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/tickets")
                                .param("fragment", "true")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/ticket-fragments/tickets-table :: tickets-table"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUseDefaultSizeWhenSizeIsInvalid() throws Exception {

        Ticket ticket = new Ticket();
        User user = new User();
        user.setUsername("ali");

        ticket.setCustomer(user);

        Page<Ticket> page =
                new PageImpl<>(List.of(ticket));

        when(ticketService.findAll(any(Pageable.class)))
                .thenReturn(page);

        when(sectionService.findAllForFilter())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/tickets")
                                .param("size", "0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(ticketService).findAll(captor.capture());

        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    @WithMockUser
    void shouldShowTicketMessages() throws Exception {

        Ticket ticket = new Ticket();

        Message message = new Message();

        Page<Message> page =
                new PageImpl<>(List.of(message));

        when(ticketService.findById(1L))
                .thenReturn(ticket);

        when(messageService.findByTicketId(1L, 0, 50))
                .thenReturn(page);

        mockMvc.perform(
                        get("/tickets/1")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("message"))
                .andExpect(model().attributeExists("ticket"))
                .andExpect(model().attributeExists("messages"));

        verify(messageService)
                .findByTicketId(1L, 0, 50);
    }
}
