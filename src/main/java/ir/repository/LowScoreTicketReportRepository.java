package ir.repository;

import ir.model.entity.LowScoreTicketReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LowScoreTicketReportRepository extends JpaRepository<LowScoreTicketReport, Long> {
}
