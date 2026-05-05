package ir.controller.web;

import ir.model.entity.LowScoreTicketReport;
import ir.model.entity.Ticket;
import ir.service.ReportService;
import ir.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/reports")
@PreAuthorize("hasAuthority('REPORT_VIEW')")
public class ReportController {

    private final ReportService reportService;
    private final TicketService ticketService;

    public ReportController(ReportService reportService, TicketService ticketService) {
        this.reportService = reportService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public String showReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean fragment,
            Model model
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LowScoreTicketReport> reports = reportService.findAll(pageable);

        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reports.getTotalPages());

        return fragment != null && fragment ?
                "fragments/report-fragments/reports-table :: reports-table" : "report";
    }

    @GetMapping("/low-score/{id}")
    @PreAuthorize("hasAuthority('TICKET_CHECK_SCORE')")
    public String showLowScoreReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean fragment,
            Model model
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("dateTime").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        LowScoreTicketReport report = reportService.findById(id);

        Page<Ticket> tickets = ticketService.findAllById(report.getTicketIds(), pageable);

        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tickets.getTotalPages());
        model.addAttribute("reportId", id);

        return fragment != null && fragment ?
                "fragments/report-fragments/reports-ticket-table :: reports-ticket-table" : "low-score-tickets-report";
    }

}
