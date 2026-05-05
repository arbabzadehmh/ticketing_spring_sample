package ir.controller.api;

import ir.model.entity.Notification;
import ir.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/rest/notifications")
public class NotificationApi {

    private final NotificationService notificationService;

    public NotificationApi(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getAllNotificationsForTable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Notification> notifications = notificationService.findAll(pageable);

        return ResponseEntity.ok(notifications);

    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
}
