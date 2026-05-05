package ir.repository;

import ir.model.entity.LowScoreTicketReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LowScoreTicketReportRepository extends JpaRepository<LowScoreTicketReport, Long> {
    Page<LowScoreTicketReport> findAll(Pageable pageable);
    List<LowScoreTicketReport> findAll();
}
