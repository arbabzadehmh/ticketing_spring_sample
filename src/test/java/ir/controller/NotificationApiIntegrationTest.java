package ir.controller;

import ir.config.SecurityConfig;
import ir.controller.api.NotificationApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.model.entity.Notification;
import ir.model.entity.User;
import ir.service.NotificationService;
import ir.service.UserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(NotificationApi.class)
@Import(SecurityConfig.class)
public class NotificationApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NotificationService notificationService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "ali")
    void shouldReturnNotifications() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Notification notification = new Notification();

        Page<Notification> page =
                new PageImpl<>(
                        List.of(notification),
                        PageRequest.of(0, 10),
                        1
                );

        when(userService.findByUsername("ali"))
                .thenReturn(user);

        when(notificationService.findByUser(
                eq(user),
                any(Pageable.class)))
                .thenReturn(page);

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(get("/rest/notifications")
                        .secure(true)
                )
                .andExpect(status().isOk());

        verify(userService)
                .findByUsername("ali");

        verify(notificationService)
                .findByUser(
                        eq(user),
                        any(Pageable.class)
                );
    }

    @Test
    @WithMockUser(username = "ali")
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

        User user = new User();
        user.setUsername("ali");

        when(userService.findByUsername("ali"))
                .thenReturn(user);

        when(notificationService.findByUser(
                eq(user),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        get("/rest/notifications")
                                .param("size", "0")
                                .secure(true)
                )
                .andDo(print());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(notificationService)
                .findByUser(
                        eq(user),
                        captor.capture()
                );

        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    @WithMockUser
    void shouldMarkNotificationAsRead() throws Exception {

        doNothing()
                .when(notificationService)
                .markAsRead(1L);

        mockMvc.perform(
                        post("/rest/notifications/1/read")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(notificationService)
                .markAsRead(1L);
    }
}
