package ir.controller.api;

import ir.model.entity.LowScoreTicketReport;
import ir.model.entity.Ticket;
import ir.service.ReportService;
import ir.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/rest/reports")
@PreAuthorize("hasAuthority('REPORT_VIEW')")
public class ReportApi {

    private final ReportService reportService;
    private final TicketService ticketService;

    public ReportApi(ReportService reportService, TicketService ticketService) {
        this.reportService = reportService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<?> getAllReportsForTable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LowScoreTicketReport> reports = reportService.findAll(pageable);

        return ResponseEntity.ok(reports);
    }

    @GetMapping("/low-score/{id}")
    @PreAuthorize("hasAuthority('TICKET_CHECK_SCORE')")
    public ResponseEntity<?> getLowScoreReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("dateTime").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        LowScoreTicketReport report = reportService.findById(id);

        Page<Ticket> tickets = ticketService.findAllById(report.getTicketIds(), pageable);

        return ResponseEntity.ok(tickets);
    }
}
