package ir.service;

import ir.model.entity.LowScoreTicketReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReportService {
    LowScoreTicketReport findById(Long id);
    Page<LowScoreTicketReport> findAll(Pageable pageable);
    List<LowScoreTicketReport> findAll();

}
