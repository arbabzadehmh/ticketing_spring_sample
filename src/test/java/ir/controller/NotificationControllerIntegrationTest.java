package ir.controller;

import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.controller.web.NotificationController;
import ir.model.entity.Notification;
import ir.model.entity.User;
import ir.service.NotificationService;
import ir.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(NotificationController.class)
public class NotificationControllerIntegrationTest {

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

    @Test
    @WithMockUser(username = "ali")
    void shouldShowNotificationsPage() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Page<Notification> page =
                new PageImpl<>(
                        List.of(new Notification()),
                        PageRequest.of(0, 10),
                        1
                );

        when(userService.findByUsername("ali"))
                .thenReturn(user);

        when(notificationService.findByUser(
                eq(user),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/notifications")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("notification"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));

        verify(userService).findByUsername("ali");
        verify(notificationService)
                .findByUser(eq(user), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "ali")
    void shouldReturnNotificationFragment() throws Exception {

        User user = new User();
        user.setUsername("ali");

        when(userService.findByUsername("ali"))
                .thenReturn(user);

        when(notificationService.findByUser(
                eq(user),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(
                        get("/notifications")
                                .param("fragment", "true")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/notification-fragments/notifications-table :: notifications-table"
                ));
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

        mockMvc.perform(
                        get("/notifications")
                                .param("size", "0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(notificationService)
                .findByUser(
                        eq(user),
                        captor.capture()
                );

        assertEquals(10,
                captor.getValue().getPageSize());
    }
}
