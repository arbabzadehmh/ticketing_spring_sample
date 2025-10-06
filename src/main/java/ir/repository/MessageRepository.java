package ir.repository;

import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByOrderByDateTime();

//    List<Message> findByUserUsernameOrderByDateTime(String username);
    List<Message> findByTicketIdOrderByDateTime(Long ticketId);
}
