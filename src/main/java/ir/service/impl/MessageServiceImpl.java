package ir.service.impl;

import ir.model.entity.Attachment;
import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.FileType;
import ir.model.enums.TicketStatus;
import ir.repository.MessageRepository;
import ir.repository.TicketRepository;
import ir.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final TicketRepository ticketRepository;

    public MessageServiceImpl(MessageRepository messageRepository, TicketRepository ticketRepository) {
        this.messageRepository = messageRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Message save(Long ticketId,
                        String content,
                        Principal principal,
                        List<MultipartFile> files) {

        // گرفتن Authentication از SecurityContext
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String username = auth.getName(); // نام کاربری
//        String role = auth.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .findFirst()
//                .orElse("ROLE_CUSTOMER");



        // ساختن Message جدید
        Message message = Message.builder()
                .content(content)
                .dateTime(LocalDateTime.now())
                .senderUsername(principal.getName())
                .senderRoleName(getUserRole(principal)) // متدی که رول کاربر رو بده
                .ticketId(ticketId)
                .build();

        // اگر فایل اومده باشه
        if (files != null && !files.isEmpty()) {
            List<Attachment> attachments = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    FileType fileType = mapContentTypeToFileType(file.getContentType());
                    if (fileType == null) continue;

                    Attachment attachment = Attachment.builder()
                            .fileName(file.getOriginalFilename())
                            .fileSize(file.getSize())
                            .fileType(fileType)
                            .attachTime(LocalDateTime.now())
                            .message(message)
                            .build();
                    attachments.add(attachment);
                }
            }
            message.setAttachments(attachments);
        }

        return messageRepository.save(message);
    }


    @Override
    public Message update(Message message) {
        return messageRepository.save(message);
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

//    @Override
//    public List<Message> findByUser(User user) {
//        return messageRepository.findByUserOrderByDateTime(user);
//    }

//    @Override
//    public List<Message> findByUserUsername(String username) {
//        return messageRepository.findByUserUsernameOrderByDateTime(username);
//    }

//    @Override
//    public List<Message> findByTicket(Ticket ticket) {
//        return messageRepository.findByTicketOrderByDateTime(ticket);
//    }

    @Override
    public List<Message> findByTicketId(Long ticketId) {
        return messageRepository.findByTicketIdOrderByDateTime(ticketId);
    }

    // متد برای گرفتن نقش کاربر (این بستگی به Security شما داره)
    private String getUserRole(Principal principal) {
        if (principal instanceof Authentication authentication) {
            return authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_CUSTOMER");
        }
        return "ROLE_CUSTOMER";
    }


    private FileType mapContentTypeToFileType(String contentType) {
        if (contentType == null) return null;
        switch (contentType.toLowerCase()) {
            case "image/jpeg": return FileType.JPG;
            case "image/png":  return FileType.PNG;
            case "image/bmp":  return FileType.BMP;
            case "application/pdf": return FileType.PDF;
            case "text/plain": return FileType.TXT;
            default: return null;
        }
    }
}
