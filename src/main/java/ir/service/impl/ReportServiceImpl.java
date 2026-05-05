package ir.service.impl;

import ir.model.entity.LowScoreTicketReport;
import ir.repository.LowScoreTicketReportRepository;
import ir.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public Page<LowScoreTicketReport> findAll(Pageable pageable) {
        return lowScoreTicketReportRepository.findAll(pageable);
    }

    @Override
    public List<LowScoreTicketReport> findAll() {
        return lowScoreTicketReportRepository.findAll();
    }
}
