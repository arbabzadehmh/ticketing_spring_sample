package ir.controller.web;

import ir.model.entity.LowScoreTicketReport;
import ir.model.entity.Ticket;
import ir.service.ReportService;
import ir.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final TicketService ticketService;

    public ReportController(ReportService reportService, TicketService ticketService) {
        this.reportService = reportService;
        this.ticketService = ticketService;
    }

    @GetMapping("/low-score/{id}")
    public String showLowScoreReport(@PathVariable Long id, Model model) {

        LowScoreTicketReport report = reportService.findById(id);

        List<Ticket> tickets =
                ticketService.findAllById(report.getTicketIds());

        model.addAttribute("report", report);
        model.addAttribute("tickets", tickets);

        return "reports/low-score-report";
    }

}
