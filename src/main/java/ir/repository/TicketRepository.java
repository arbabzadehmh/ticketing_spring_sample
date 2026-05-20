package ir.repository;

import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Page<Ticket> findByTitleIsLikeOrderByDateTime(String title, Pageable pageable);
    Page<Ticket> findAllByOrderByDateTime(Pageable pageable);
    Page<Ticket> findByCustomerOrderByDateTime(User user, Pageable pageable);
    Page<Ticket> findByCustomerUsernameOrderByDateTime(String username, Pageable pageable);
    Page<Ticket> findByStatusOrderByDateTime(TicketStatus status, Pageable pageable);
    Page<Ticket> findByScoreIsLessThanEqualOrderByDateTime(Integer score, Pageable pageable);
    Page<Ticket> findBySection_IdOrderByDateTime(Long id, Pageable pageable);
    Page<Ticket> findByIdInOrderByDateTime(List<Long> ids, Pageable pageable);
    long countByStatus(TicketStatus status);
    long countByStatusAndCustomer(TicketStatus status, User customer);
    long countByAdminUnreadTrueAndStatusNot(TicketStatus status);
    long countByCustomerUnreadTrueAndCustomerAndStatusNot(User customer, TicketStatus status);


    @Query("""
        SELECT t
        FROM ticketEntity t
        WHERE t.status <> :closedStatus
          AND (
                 (SELECT MAX(m.dateTime) FROM messageEntity m WHERE m.ticketId = t.id) < :threshold
                 OR NOT EXISTS (SELECT 1 FROM messageEntity m WHERE m.ticketId = t.id)
          )
    """)
    List<Ticket> findTicketsToAutoClose(
            @Param("closedStatus") TicketStatus closedStatus,
            @Param("threshold") LocalDateTime threshold
    );


    @Query("""
    SELECT t
    FROM ticketEntity t
    WHERE t.score IS NOT NULL
      AND t.score < :thresholdScore
      AND (
            SELECT MAX(m.dateTime)
            FROM messageEntity m
            WHERE m.ticketId = t.id
          ) BETWEEN :start AND :end
""")
    List<Ticket> findTicketsWithLowScoreInPeriod(
            @Param("thresholdScore") Integer thresholdScore,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}
