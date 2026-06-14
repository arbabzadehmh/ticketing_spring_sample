package ir.service.impl;

import ir.model.entity.Notification;
import ir.model.entity.User;
import ir.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;


    @Test
    void findAll_shouldReturnNotifications() {

        List<Notification> notifications = List.of(
                new Notification(),
                new Notification()
        );

        when(notificationRepository.findAll())
                .thenReturn(notifications);

        List<Notification> result =
                notificationService.findAll();

        assertEquals(2, result.size());

        verify(notificationRepository)
                .findAll();
    }


    @Test
    void findAllPageable_shouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Notification> page =
                new PageImpl<>(List.of(new Notification()));

        when(notificationRepository.findAll(pageable))
                .thenReturn(page);

        Page<Notification> result =
                notificationService.findAll(pageable);

        assertEquals(1, result.getTotalElements());

        verify(notificationRepository)
                .findAll(pageable);
    }


    @Test
    void findByUser_shouldReturnUserNotifications() {

        User user = new User();

        Pageable pageable =
                PageRequest.of(0, 5);

        Page<Notification> page =
                new PageImpl<>(List.of(new Notification()));

        when(notificationRepository
                .findByUserOrderByCreatedAtDesc(user, pageable))
                .thenReturn(page);


        Page<Notification> result =
                notificationService.findByUser(user, pageable);


        assertEquals(1, result.getTotalElements());

        verify(notificationRepository)
                .findByUserOrderByCreatedAtDesc(user, pageable);
    }


    @Test
    void getUnread_shouldReturnUnreadNotifications() {

        User user = new User();

        List<Notification> list =
                List.of(new Notification());

        when(notificationRepository
                .findTop5ByUserAndIsReadFalseOrderByCreatedAtDesc(user))
                .thenReturn(list);


        List<Notification> result =
                notificationService.getUnread(user);


        assertEquals(1, result.size());

        verify(notificationRepository)
                .findTop5ByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }


    @Test
    void unreadCount_shouldReturnCount() {

        User user = new User();

        when(notificationRepository
                .countByUserAndIsReadFalse(user))
                .thenReturn(3L);


        long result =
                notificationService.unreadCount(user);


        assertEquals(3, result);

        verify(notificationRepository)
                .countByUserAndIsReadFalse(user);
    }


    @Test
    void markAsRead_shouldUpdateNotification() {

        Notification notification =
                new Notification();

        notification.setRead(false);


        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));


        notificationService.markAsRead(1L);


        assertTrue(notification.isRead());

        verify(notificationRepository)
                .save(notification);
    }


    @Test
    void markAsRead_shouldThrowWhenNotFound() {

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                EntityNotFoundException.class,
                () -> notificationService.markAsRead(1L)
        );


        verify(notificationRepository, never())
                .save(any(Notification.class));
    }


    @Test
    void notify_shouldCreateAndSaveNotification() {

        User user = new User();


        notificationService.notify(
                user,
                "New Ticket",
                "Ticket created successfully",
                "/tickets/1"
        );


        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);


        verify(notificationRepository)
                .save(captor.capture());


        Notification saved =
                captor.getValue();


        assertEquals("New Ticket", saved.getTitle());

        assertEquals("Ticket created successfully", saved.getMessage());

        assertEquals("/tickets/1", saved.getLink());

        assertEquals(user, saved.getUser());

        assertNotNull(saved.getCreatedAt());
    }
}
