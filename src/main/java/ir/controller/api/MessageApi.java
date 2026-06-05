package ir.controller.api;

import ir.model.entity.Message;
import ir.service.MessageService;
import ir.service.TicketService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/rest/messages")
public class MessageApi {

    private final MessageService messageService;
    private final TicketService ticketService;
    private final MessageSource messageSource;

    public MessageApi(MessageService messageService, TicketService ticketService, MessageSource messageSource) {
        this.messageService = messageService;
        this.ticketService = ticketService;
        this.messageSource = messageSource;
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getMessages(
            @PathVariable Long ticketId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return ResponseEntity.ok(
                messageService.findByTicketId(ticketId, page, size)
        );
    }

    @PostMapping("/{ticketId}")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long ticketId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Principal principal) {

        Message savedMessage = messageService.save(ticketId, content, principal, files);
        return ResponseEntity.ok(savedMessage);
    }

    @PostMapping("/ocr/{ticketId}")
    public ResponseEntity<Message> sendOcrMessage(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        Message ocrMessage = messageService.saveOcrMessage(ticketId, principal, file);
        return ResponseEntity.ok(ocrMessage);
    }


    @PutMapping("/ticket-close/{ticketId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> closeTicket(
            @PathVariable Long ticketId,
            Locale locale
    ) {

        ticketService.closeTicket(ticketId);

        String message = messageSource.getMessage("tickets.close.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }


    @PutMapping("/ticket-score/{ticketId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> scoreTicket(
            @PathVariable Long ticketId,
            @RequestBody Integer score,
            Locale locale
    ) {

        ticketService.scoreTicket(ticketId, score);

        String message = messageSource.getMessage("tickets.score.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PutMapping("/seen/{ticketId}")
    public ResponseEntity<?> markSeen(
            @PathVariable Long ticketId,
            Principal principal
    ) {

        messageService.markMessagesAsSeen(ticketId, principal);

        return ResponseEntity.ok().build();
    }
}
