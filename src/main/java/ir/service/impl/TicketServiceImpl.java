package ir.service.impl;

import ir.dto.TicketCreateDto;
import ir.dto.TicketEditDto;
import ir.model.entity.Message;
import ir.model.entity.Section;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
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

import java.time.LocalDateTime;


@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final SectionService sectionService;
    private final UserService userService;

    public TicketServiceImpl(TicketRepository ticketRepository, SectionService sectionService, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.sectionService = sectionService;
        this.userService = userService;
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
                        .status(TicketStatus.NotSeen)
                        .dateTime(LocalDateTime.now())
                        .section(section)
                        .customer(customer)
                        .build();

        Message firstMessage =
                Message
                        .builder()
                        .content(ticketDto.getContent())
                        .dateTime(LocalDateTime.now())
                        .senderUsername(customer.getUsername())
                        .senderRoleName("ROLE_CUSTOMER")
                        .user(customer)
                        .ticket(ticket)
                        .build();

        ticket.addMessage(firstMessage);

        return ticketRepository.save(ticket);
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
    public Page<Ticket> findByCustomerUsername(String username, Pageable pageable) {
        return ticketRepository.findByCustomerUsernameOrderByDateTime(username, pageable);
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
}
