package ir.controller.web;

import ir.model.entity.User;
import ir.service.NotificationService;
import ir.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    private final NotificationService notificationService;
    private final UserService userService;

    public GlobalModelAttribute(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @ModelAttribute
    public void addNotifications(Model model, Authentication auth) {

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByUsername(auth.getName());

            model.addAttribute("notifications",
                    notificationService.getUnread(user));

            model.addAttribute("notificationCount",
                    notificationService.unreadCount(user));
        }
    }
}
