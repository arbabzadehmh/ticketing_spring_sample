package ir.controller.web;

import ir.model.entity.Message;
import ir.service.MessageService;
import ir.service.TicketService;
import ir.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;
    private final TicketService ticketService;
    private final UserService userService;


    public MessageController(MessageService messageService, TicketService ticketService, UserService userService) {
        this.messageService = messageService;

        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping(path = "/tickets/messages/{ticketId}")
    public String showForm(Model model) {
        model.addAttribute("message", new Message());
//        model.addAttribute("messageList", messageService.findAll());
//        model.addAttribute("ticketList", ticketService.findAll());
//        model.addAttribute("userList", userService.findAll());
        return "message";
    }


    @PostMapping
    public String saveMessage(Message message, @ModelAttribute("ticketId") Long ticketId, Principal principal) {
        message.setTicket(ticketService.findById(ticketId));
        message.setUser(userService.findByUsername(principal.getName()));
        message.setDateTime(LocalDateTime.now());
        messageService.save(message);
        log.info("message Saved");
        return "redirect:/tickets/messages/" + ticketId;
    }

    @DeleteMapping(path = "/{messageId}")
    public String removeMessage(@PathVariable("messageId") long messageId) {
        Message message = messageService.findById(messageId);
        messageService.delete(messageId);
        log.info("Message Removed");

        return "redirect:/tickets/messages/" + message.getTicket().getId();
//        return "redirect:/tickets";
    }


}
