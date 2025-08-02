package ir.service;

import ir.model.entity.Section;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;

import java.util.List;

public interface TicketService {
    void save(Ticket ticket);
    void update(Ticket ticket);
    void delete(Long id);
    List<Ticket> findAll();
    Ticket findById(Long id);
    List<Ticket> findByUser(User user);
    List<Ticket> findByUserUsername(String username);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByTitleContains(String title);
    List<Ticket> findBySection(Section section);
    List<Ticket> findByScoreLessThan(Integer score);

}
