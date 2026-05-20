package ir.service.impl;

import ir.controller.exception.FileStorageException;
import ir.controller.exception.TicketClosedException;
import ir.controller.exception.TicketExpiredException;
import ir.model.entity.*;
import ir.model.enums.FileType;
import ir.model.enums.TicketStatus;
import ir.repository.AttachmentRepository;
import ir.repository.MessageRepository;
import ir.repository.TicketRepository;
import ir.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final FileStorageService fileStorageService;
    private final AttachmentRepository attachmentRepository;
    private final OcrService ocrService;
    private final TicketRepository ticketRepository;

    public MessageServiceImpl(MessageRepository messageRepository, FileStorageService fileStorageService, AttachmentRepository attachmentRepository, OcrService ocrService, TicketRepository ticketRepository) {
        this.messageRepository = messageRepository;
        this.fileStorageService = fileStorageService;
        this.attachmentRepository = attachmentRepository;
        this.ocrService = ocrService;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(noRollbackFor = TicketExpiredException.class)
    @Override
    public Message save(Long ticketId,
                        String content,
                        Principal principal,
                        List<MultipartFile> files) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        LocalDateTime lastMessageTime = messageRepository.findLastMessageTime(ticketId);

        if (lastMessageTime != null &&
                lastMessageTime.isBefore(LocalDateTime.now().minusDays(7))) {

            ticket.setStatus(TicketStatus.Closed);
            ticketRepository.save(ticket);

            throw new TicketExpiredException();
        }

        String role = getUserRole(principal);  // متدی که رول کاربر رو بده

        // ساختن Message جدید
        Message message = Message.builder()
                .content(content)
                .dateTime(LocalDateTime.now())
                .senderUsername(principal.getName())
                .senderRoleName(role)
                .ticketId(ticketId)
                .seenByAdmin(
                        role.equals("ROLE_ADMIN") ||
                                role.equals("ROLE_MANAGER")
                )

                .seenByCustomer(
                        role.equals("ROLE_CUSTOMER")
                )
                .build();

        messageRepository.save(message);

        updateTicketStatusOnNewMessage(ticket, message);

        if (files == null || files.isEmpty()) {
            return message;
        }

        List<Attachment> attachments = new ArrayList<>();
        List<String> storedMongoIds = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                // store in GridFS
                String mongoId = fileStorageService.store(file, principal.getName());
                storedMongoIds.add(mongoId);

                // map contentType -> FileType enum (implement mapContentTypeToFileType)
                FileType fileType = mapContentTypeToFileType(file.getContentType());

                Attachment att = Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .fileType(fileType)
                        .attachTime(LocalDateTime.now())
                        .mongoFileId(mongoId)
                        .message(message)  // set FK to message
                        .build();

                attachments.add(att);
            }

            // save attachments metadata in Oracle
            attachmentRepository.saveAll(attachments);

            message.setAttachments(attachments);
            messageRepository.save(message);

            return message;

        } catch (Exception ex) {
            // cleanup GridFS files that were created
            for (String id : storedMongoIds) {
                try {
                    fileStorageService.deleteById(id);
                } catch (Exception ignore) {

                }
            }
            // optionally delete the message we saved (if you want atomic-like behaviour)
            // messageRepository.deleteById(message.getId());

            throw new FileStorageException();
        }
    }

    @Transactional
    @Override
    public Message saveOcrMessage(Long ticketId,
                                  Principal principal,
                                  MultipartFile file) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        LocalDateTime lastMessageTime = messageRepository.findLastMessageTime(ticketId);

        if (lastMessageTime != null &&
                lastMessageTime.isBefore(LocalDateTime.now().minusDays(7))) {

            ticket.setStatus(TicketStatus.Closed);
            ticketRepository.save(ticket);

            throw new TicketExpiredException();
        }

        String role = getUserRole(principal);

        Message message = Message.builder()
                .content("")
                .dateTime(LocalDateTime.now())
                .senderUsername(principal.getName())
                .senderRoleName(role)
                .ticketId(ticketId)
                .seenByAdmin(
                        role.equals("ROLE_ADMIN") ||
                                role.equals("ROLE_MANAGER")
                )

                .seenByCustomer(
                        role.equals("ROLE_CUSTOMER")
                )
                .build();

        messageRepository.save(message);

        updateTicketStatusOnNewMessage(ticket, message);

        if (file == null || file.isEmpty()) {
            return message;
        }

        try {

            String mongoId = fileStorageService.store(file, principal.getName());
            FileType fileType = mapContentTypeToFileType(file.getContentType());

            Attachment attachment = Attachment.builder()
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .attachTime(LocalDateTime.now())
                    .mongoFileId(mongoId)
                    .message(message)
                    .build();

            ocrService.extractTextSync(attachment);
            attachmentRepository.save(attachment);

            message.addAttachment(attachment);
            messageRepository.save(message);

            return message;

        } catch (Exception ex) {
            throw new FileStorageException();
        }
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
    public Page<Message> findByTicketId(Long ticketId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("dateTime").descending()
        );

        return messageRepository.findByTicketId(ticketId, pageable);
    }

    @Override
    public Message findById(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void markMessagesAsSeen(Long ticketId, Principal principal) {

        String role = getUserRole(principal);

        if (role.equals("ROLE_CUSTOMER")) {

            messageRepository.markSeenByCustomer(ticketId);

        } else {

            messageRepository.markSeenByAdmin(ticketId);

        }
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

//    @Override
//    public List<Message> findByTicketId(Long ticketId) {
//        return messageRepository.findByTicketIdOrderByDateTime(ticketId);
//    }

    private void updateTicketStatusOnNewMessage(Ticket ticket, Message message) {

        // 1) بررسی بسته بودن تیکت
        if (ticket.getStatus() == TicketStatus.Closed) {
            throw new TicketClosedException();
        }

        // 3) تعیین وضعیت بر اساس role فرستنده
        if (message.getSenderRoleName().equals("ROLE_CUSTOMER")) {
            ticket.setAdminUnread(true);
            ticket.setCustomerUnread(false);

            ticket.setStatus(TicketStatus.WaitingForAdmin);

        } else if (message.getSenderRoleName().equals("ROLE_ADMIN") || message.getSenderRoleName().equals("ROLE_MANAGER")) {
            ticket.setCustomerUnread(true);
            ticket.setAdminUnread(false);

            ticket.setStatus(TicketStatus.WaitingForCustomer);
        }

        ticketRepository.save(ticket);
    }


    // متد برای گرفتن نقش کاربر (این بستگی به Security شما داره)
    private String getUserRole(Principal principal) {
        CustomUserDetails userDetails = (CustomUserDetails) ((Authentication) principal).getPrincipal();

        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "ROLE_ADMIN";
        }

        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            return "ROLE_MANAGER";
        }

        return "ROLE_CUSTOMER"; // پیش‌فرض
    }


    private FileType mapContentTypeToFileType(String contentType) {
        if (contentType == null) return null;
        switch (contentType.toLowerCase()) {
            case "image/jpeg":
                return FileType.JPG;
            case "image/png":
                return FileType.PNG;
            case "image/bmp":
                return FileType.BMP;
            case "application/pdf":
                return FileType.PDF;
            case "text/plain":
                return FileType.TXT;
            default:
                return null;
        }
    }
}
