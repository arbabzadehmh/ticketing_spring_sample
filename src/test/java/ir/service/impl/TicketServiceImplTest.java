package ir.service.impl;

import ir.controller.exception.TicketIsAlreadyScoredException;
import ir.dto.TicketCreateDto;
import ir.dto.TicketEditDto;
import ir.model.entity.Section;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import ir.repository.MessageRepository;
import ir.repository.TicketRepository;
import ir.service.SectionService;
import ir.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityNotFoundException;
import ir.model.entity.Message;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;


import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserService userService;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void save_shouldCreateTicketAndFirstMessage() {

        TicketCreateDto dto = new TicketCreateDto();

        dto.setTitle("Problem");
        dto.setContent("Help me");
        dto.setCustomerUsername("customer");
        dto.setSectionId(1L);

        User customer = new User();
        customer.setUsername("customer");

        Section section = new Section();
        section.setId(1L);
        section.setTitle("Support");

        when(userService.findByUsername("customer"))
                .thenReturn(customer);

        when(sectionService.findById(1L))
                .thenReturn(section);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> {

                    Ticket t = invocation.getArgument(0);

                    t.setId(1L);

                    return t;
                });

        Ticket result = ticketService.save(dto);

        assertEquals("Problem", result.getTitle());

        assertEquals(1L, result.getSectionId());

        assertEquals("Support", result.getSectionTitle());

        verify(ticketRepository).save(any(Ticket.class));

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void update_shouldUpdateSectionSnapshot() {

        Ticket ticket = new Ticket();

        TicketEditDto dto = new TicketEditDto();

        dto.setSectionId(2L);

        Section section = new Section();

        section.setId(2L);
        section.setTitle("Technical Support");

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(sectionService.findById(2L))
                .thenReturn(section);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(i -> i.getArgument(0));

        Ticket result =
                ticketService.update(1L, dto);

        assertEquals(2L, result.getSectionId());

        assertEquals("Technical Support", result.getSectionTitle());

        verify(ticketRepository)
                .save(ticket);
    }

    @Test
    void update_shouldUpdateStatus() {

        Ticket ticket = new Ticket();

        TicketEditDto dto = new TicketEditDto();
        dto.setStatus(TicketStatus.Closed);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(i -> i.getArgument(0));

        Ticket result = ticketService.update(1L, dto);

        assertEquals(TicketStatus.Closed, result.getStatus());

        verify(ticketRepository)
                .save(ticket);
    }

    @Test
    void update_shouldThrowWhenTicketNotFound() {

        TicketEditDto dto = new TicketEditDto();

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> ticketService.update(1L, dto)
        );
    }

    @Test
    void deleteById_shouldMarkTicketDeleted() {

        Ticket ticket = new Ticket();

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        ticketService.deleteById(1L);

        assertTrue(ticket.isDeleted());

        verify(ticketRepository).save(ticket);
    }

    @Test
    void deleteById_shouldThrowWhenTicketNotFound() {

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> ticketService.deleteById(1L)
        );
    }

    @Test
    void closeTicket_shouldCloseTicket() {

        Principal principal =
                () -> "customer";

        Ticket ticket = new Ticket();

        ticket.setStatus(TicketStatus.WaitingForAdmin);

        when(ticketRepository.findByIdAndCustomerUsername(1L, "customer"))
                .thenReturn(Optional.of(ticket));

        ticketService.closeTicket(1L, principal);

        assertEquals(TicketStatus.Closed, ticket.getStatus());

        verify(ticketRepository).save(ticket);
    }

    @Test
    void scoreTicket_shouldSetScore() {

        Principal principal = () -> "customer";

        Ticket ticket = new Ticket();

        ticket.setScore(null);

        when(ticketRepository.findByIdAndCustomerUsername(1L, "customer"))
                .thenReturn(Optional.of(ticket));

        ticketService.scoreTicket(1L, 5, principal);

        assertEquals(5, ticket.getScore());

        verify(ticketRepository)
                .save(ticket);
    }

    @Test
    void scoreTicket_shouldThrowWhenAlreadyScored() {

        Principal principal = () -> "customer";

        Ticket ticket = new Ticket();

        ticket.setScore(4);

        when(ticketRepository.findByIdAndCustomerUsername(1L, "customer"))
                .thenReturn(Optional.of(ticket));

        assertThrows(
                TicketIsAlreadyScoredException.class,
                () -> ticketService.scoreTicket(1L, 5, principal)
        );

        verify(ticketRepository, never())
                .save(any(Ticket.class));
    }

    @Test
    void scoreTicket_shouldThrowAccessDeniedException() {

        Principal principal = () -> "customer";

        when(ticketRepository.findByIdAndCustomerUsername(1L, "customer"))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> ticketService.scoreTicket(1L, 5, principal)
        );
    }
}
