package ir.controller;


import ir.config.SecurityConfig;
import ir.controller.api.ReportApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.model.entity.LowScoreTicketReport;
import ir.model.entity.Ticket;
import ir.service.ReportService;
import ir.service.TicketService;
import ir.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(ReportApi.class)
@Import(SecurityConfig.class)
public class ReportApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReportService reportService;

    @MockitoBean
    TicketService ticketService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW"
    })
    void shouldGetReports() throws Exception {

        LowScoreTicketReport report = new LowScoreTicketReport();

        Page<LowScoreTicketReport> reports =
                new PageImpl<>(List.of(report),
                        PageRequest.of(0, 10),
                        1
                );

        when(reportService.findAll(any(Pageable.class)))
                .thenReturn(reports);

        mockMvc.perform(
                        get("/rest/reports")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(reportService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW"
    })
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

        LowScoreTicketReport report = new LowScoreTicketReport();

        Page<LowScoreTicketReport> reports =
                new PageImpl<>(List.of(report),
                        PageRequest.of(0, 10),
                        1
                );

        when(reportService.findAll(any(Pageable.class)))
                .thenReturn(reports);

        mockMvc.perform(
                        get("/rest/reports")
                                .param("size", "0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(reportService)
                .findAll(captor.capture());

        assertEquals(10,
                captor.getValue().getPageSize());
    }

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW"
    })
    void shouldPassPagingParameters() throws Exception {

        LowScoreTicketReport report = new LowScoreTicketReport();

        Page<LowScoreTicketReport> reports =
                new PageImpl<>(List.of(report),
                        PageRequest.of(0, 10),
                        1
                );

        when(reportService.findAll(any(Pageable.class)))
                .thenReturn(reports);

        mockMvc.perform(
                        get("/rest/reports")
                                .param("page", "2")
                                .param("size", "5")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(reportService)
                .findAll(captor.capture());

        assertEquals(2,
                captor.getValue().getPageNumber());

        assertEquals(5,
                captor.getValue().getPageSize());
    }

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW",
            "TICKET_CHECK_SCORE"
    })
    void shouldGetLowScoreReport() throws Exception {

        LowScoreTicketReport report =
                new LowScoreTicketReport();

        report.setTicketIds(
                List.of(1L, 2L)
        );

        Page<Ticket> tickets =
                new PageImpl<>(List.of(new Ticket()),
                        PageRequest.of(0, 10),
                        1
                );

        when(reportService.findById(1L))
                .thenReturn(report);

        when(ticketService.findAllById(
                eq(List.of(1L, 2L)),
                any(Pageable.class)
        )).thenReturn(tickets);

        mockMvc.perform(
                        get("/rest/reports/low-score/1")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(reportService)
                .findById(1L);

        verify(ticketService)
                .findAllById(
                        eq(List.of(1L, 2L)),
                        any(Pageable.class)
                );
    }

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW",
            "TICKET_CHECK_SCORE"
    })
    void shouldUseDefaultSizeForLowScoreReport() throws Exception {

        LowScoreTicketReport report =
                new LowScoreTicketReport();

        report.setTicketIds(List.of(1L));

        when(reportService.findById(1L))
                .thenReturn(report);

        when(ticketService.findAllById(
                anyList(),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(List.of(new Ticket()),
                        PageRequest.of(0, 10),
                        1)
        );

        mockMvc.perform(
                        get("/rest/reports/low-score/1")
                                .param("size", "0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(ticketService)
                .findAllById(
                        anyList(),
                        captor.capture()
                );

        assertEquals(
                10,
                captor.getValue().getPageSize()
        );
    }

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW"
    })
    void shouldReturn403WhenUserHasNoTicketCheckScorePermission() throws Exception {

        mockMvc.perform(
                        get("/rest/reports/low-score/1")
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }
}
