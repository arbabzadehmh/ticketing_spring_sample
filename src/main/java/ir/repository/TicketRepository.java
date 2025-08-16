package ir.repository;

import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Page<Ticket> findByTitleIsLikeOrderByDateTime(String title, Pageable pageable);
    Page<Ticket> findAllByOrderByDateTime(Pageable pageable);
    Page<Ticket> findByCustomerOrderByDateTime(User user, Pageable pageable);
    Page<Ticket> findByCustomerUsernameOrderByDateTime(String username, Pageable pageable);
    Page<Ticket> findByStatusOrderByDateTime(TicketStatus status, Pageable pageable);
    Page<Ticket> findByScoreIsLessThanEqualOrderByDateTime(Integer score, Pageable pageable);
    Page<Ticket> findBySection_IdOrderByDateTime(Long id, Pageable pageable);
}
