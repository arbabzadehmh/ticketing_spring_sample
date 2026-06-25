package ir.controller;


import ir.config.SecurityConfig;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.controller.web.ReportController;
import ir.model.entity.LowScoreTicketReport;
import ir.model.entity.Ticket;
import ir.model.entity.User;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
public class ReportControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReportService reportService;

    @MockitoBean
    TicketService ticketService;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW"
    })
    void shouldShowReportsPage() throws Exception {

        Page<LowScoreTicketReport> reports =
                new PageImpl<>(List.of(new LowScoreTicketReport()));

        when(reportService.findAll(any(Pageable.class)))
                .thenReturn(reports);

        mockMvc.perform(
                        get("/reports")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attributeExists("reports"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));

        verify(reportService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW"
    })
    void shouldReturnReportsFragment() throws Exception {

        Page<LowScoreTicketReport> reports =
                new PageImpl<>(List.of(new LowScoreTicketReport()));

        when(reportService.findAll(any(Pageable.class)))
                .thenReturn(reports);

        mockMvc.perform(
                        get("/reports")
                                .param("fragment", "true")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/report-fragments/reports-table :: reports-table"
                ));
    }

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMIN",
            "REPORT_VIEW"
    })
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

        Page<LowScoreTicketReport> reports =
                new PageImpl<>(List.of(new LowScoreTicketReport()));

        when(reportService.findAll(any(Pageable.class)))
                .thenReturn(reports);

        mockMvc.perform(
                        get("/reports")
                                .param("size", "0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(reportService)
                .findAll(captor.capture());

        assertEquals(
                10,
                captor.getValue().getPageSize()
        );
    }

    @Test
    @WithMockUser(
            authorities = {
                    "ROLE_ADMIN",
                    "REPORT_VIEW",
                    "TICKET_CHECK_SCORE"
            }
    )
    void shouldShowLowScoreReport() throws Exception {

        LowScoreTicketReport report =
                new LowScoreTicketReport();

        report.setTicketIds(List.of(1L, 2L));

        User user = new User();
        user.setUsername("ali_123");

        Ticket ticket = new Ticket();
        ticket.setCustomer(user);

        Page<Ticket> tickets =
                new PageImpl<>(List.of(ticket));

        when(reportService.findById(1L))
                .thenReturn(report);

        when(ticketService.findAllById(
                eq(List.of(1L, 2L)),
                any(Pageable.class)))
                .thenReturn(tickets);

        mockMvc.perform(
                        get("/reports/low-score/1")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("low-score-tickets-report"))
                .andExpect(model().attributeExists("tickets"))
                .andExpect(model().attribute("reportId", 1L));
    }

    @Test
    @WithMockUser(
            authorities = {
                    "ROLE_ADMIN",
                    "REPORT_VIEW",
                    "TICKET_CHECK_SCORE"
            }
    )
    void shouldReturnLowScoreReportFragment() throws Exception {

        LowScoreTicketReport report =
                new LowScoreTicketReport();

        report.setTicketIds(List.of(1L));

        User user = new User();
        user.setUsername("ali_123");

        Ticket ticket = new Ticket();
        ticket.setCustomer(user);

        Page<Ticket> tickets =
                new PageImpl<>(List.of(ticket));

        when(reportService.findById(1L))
                .thenReturn(report);

        when(ticketService.findAllById(
                anyList(),
                any(Pageable.class)))
                .thenReturn(tickets);

        mockMvc.perform(
                        get("/reports/low-score/1")
                                .param("fragment", "true")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/report-fragments/reports-ticket-table :: reports-ticket-table"
                ));
    }

    @Test
    @WithMockUser(
            authorities = {
                    "ROLE_ADMIN",
                    "REPORT_VIEW",
                    "TICKET_CHECK_SCORE"
            }
    )
    void shouldUseDefaultSizeForLowScoreReport() throws Exception {

        LowScoreTicketReport report =
                new LowScoreTicketReport();

        report.setTicketIds(List.of(1L));

        User user = new User();
        user.setUsername("ali_123");

        Ticket ticket = new Ticket();
        ticket.setCustomer(user);

        Page<Ticket> tickets =
                new PageImpl<>(List.of(ticket));

        when(reportService.findById(1L))
                .thenReturn(report);

        when(ticketService.findAllById(
                anyList(),
                any(Pageable.class)))
                .thenReturn(tickets);

        mockMvc.perform(
                        get("/reports/low-score/1")
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
    @WithMockUser
    void shouldReturnForbiddenWhenUserHasNoReportViewAuthority()
            throws Exception {

        mockMvc.perform(
                        get("/reports")
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "REPORT_VIEW")
    void shouldReturnForbiddenWhenUserHasNoTicketCheckScoreAuthority()
            throws Exception {

        mockMvc.perform(
                        get("/reports/low-score/1")
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }

}
