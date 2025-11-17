package ir.controller.web;

import ir.controller.exception.ValidationException;
import ir.dto.TicketCreateDto;
import ir.dto.TicketEditDto;
import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.TicketSpecifications;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import ir.repository.MessageRepository;
import ir.service.MessageService;
import ir.service.SectionService;
import ir.service.TicketService;
import ir.service.UserService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;




@Controller
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final MessageSource messageSource;
    private final SectionService sectionService;
    private final MessageRepository messageRepository;
    private final UserService userService;

    public TicketController(TicketService ticketService, MessageSource messageSource, SectionService sectionService, MessageRepository messageRepository, UserService userService) {
        this.ticketService = ticketService;
        this.messageSource = messageSource;
        this.sectionService = sectionService;
        this.messageRepository = messageRepository;
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
    public String showTicketMessages(@PathVariable Long ticketId, Model model) {

        Ticket ticket = ticketService.findById(ticketId);
        List<Message> messageList = messageRepository.findByTicketIdOrderByDateTime(ticketId);

        model.addAttribute("ticket", ticket);
        model.addAttribute("messages", messageList);
        return "message"; // صفحه کامل message.html
    }


//    @GetMapping
//    public String ticketsList(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
//            @RequestParam(required = false) TicketStatus status,
//            @RequestParam(required = false) Integer scoreLessThan,
//            @RequestParam(required = false) String customer,
//            @RequestParam(required = false) Long sectionId,
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) Boolean fragment,
//            Model model,
//            Authentication authentication
//            ) {
//
//        if (size <= 0) size = 10;
//
//        // ایجاد صفحه‌بندی با مرتب‌سازی
//        Sort sort = Sort.by("dateTime").ascending();
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        boolean isAdminOrManager = authentication.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
//
//        Specification<Ticket> spec = TicketSpecifications.build(
//                dateFrom, dateTo, status, scoreLessThan, customer, sectionId, title
//        );
//
//        Page<Ticket> tickets;
//
//        if (isAdminOrManager) {
//            tickets = ticketService.findAll(spec, pageable);
//        } else {
//            tickets = ticketService.findByCustomerUsername(spec, authentication.getName(), pageable);
//        }
//
//        model.addAttribute("tickets", tickets);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", tickets.getTotalPages());
//
//        model.addAttribute("sectionsForFilter", sectionService.findAllForFilter());
//        model.addAttribute("ticketStatuses", TicketStatus.values());
//
//        return fragment != null && fragment ?
//                "fragments/ticket-fragments/tickets-table :: tickets-table" :
//                "ticket";
//    }
//
//    @GetMapping("/{ticketId}")
//    public String showTicketMessages(@PathVariable Long ticketId, Model model) {
//
//        Ticket ticket = ticketService.findById(ticketId);
//        List<Message> messageList = messageRepository.findByTicketIdOrderByDateTime(ticketId);
//
//        model.addAttribute("ticket", ticket);
//        model.addAttribute("messages", messageList);
//        return "message"; // صفحه کامل message.html
//    }
//
//    @PostMapping
//    @PreAuthorize("hasAuthority('TICKET_CREATE')")
//    @ResponseBody
//    public ResponseEntity<?> saveTicket(
//            @Valid @RequestBody TicketCreateDto ticketDto,
//            BindingResult bindingResult,
//            Locale locale,
//            Principal principal
//    ){
//
//        if (bindingResult.hasErrors()) {
//            Map<String, String> errors = new HashMap<>();
//            bindingResult.getFieldErrors().forEach(error ->
//                    errors.put(error.getField(), error.getDefaultMessage())
//            );
//            throw new ValidationException(errors);
//        }
//
//        ticketDto.setCustomerUsername(principal.getName());
//
//        ticketService.save(ticketDto);
//
//        String message = messageSource.getMessage("tickets.create.success", null, locale);
//
//        return ResponseEntity.ok(Map.of("message", message));
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('TICKET_EDIT')")
//    @ResponseBody
//    public ResponseEntity<?> updateTicket(
//            @PathVariable Long id,
//            @RequestBody TicketEditDto ticketEditDto,
//            Locale locale
//    ){
//
//        ticketService.update(id, ticketEditDto);
//
//        String message = messageSource.getMessage("tickets.edit.success", null, locale);
//        return ResponseEntity.ok(Map.of("message", message));
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('TICKET_DELETE')")
//    @ResponseBody
//    public ResponseEntity<?> deleteTicket(@PathVariable Long id, Locale locale){
//        ticketService.deleteById(id);
//        String message = messageSource.getMessage("tickets.delete.success", null, locale);
//        return ResponseEntity.ok(Map.of("message", message));
//    }
}
