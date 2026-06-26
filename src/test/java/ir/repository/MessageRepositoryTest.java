package ir.repository;

import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageRepositoryTest extends BaseRepositoryTest{

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void shouldFindLastMessageTime() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("ali")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .customer(user)
                        .build()
        );

        LocalDateTime first =
                LocalDateTime.of(2026, 10, 20, 20, 30);

        LocalDateTime second =
                LocalDateTime.of(2026, 10, 25, 22, 45);

        messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("ali")
                        .senderRoleName("ROLE_CUSTOMER")
                        .dateTime(first)
                        .build()
        );

        messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("ali")
                        .senderRoleName("ROLE_CUSTOMER")
                        .dateTime(second)
                        .build()
        );

        LocalDateTime result =
                messageRepository.findLastMessageTime(ticket.getId());

        assertEquals(second, result);
    }

    @Test
    @Transactional
    void shouldMarkSeenByAdmin() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("ali")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .customer(user)
                        .build()
        );

        Message msg = messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("ali")
                        .senderRoleName("ROLE_CUSTOMER")
                        .seenByAdmin(false)
                        .build()
        );

        messageRepository.markSeenByAdmin(ticket.getId());

        entityManager.clear();

        Message updated =
                messageRepository.findById(msg.getId()).orElseThrow();

        assertTrue(updated.isSeenByAdmin());
    }

    @Test
    @Transactional
    void shouldMarkSeenByCustomer() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("ali")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .customer(user)
                        .build()
        );

        Message msg = messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("admin")
                        .senderRoleName("ROLE_ADMIN")
                        .seenByCustomer(false)
                        .build()
        );

        messageRepository.markSeenByCustomer(ticket.getId());

        entityManager.clear();

        Message updated =
                messageRepository.findById(msg.getId()).orElseThrow();

        assertTrue(updated.isSeenByCustomer());
    }

    @Test
    void shouldFindMessagesByTicketId() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("ali")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .customer(user)
                        .build()
        );

        messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("ali")
                        .senderRoleName("ROLE_CUSTOMER")
                        .build()
        );

        Page<Message> result =
                messageRepository.findByTicket_Id(
                        ticket.getId(),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldCountMessagesByTicketId() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("ali")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .customer(user)
                        .build()
        );

        messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("ali")
                        .senderRoleName("ROLE_CUSTOMER")
                        .build()
        );

        messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("ali")
                        .senderRoleName("ROLE_CUSTOMER")
                        .build()
        );

        long count =
                messageRepository.countByTicket_Id(ticket.getId());

        assertEquals(2, count);
    }

}
