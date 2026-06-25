package ir.controller;

import ir.config.SecurityConfig;
import ir.controller.api.MessageApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.model.entity.Message;
import ir.service.MessageService;
import ir.service.TicketService;
import ir.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageApi.class)
@Import(SecurityConfig.class)
public class MessageApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MessageService messageService;

    @MockitoBean
    TicketService ticketService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void shouldReturnMessages() throws Exception {

        Message message = new Message();

        Page<Message> page =
                new PageImpl<>(
                        List.of(message),
                        PageRequest.of(0, 10),
                        1
                );

        when(messageService.findByTicketId(
                eq(1L),
                eq(0),
                eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/rest/messages/1")
                        .param("page", "0")
                        .param("size", "10")
                        .secure(true)
                )
                .andExpect(status().isOk());

        verify(messageService)
                .findByTicketId(eq(1L), eq(0), eq(10));
    }

    @Test
    @WithMockUser
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

        Message message = new Message();

        Page<Message> page =
                new PageImpl<>(
                        List.of(message),
                        PageRequest.of(0, 10),
                        1
                );

        when(messageService.findByTicketId(
                anyLong(),
                anyInt(),
                anyInt()
        )).thenReturn(page);

        mockMvc.perform(get("/rest/messages/1")
                        .param("page", "0")
                        .param("size", "0")
                        .secure(true)
                )
                .andExpect(status().isOk());

        verify(messageService).findByTicketId(
                eq(1L),
                eq(0),
                eq(10)
        );

    }

    @Test
    @WithMockUser(username = "ali")
    void shouldSendMessage() throws Exception {

        Message message = new Message();
        message.setId(1L);

        MockMultipartFile file =
                new MockMultipartFile(
                        "files",
                        "test.txt",
                        "text/plain",
                        "hello".getBytes()
                );

        when(messageService.save(
                eq(1L),
                eq("hello"),
                any(Principal.class),
                anyList()
        )).thenReturn(message);

        mockMvc.perform(multipart("/rest/messages/1")
                        .file(file)
                        .param("content", "hello")
                        .with(csrf())
                        .secure(true)
                )
                .andExpect(status().isOk());

        verify(messageService).save(
                eq(1L),
                eq("hello"),
                any(),
                anyList()
        );
    }

    @Test
    @WithMockUser
    void shouldSendOcrMessage() throws Exception {

        Message message = new Message();
        message.setId(2L);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "image.png",
                        "image/png",
                        "fake-image".getBytes()
                );

        when(messageService.saveOcrMessage(
                anyLong(),
                any(),
                any()
        )).thenReturn(message);

        mockMvc.perform(multipart("/rest/messages/ocr/1")
                        .file(file)
                        .with(csrf())
                        .secure(true)
                )
                .andExpect(status().isOk());

        verify(messageService).saveOcrMessage(
                eq(1L),
                any(),
                any()
        );
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldCloseTicket() throws Exception {

        when(messageSource.getMessage(
                eq("tickets.close.success"),
                isNull(),
                any(Locale.class)
        )).thenReturn("closed");

        mockMvc.perform(put("/rest/messages/ticket-close/1")
                        .with(csrf())
                        .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("closed"));

        verify(ticketService).closeTicket(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldScoreTicket() throws Exception {

        when(messageSource.getMessage(
                eq("tickets.score.success"),
                isNull(),
                any(Locale.class)
        )).thenReturn("scored");

        mockMvc.perform(put("/rest/messages/ticket-score/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content("5")
                        .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("scored"));

        verify(ticketService).scoreTicket(
                eq(1L),
                eq(5),
                any()
        );
    }

    @Test
    @WithMockUser
    void shouldMarkMessagesAsSeen() throws Exception {

        mockMvc.perform(put("/rest/messages/seen/1")
                        .with(csrf())
                        .secure(true)
                )
                .andExpect(status().isOk());

        verify(messageService).markMessagesAsSeen(
                eq(1L),
                any()
        );
    }
}
