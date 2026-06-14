package ir.service.impl;

import ir.model.entity.LowScoreTicketReport;
import ir.repository.LowScoreTicketReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {

    @Mock
    private LowScoreTicketReportRepository lowScoreTicketReportRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void findById_shouldReturnReport() {

        LowScoreTicketReport report = new LowScoreTicketReport();
        report.setId(1L);

        when(lowScoreTicketReportRepository.findById(1L))
                .thenReturn(Optional.of(report));

        LowScoreTicketReport result =
                reportService.findById(1L);

        assertNotNull(result);

        assertEquals(1L, result.getId());

        verify(lowScoreTicketReportRepository)
                .findById(1L);
    }

    @Test
    void findById_shouldThrowWhenReportNotFound() {

        when(lowScoreTicketReportRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> reportService.findById(1L)
        );

        verify(lowScoreTicketReportRepository)
                .findById(1L);
    }

    @Test
    void findAll_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        LowScoreTicketReport report =
                new LowScoreTicketReport();

        Page<LowScoreTicketReport> page =
                new PageImpl<>(List.of(report));

        when(lowScoreTicketReportRepository.findAll(pageable))
                .thenReturn(page);

        Page<LowScoreTicketReport> result =
                reportService.findAll(pageable);

        assertEquals(1, result.getTotalElements());

        verify(lowScoreTicketReportRepository)
                .findAll(pageable);
    }

    @Test
    void findAll_shouldReturnList() {

        List<LowScoreTicketReport> reports =
                List.of(
                        new LowScoreTicketReport(),
                        new LowScoreTicketReport()
                );

        when(lowScoreTicketReportRepository.findAll())
                .thenReturn(reports);

        List<LowScoreTicketReport> result =
                reportService.findAll();

        assertEquals(2, result.size());

        verify(lowScoreTicketReportRepository)
                .findAll();
    }
}
