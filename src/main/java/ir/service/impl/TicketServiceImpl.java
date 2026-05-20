package ir.service.impl;

import ir.controller.exception.TicketClosedException;
import ir.controller.exception.TicketIsAlreadyClosedException;
import ir.controller.exception.TicketIsAlreadyScoredException;
import ir.dto.TicketCreateDto;
import ir.dto.TicketEditDto;
import ir.model.entity.Message;
import ir.model.entity.Section;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import ir.repository.MessageRepository;
import ir.repository.TicketRepository;
import ir.service.SectionService;
import ir.service.TicketService;
import ir.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ir.model.entity.Role;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final SectionService sectionService;
    private final UserService userService;
    private final MessageRepository messageRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, SectionService sectionService, UserService userService, MessageRepository messageRepository) {
        this.ticketRepository = ticketRepository;
        this.sectionService = sectionService;
        this.userService = userService;
        this.messageRepository = messageRepository;
    }

    @Transactional
    @Override
    public Ticket save(TicketCreateDto ticketDto) {

        User customer = userService.findByUsername(ticketDto.getCustomerUsername());
        Section section = sectionService.findById(ticketDto.getSectionId());

        Ticket ticket =
                Ticket
                        .builder()
                        .title(ticketDto.getTitle())
                        .status(TicketStatus.WaitingForAdmin)
                        .dateTime(LocalDateTime.now())
                        .section(section)
                        .customer(customer)
                        .build();

        ticket = ticketRepository.save(ticket);

        Message firstMessage =
                Message
                        .builder()
                        .content(ticketDto.getContent())
                        .dateTime(LocalDateTime.now())
                        .senderUsername(customer.getUsername())
                        .senderRoleName("ROLE_CUSTOMER")
                        .ticketId(ticket.getId())
                        .build();

        messageRepository.save(firstMessage);

        return ticket;
    }

    @Transactional
    @Override
    public Ticket update(Long id, TicketEditDto ticketEditDto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        Section section = sectionService.findById(ticketEditDto.getSectionId());

        ticket.setSection(section);
        ticket.setStatus(ticketEditDto.getStatus());

         return ticketRepository.save(ticket);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        ticket.setDeleted(true);
        ticketRepository.save(ticket);
    }

    @Override
    public Page<Ticket> findAll(Pageable pageable) {
        return ticketRepository.findAllByOrderByDateTime(pageable);
    }

    @Override
    public Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable) {
        return ticketRepository.findAll(spec, pageable);
    }


    @Override
    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
    }

    @Override
    public Page<Ticket> findByCustomer(User user, Pageable pageable) {
        return ticketRepository.findByCustomerOrderByDateTime(user, pageable);
    }

    @Override
    public Page<Ticket> findByCustomerUsername(Specification<Ticket> spec, String username, Pageable pageable) {
        Specification<Ticket> usernameSpec = (root, query, cb) ->
                cb.equal(root.get("customer").get("username"), username);

        Specification<Ticket> finalSpec = spec == null ? usernameSpec : spec.and(usernameSpec);

        return ticketRepository.findAll(finalSpec, pageable);
    }


    @Override
    public Page<Ticket> findByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatusOrderByDateTime(status, pageable);
    }

    @Override
    public Page<Ticket> findByTitleContains(String title, Pageable pageable) {
        return ticketRepository.findByTitleIsLikeOrderByDateTime("%" + title + "%", pageable);
    }

    @Override
    public Page<Ticket> findBySection(Section section, Pageable pageable) {
        return ticketRepository.findBySection_IdOrderByDateTime(section.getId(), pageable);
    }

    @Override
    public Page<Ticket> findByScoreLessThan(Integer score, Pageable pageable) {
        return ticketRepository.findByScoreIsLessThanEqualOrderByDateTime(score, pageable);
    }

    @Transactional
    @Override
    public void closeTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        if (ticket.getStatus() == TicketStatus.Closed) {
            throw new TicketIsAlreadyClosedException();
        }
        ticket.setStatus(TicketStatus.Closed);
        ticketRepository.save(ticket);
    }

    @Transactional
    @Override
    public void scoreTicket(Long id, Integer score) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        if (ticket.getScore() != null) {
            throw new TicketIsAlreadyScoredException();
        }

        ticket.setScore(score);
        ticketRepository.save(ticket);
    }

    @Override
    public Page<Ticket> findAllById(List<Long> ids, Pageable pageable) {
        if (ids == null || ids.isEmpty()) {
            return Page.empty(pageable);
        }
        return ticketRepository.findByIdInOrderByDateTime(ids, pageable);
    }

    @Transactional
    public void markAsRead(Long ticketId, Principal principal) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        String username = principal.getName();

        boolean isCustomer =
                ticket.getCustomer().getUsername().equals(username);

        if (isCustomer) {
            ticket.setCustomerUnread(false);
        } else {
            ticket.setAdminUnread(false);
        }

        ticketRepository.save(ticket);
    }

    @Override
    public long unreadTicketCount(User user) {

        boolean isAdminOrManager = user.hasRole("ROLE_ADMIN") || user.hasRole("ROLE_MANAGER");

        boolean isCustomer = user.hasRole("ROLE_CUSTOMER");

        if (isAdminOrManager) {
            return ticketRepository.countByAdminUnreadTrueAndStatusNot(TicketStatus.Closed);
        }

        if (isCustomer) {
            return ticketRepository.countByCustomerUnreadTrueAndCustomerAndStatusNot(user, TicketStatus.Closed);
        }

        return 0;

    }
}
