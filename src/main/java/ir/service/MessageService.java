package ir.service;

import ir.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface MessageService {
    Message save(Long ticketId, String content, Principal principal, List<MultipartFile> files);
    Message saveOcrMessage(Long ticketId, Principal principal, MultipartFile file);
    Message update(Message message);
    void delete(Long id);
    List<Message> findAll();
    Page<Message> findByTicketId(Long ticketId, int page, int size);
    Message findById(Long id);
    void markMessagesAsSeen(Long ticketId, Principal principal);
}
