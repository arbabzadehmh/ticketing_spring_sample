package ir.repository;

import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByTitleIsLikeOrderByDateTime(String title);
    List<Ticket> findAllByOrderByDateTime();
    List<Ticket> findByUserOrderByDateTime(User user);
    List<Ticket> findByUserUsernameOrderByDateTime(String username);
    List<Ticket> findByStatusOrderByDateTime(TicketStatus status);
    List<Ticket> findByScoreIsLessThanEqualOrderByDateTime(Integer score);
    List<Ticket> findBySection_IdOrderByDateTime(Long id);
}
