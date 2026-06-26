package ir.repository;

import ir.model.entity.Notification;
import ir.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnTop5UnreadNotificationsOrderedByCreatedAtDesc() {

        User user =
                User
                        .builder()
                        .username("ali")
                        .password("123")
                        .build();

        userRepository.saveAndFlush(user);

        for (int i = 1; i <= 6; i++) {

            Notification notification =
                    Notification
                            .builder()
                            .user(user)
                            .isRead(false)
                            .createdAt(
                                    LocalDateTime.now().minusSeconds(100 - i)
                            )
                            .build();

            notificationRepository.save(notification);
        }

        List<Notification> result =
                notificationRepository
                        .findTop5ByUserAndIsReadFalseOrderByCreatedAtDesc(
                                user
                        );

        assertEquals(5, result.size());

        for (int i = 0; i < result.size() - 1; i++) {

            assertTrue(
                    result.get(i).getCreatedAt()
                            .isAfter(result.get(i + 1).getCreatedAt())
                            ||
                            result.get(i).getCreatedAt()
                                    .equals(result.get(i + 1).getCreatedAt())
            );
        }
    }

    @Test
    void shouldCountUnreadNotifications() {

        User user =
                User
                        .builder()
                        .username("ali")
                        .password("123")
                        .build();

        userRepository.saveAndFlush(user);

        notificationRepository.save(
                Notification
                        .builder()
                        .user(user)
                        .isRead(false)
                        .build()
        );

        notificationRepository.save(
                Notification
                        .builder()
                        .user(user)
                        .isRead(false)
                        .build()
        );

        notificationRepository.save(
                Notification
                        .builder()
                        .user(user)
                        .isRead(true)
                        .build()
        );

        long count =
                notificationRepository
                        .countByUserAndIsReadFalse(user);

        assertEquals(2, count);
    }

    @Test
    void shouldFindNotificationsByUserOrderedByCreatedAtDesc() {

        User user =
                User
                        .builder()
                        .username("ali")
                        .password("123")
                        .build();

        userRepository.saveAndFlush(user);

        Notification oldNotification =
                Notification
                        .builder()
                        .user(user)
                        .createdAt(
                                LocalDateTime.now().minusSeconds(100)
                        )
                        .build();

        Notification newNotification =
                Notification
                        .builder()
                        .user(user)
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        notificationRepository.save(oldNotification);
        notificationRepository.save(newNotification);

        Page<Notification> result =
                notificationRepository.findByUserOrderByCreatedAtDesc(
                        user,
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());

        assertEquals(
                newNotification.getId(),
                result.getContent().get(0).getId()
        );

        assertEquals(
                oldNotification.getId(),
                result.getContent().get(1).getId()
        );
    }
}
