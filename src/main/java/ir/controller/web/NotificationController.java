package ir.controller.web;

import ir.model.entity.Notification;
import ir.model.entity.User;
import ir.service.NotificationService;
import ir.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String showAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean fragment,
            Model model,
            Authentication auth
    ) {

        if (size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName());
        Page<Notification> notifications = notificationService.findByUser(user, pageable);


        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifications.getTotalPages());

        return fragment != null && fragment
                ? "fragments/notification-fragments/notifications-table :: notifications-table"
                : "notification";

    }

}
