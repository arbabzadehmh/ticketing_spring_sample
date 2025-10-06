package ir.controller.api;

import ir.model.entity.Message;
import ir.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/rest/messages")
public class MessageApi {

    private final MessageService messageService;

    public MessageApi(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/{ticketId}")
    public List<Message> getMessages(@PathVariable Long ticketId) {
        return messageService.findByTicketId(ticketId);
    }

    @PostMapping("/{ticketId}")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long ticketId,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Principal principal) {

        Message savedMessage = messageService.save(ticketId, content, principal, files);
        return ResponseEntity.ok(savedMessage);
    }

}
