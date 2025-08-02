package ir.service.impl;

import ir.model.entity.Section;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import ir.repository.TicketRepository;
import ir.service.TicketService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void save(Ticket ticket) {
        ticketRepository.save(ticket);
    }

    @Override
    public void update(Ticket ticket) {
        ticketRepository.save(ticket);
    }

    @Override
    public void delete(Long id) {
        ticketRepository.deleteById(id);
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAllByOrderByDateTime();
    }

    @Override
    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    @Override
    public List<Ticket> findByUser(User user) {
        return ticketRepository.findByUserOrderByDateTime(user);
    }

    @Override
    public List<Ticket> findByUserUsername(String username) {
        return ticketRepository.findByUserUsernameOrderByDateTime(username);
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        return ticketRepository.findByStatusOrderByDateTime(status);
    }

    @Override
    public List<Ticket> findByTitleContains(String title) {
        return ticketRepository.findByTitleIsLikeOrderByDateTime("%" + title + "%");
    }

    @Override
    public List<Ticket> findBySection(Section section) {
        return ticketRepository.findBySection_IdOrderByDateTime(section.getId());
    }

    @Override
    public List<Ticket> findByScoreLessThan(Integer score) {
        return ticketRepository.findByScoreIsLessThanEqualOrderByDateTime(score);
    }
}
