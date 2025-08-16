package ir.controller.web;

import ir.controller.exception.ValidationException;
import ir.dto.TicketCreateDto;
import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.entity.TicketSpecifications;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final MessageSource messageSource;
    private final UserService userService;
    private final MessageService messageService;
    private final SectionService sectionService;

    public TicketController(TicketService ticketService, MessageSource messageSource, UserService userService, MessageService messageService, SectionService sectionService) {
        this.ticketService = ticketService;
        this.messageSource = messageSource;
        this.userService = userService;
        this.messageService = messageService;
        this.sectionService = sectionService;
    }

    @GetMapping
    public String ticketsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Integer scoreLessThan,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean fragment,
            Model model
            ) {

        if (size <= 0) size = 10;

        // ایجاد صفحه‌بندی با مرتب‌سازی
        Sort sort = Sort.by("dateTime").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Ticket> spec = TicketSpecifications.build(
                dateFrom, dateTo, status, scoreLessThan, customer, sectionId, title
        );

        Page<Ticket> tickets = ticketService.findAll(spec, pageable);

        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tickets.getTotalPages());

        model.addAttribute("sectionsForFilter", sectionService.findAllForFilter());
        model.addAttribute("ticketStatuses", TicketStatus.values());

        return fragment != null && fragment ?
                "fragments/ticket-fragments/tickets-table :: tickets-table" :
                "ticket";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> saveTicket(
            @Valid @RequestBody TicketCreateDto ticketDto,
            BindingResult bindingResult,
            Locale locale,
            Principal principal
    ){

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            throw new ValidationException(errors);
        }

        ticketDto.setCustomerUsername(principal.getName());

        ticketService.save(ticketDto);

        String message = messageSource.getMessage("tickets.create.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTicket(@PathVariable Long id, Locale locale){
        ticketService.deleteById(id);
        String message = messageSource.getMessage("tickets.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
