package ir.service;

import ir.dto.TicketCreateDto;
import ir.model.entity.Section;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface TicketService {
    Ticket save(TicketCreateDto ticketDto);
    Ticket update(Ticket ticket);
    void deleteById(Long id);
    Page<Ticket> findAll(Pageable pageable);
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);
    Ticket findById(Long id);
    Page<Ticket> findByCustomer(User user, Pageable pageable);
    Page<Ticket> findByCustomerUsername(String username, Pageable pageable);
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    Page<Ticket> findByTitleContains(String title, Pageable pageable);
    Page<Ticket> findBySection(Section section, Pageable pageable);
    Page<Ticket> findByScoreLessThan(Integer score, Pageable pageable);

}
