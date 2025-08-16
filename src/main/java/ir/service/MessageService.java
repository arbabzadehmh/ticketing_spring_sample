package ir.service;

import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;

import java.util.List;

public interface MessageService {
    Message save(Message message);
    Message update(Message message);
    void delete(Long id);
    List<Message> findAll();
    Message findById(Long id);
    List<Message> findByUser(User user);
    List<Message> findByUserUsername(String username);
    List<Message> findByTicket(Ticket ticket);
    List<Message> findByTicketId(Long ticketId);
}
