package ir.service.impl;

import ir.model.entity.LowScoreTicketReport;
import ir.repository.LowScoreTicketReportRepository;
import ir.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {

    private final LowScoreTicketReportRepository lowScoreTicketReportRepository;

    public ReportServiceImpl(LowScoreTicketReportRepository lowScoreTicketReportRepository) {
        this.lowScoreTicketReportRepository = lowScoreTicketReportRepository;
    }

    @Override
    public LowScoreTicketReport findById(Long id) {
        return lowScoreTicketReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));
    }
}
