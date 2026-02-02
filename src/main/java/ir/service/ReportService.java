package ir.service;

import ir.model.entity.LowScoreTicketReport;

public interface ReportService {
    public LowScoreTicketReport findById(Long id);
}
