package ir.controller.web;

import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import ir.service.MessageService;
import ir.service.TicketService;
import ir.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final UserService userService;
    private final MessageService messageService;

    public TicketController(TicketService ticketService, UserService userService, MessageService messageService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @GetMapping(path = "/messages/{ticketId}")
    public String getMessages(Model model, @PathVariable("ticketId") Long ticketId) {
        model.addAttribute("message", new Message());
        model.addAttribute("messageList", messageService.findByTicketId(ticketId));
        model.addAttribute("userList", userService.findAll());
        model.addAttribute("ticketId", ticketId);
        return "message";
    }

    @GetMapping
    public String showAllTickets(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        if (user.getRoleSet().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))){
            model.addAttribute("tickets", ticketService.findAll());
        } else {
            model.addAttribute("tickets", ticketService.findByUser(user));
        }

        model.addAttribute("ticket", new Ticket());

        return "ticket";
    }

    @PostMapping
    public String saveTicket(Ticket ticketForm, Model model, @ModelAttribute("status") TicketStatus status, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            Ticket ticket = Ticket.builder()
                    .title(ticketForm.getTitle())
                    .status(status)
                    .dateTime(ticketForm.getDateTime())
                    .user(user)
                    .build();
            ticketService.save(ticket);
            log.info("Ticket Saved : " + ticket);
        }catch (Exception e) {
       log.error(e.getMessage());
        }
        return "redirect:/tickets";
    }

    @DeleteMapping(path = "/{id}")
    public String deleteTicket(@PathVariable ("id") Long id) {
        try {
            ticketService.delete(id);
            log.info("Ticket Removed : " + id);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "redirect:/tickets";
    }
}
