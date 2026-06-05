package ir.controller.web;

import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import ir.repository.MessageRepository;
import ir.service.MessageService;
import ir.service.SectionService;
import ir.service.TicketService;
import ir.service.UserService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



@Controller
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final MessageSource messageSource;
    private final SectionService sectionService;
    private final MessageService messageService;
    private final UserService userService;

    public TicketController(TicketService ticketService, MessageSource messageSource, SectionService sectionService, MessageRepository messageRepository, MessageService messageService, UserService userService) {
        this.ticketService = ticketService;
        this.messageSource = messageSource;
        this.sectionService = sectionService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping
    public String showAllTickets(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Boolean fragment,
        Model model,
        Authentication authentication
    ){

        if (size <= 0) size = 10;

        Sort sort = Sort.by("dateTime").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean isAdminOrManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));


        Page<Ticket> tickets;

        if (isAdminOrManager) {
            tickets = ticketService.findAll(pageable);
        } else {
            User user = userService.findByUsername(authentication.getName());
            tickets = ticketService.findByCustomer(user, pageable);
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tickets.getTotalPages());

        model.addAttribute("sectionsForFilter", sectionService.findAllForFilter());
        model.addAttribute("ticketStatuses", TicketStatus.values());

        return fragment != null && fragment ?
                "fragments/ticket-fragments/tickets-table :: tickets-table" :
                "ticket";
    }


    @GetMapping("/{ticketId}")
    public String showTicketMessages(
            @PathVariable Long ticketId,
            Model model
    ) {

        Ticket ticket = ticketService.findById(ticketId);

        Page<Message> page = messageService.findByTicketId(ticketId, 0, 50);

        model.addAttribute("ticket", ticket);
        model.addAttribute("messages", page.getContent());

        return "message";
    }
}
