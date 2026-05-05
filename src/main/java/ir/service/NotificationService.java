package ir.service;

import ir.model.entity.Notification;
import ir.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    List<Notification> findAll();
    Page<Notification> findAll(Pageable pageable);
    Page<Notification> findByUser(User user, Pageable pageable);
    List<Notification> getUnread(User user);
    long unreadCount(User user);
    void markAsRead(Long id);
    void notify(User user, String title, String message, String link);
}
