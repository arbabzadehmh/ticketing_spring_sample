package ir.service.impl;

import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import ir.repository.MessageRepository;
import ir.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void save(Message message) {
        message.getTicket().setStatus(TicketStatus.NotSeen);
        messageRepository.save(message);
    }

    @Override
    public void update(Message message) {
        messageRepository.save(message);
    }

    @Override
    public void delete(Long id) {
        messageRepository.deleteById(id);
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAllByOrderByDateTime();
    }

    @Override
    public Message findById(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    @Override
    public List<Message> findByUser(User user) {
        return messageRepository.findByUserOrderByDateTime(user);
    }

    @Override
    public List<Message> findByUserUsername(String username) {
        return messageRepository.findByUserUsernameOrderByDateTime(username);
    }

    @Override
    public List<Message> findByTicket(Ticket ticket) {
        return messageRepository.findByTicketOrderByDateTime(ticket);
    }

    @Override
    public List<Message> findByTicketId(Long ticketId) {
        return messageRepository.findByTicketIdOrderByDateTime(ticketId);
    }
}
