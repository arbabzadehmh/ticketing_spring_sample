package ir.service;

import ir.model.entity.Notification;
import ir.model.entity.User;

import java.util.List;

public interface NotificationService {
    List<Notification> getUnread(User user);
    long unreadCount(User user);
    void markAsRead(Long id);
    void notify(User user, String title, String message, String link);
}
