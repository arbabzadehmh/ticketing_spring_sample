package ir.repository;

import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TicketRepositoryTest extends BaseRepositoryTest {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void shouldFindTicketsByCustomerUsername() {

        User user = User.builder()
                .username("ali")
                .password("123")
                .build();

        user = userRepository.saveAndFlush(user);

        Ticket ticket = Ticket.builder()
                .title("ticket1")
                .customer(user)
                .dateTime(LocalDateTime.now())
                .build();

        ticketRepository.saveAndFlush(ticket);

        Page<Ticket> result =
                ticketRepository.findByCustomerUsernameOrderByDateTime(
                        "ali",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "ticket1",
                result.getContent().get(0).getTitle()
        );
    }

    @Test
    void shouldCountTicketsByStatus() {

        Ticket t1 = Ticket.builder()
                .title("t1")
                .status(TicketStatus.WaitingForCustomer)
                .build();

        Ticket t2 = Ticket.builder()
                .title("t2")
                .status(TicketStatus.WaitingForCustomer)
                .build();

        ticketRepository.save(t1);
        ticketRepository.save(t2);

        long count =
                ticketRepository.countByStatus(
                        TicketStatus.WaitingForCustomer
                );

        assertEquals(2, count);
    }

    @Test
    void shouldCountTicketsByStatusAndCustomer() {

        User user = User.builder()
                .username("ali")
                .password("123")
                .build();

        user = userRepository.saveAndFlush(user);

        Ticket ticket = Ticket.builder()
                .title("ticket")
                .customer(user)
                .status(TicketStatus.WaitingForAdmin)
                .build();

        ticketRepository.save(ticket);

        long count =
                ticketRepository.countByStatusAndCustomer(
                        TicketStatus.WaitingForAdmin,
                        user
                );

        assertEquals(1, count);
    }

    @Test
    void shouldCountUnreadAdminTickets() {

        Ticket ticket = Ticket.builder()
                .title("ticket")
                .adminUnread(true)
                .status(TicketStatus.WaitingForAdmin)
                .build();

        ticketRepository.save(ticket);

        long count =
                ticketRepository.countByAdminUnreadTrueAndStatusNot(
                        TicketStatus.WaitingForCustomer
                );

        assertEquals(1, count);
    }

    @Test
    void shouldCountUnreadCustomerTickets() {

        User user = User.builder()
                .username("ali")
                .password("123")
                .build();

        user = userRepository.saveAndFlush(user);

        Ticket ticket = Ticket.builder()
                .title("ticket")
                .customer(user)
                .customerUnread(true)
                .status(TicketStatus.WaitingForCustomer)
                .build();

        ticketRepository.save(ticket);

        long count =
                ticketRepository
                        .countByCustomerUnreadTrueAndCustomerAndStatusNot(
                                user,
                                TicketStatus.Closed
                        );

        assertEquals(1, count);
    }

    @Test
    void shouldFindTicketsToAutoClose() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("ali")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .status(TicketStatus.WaitingForAdmin)
                        .customer(user)
                        .dateTime(LocalDateTime.now().minusDays(10))
                        .build()
        );

        messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("ali")
                        .senderRoleName("ROLE_CUSTOMER")
                        .dateTime(LocalDateTime.now().minusDays(8))
                        .build()
        );

        List<Ticket> result =
                ticketRepository.findTicketsToAutoClose(
                        TicketStatus.Closed,
                        LocalDateTime.now().minusDays(5)
                );

        assertEquals(1, result.size());
        assertEquals(ticket.getId(), result.get(0).getId());
    }

    @Test
    void shouldFindTicketWithoutMessagesToAutoClose() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("ali2")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .status(TicketStatus.WaitingForCustomer)
                        .customer(user)
                        .build()
        );

        List<Ticket> result =
                ticketRepository.findTicketsToAutoClose(
                        TicketStatus.Closed,
                        LocalDateTime.now().minusDays(5)
                );

        assertEquals(1, result.size());
    }

    @Test
    void shouldFindLowScoreTicketsInPeriod() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("reza")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("low-score")
                        .score(2)
                        .customer(user)
                        .build()
        );

        LocalDateTime msgTime =
                LocalDateTime.now().minusDays(2);

        messageRepository.saveAndFlush(
                Message.builder()
                        .ticket(ticket)
                        .senderUsername("reza")
                        .senderRoleName("ROLE_CUSTOMER")
                        .dateTime(msgTime)
                        .build()
        );

        List<Ticket> result =
                ticketRepository.findTicketsWithLowScoreInPeriod(
                        3,
                        LocalDateTime.now().minusDays(3),
                        LocalDateTime.now()
                );

        assertEquals(1, result.size());
        assertEquals(ticket.getId(), result.get(0).getId());
    }

    @Test
    @Transactional
    void shouldUpdateSectionTitleBySectionId() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("mohammad")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .sectionId(10L)
                        .sectionTitle("Old")
                        .customer(user)
                        .build()
        );

        ticketRepository.updateSectionTitleBySectionId(
                10L,
                "New Section"
        );

        entityManager.flush();
        entityManager.clear();

        Ticket updated =
                ticketRepository.findById(ticket.getId()).orElseThrow();

        assertEquals(
                "New Section",
                updated.getSectionTitle()
        );
    }

    @Test
    @Transactional
    void shouldMarkTicketsSectionDeleted() {

        User user = userRepository.saveAndFlush(
                User.builder()
                        .username("user1")
                        .password("123")
                        .build()
        );

        Ticket ticket = ticketRepository.saveAndFlush(
                Ticket.builder()
                        .title("ticket")
                        .sectionId(50L)
                        .sectionTitle("Support")
                        .customer(user)
                        .build()
        );

        ticketRepository.markTicketsSectionDeleted(50L);

        entityManager.flush();
        entityManager.clear();

        Ticket updated =
                ticketRepository.findById(ticket.getId()).orElseThrow();

        assertEquals(
                "DELETED SECTION",
                updated.getSectionTitle()
        );
    }
}
