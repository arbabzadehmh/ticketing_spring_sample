package ir.repository;

import ir.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByOrderByDateTime();

    Page<Message> findByTicket_Id(Long ticketId, Pageable pageable);
    long countByTicket_Id(Long ticketId);


    @Query("select max(m.dateTime) from messageEntity m where m.ticket.id = :ticketId")
    LocalDateTime findLastMessageTime(Long ticketId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
update messageEntity m
set m.seenByAdmin = true
where m.ticket.id = :ticketId
and m.senderRoleName = 'ROLE_CUSTOMER'
and m.seenByAdmin = false
""")
    void markSeenByAdmin(@Param("ticketId") Long ticketId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
update messageEntity m
set m.seenByCustomer = true
where m.ticket.id = :ticketId
and m.senderRoleName <> 'ROLE_CUSTOMER'
and m.seenByCustomer = false
""")
    void markSeenByCustomer(@Param("ticketId") Long ticketId);

}
