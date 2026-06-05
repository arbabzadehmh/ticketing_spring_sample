package ir.service.impl;

import ir.model.entity.LowScoreTicketReport;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.TicketStatus;
import ir.repository.LowScoreTicketReportRepository;
import ir.repository.TicketRepository;
import ir.repository.UserRepository;
import ir.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketSchedulerService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final LowScoreTicketReportRepository lowScoreTicketReportRepository;
    private final ProfileService profileService;

    public TicketSchedulerService(TicketRepository ticketRepository, UserRepository userRepository, NotificationService notificationService, EmailService emailService, LowScoreTicketReportRepository lowScoreTicketReportRepository, ProfileService profileService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.lowScoreTicketReportRepository = lowScoreTicketReportRepository;
        this.profileService = profileService;
    }


    @Scheduled(cron = "0 1 0 * * *") // هر روز ساعت 00:01
//    @Scheduled(fixedDelay = 60000) //هر یک دقیقه برای تست
    @SchedulerLock(
            name = "autoCloseOldTickets",
            lockAtMostFor = "15m",
            lockAtLeastFor = "1m"
    )
    @Transactional
    public void autoCloseOldTickets() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7); // 7 روز قبل
        LocalDate thresholdDate = LocalDate.now().minusDays(7);


        List<Ticket> ticketsToClose = ticketRepository.findTicketsToAutoClose(TicketStatus.Closed, threshold);

        if (!ticketsToClose.isEmpty()) {
            ticketsToClose.forEach(ticket -> ticket.setStatus(TicketStatus.Closed));

            List<User> admins = userRepository.findAdminsWithPermission("TICKET_CHECK_CLOSE");
            if (admins.isEmpty()) {
                log.info("No admins with permission TICKET_CHECK_CLOSE found.");
                return;
            }

            for (User admin : admins) {
                notificationService.notify(
                        admin,
                        "System Notification",

                       "Auto closed " + ticketsToClose.size() +" tickets older than " + thresholdDate,
                        "#"
                );
            }

            // چون داخل @Transactional هستیم، تغییرات خودکار سیو می‌شوند
            log.info("Auto closed " + ticketsToClose.size() +" tickets older than " + thresholdDate);
        } else {
            log.info("No tickets to auto-close today.");
        }
    }

    @Scheduled(cron = "0 0 9 * * MON") // هر دوشنبه ساعت 09:00 صبح
    @SchedulerLock(
            name = "sendLowScoreTicketsToAdmins",
            lockAtMostFor = "10m",
            lockAtLeastFor = "1m"
    )
    @Transactional
    public void sendLowScoreTicketsToAdmins() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusWeeks(1); // هفته گذشته

        List<Ticket> lowScoreTickets = ticketRepository.findTicketsWithLowScoreInPeriod(3, start, end);

        if (lowScoreTickets.isEmpty()) {
            log.info("No low-score tickets in the last week.");
            return;
        }

        LowScoreTicketReport report = lowScoreTicketReportRepository.save(
                LowScoreTicketReport.builder()
                        .createdAt(LocalDateTime.now())
                        .fromDate(start)
                        .toDate(end)
                        .ticketIds(
                                lowScoreTickets.stream()
                                        .map(Ticket::getId)
                                        .toList()
                        )
                        .build()
        );

        List<User> admins = userRepository.findAdminsWithPermission("TICKET_CHECK_SCORE");

        if (admins.isEmpty()) {
            log.info("No admins with permission TICKET_CHECK_SCORE found.");
            return;
        }

        // مثال ساده: ایمیل یا هر ارسال دیگر
        for (User admin : admins) {
            String subject = "Weekly Low Score Tickets Report";
            String body = "Tickets with score < 3 from " + start + " to " + end +  " :\n\n" +
                    lowScoreTickets.stream()
                            .map(t -> "ID: " + t.getId() + ", Title: " + t.getTitle() + ", Score: " + t.getScore())
                            .collect(Collectors.joining("\n"));

            emailService.sendEmail(profileService.getEmailByUsername(admin.getUsername()), subject, body);

            notificationService.notify(
                    admin,
                    "System Notification-Weekly Ticket Score Report",

                    "There are " + lowScoreTickets.size() + " tickets with score < 3",
                    "/reports/low-score/" + report.getId()
            );


        }

        log.info("Weekly low-score report sent to {} admins.", admins.size());
    }
}
