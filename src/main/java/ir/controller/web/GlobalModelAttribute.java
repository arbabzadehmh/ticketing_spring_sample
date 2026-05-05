package ir.controller.web;

import ir.model.entity.User;
import ir.service.NotificationService;
import ir.service.TicketService;
import ir.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    private final NotificationService notificationService;
    private final UserService userService;
    private final TicketService ticketService;

    public GlobalModelAttribute(NotificationService notificationService, UserService userService, TicketService ticketService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.ticketService = ticketService;
    }

    @ModelAttribute
    public void addNotifications(Model model, Authentication auth) {

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return;
        }

        User user = userService.findByUsername(auth.getName());

        model.addAttribute("notifications",
                notificationService.getUnread(user));

        model.addAttribute("notificationCount",
                notificationService.unreadCount(user));

        model.addAttribute("messageCount",
                ticketService.unreadTicketCount(user));

    }
}
