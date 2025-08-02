package ir;

import ir.model.entity.Role;
import ir.model.entity.User;
import ir.service.MessageService;
import ir.service.RoleService;
import ir.service.TicketService;
import ir.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;
import java.util.Set;


@Slf4j
@EnableJpaRepositories
@SpringBootApplication
public class TicketingSpringSample {
    private static RoleService roleService;
    private static UserService userService;
    private static TicketService ticketService;
    private static MessageService messageService;


    public TicketingSpringSample(RoleService roleService, UserService userService, TicketService ticketService, MessageService messageService) {
        TicketingSpringSample.roleService = roleService;
        TicketingSpringSample.userService = userService;
        TicketingSpringSample.ticketService = ticketService;
        TicketingSpringSample.messageService = messageService;
    }

    public static void main(String[] args) {
        SpringApplication.run(TicketingSpringSample.class, args);
        log.info("**************************************************************Spring Boot Application Started");

//        System.out.println("Static files root: " + new File("src/main/resources/static").getAbsolutePath());


//        Role adminRole =
//                Role
//                        .builder()
//                        .name("ROLE_ADMIN")
//                        .build();
//        roleService.save(adminRole);
//        log.info("Admin Role Saved");
//
//        User adminUser =
//                User
//                        .builder()
//                        .username("ali")
//                        .password("123456")
//                        .locked(false)
//                        .roleSet(Set.of(adminRole))
//                        .build();
//        userService.save(adminUser);
//        log.info("Admin User Saved");

//

//        Role adminRole = roleService.findByName("ROLE_ADMIN");
//
//        User userUser =
//                User
//                        .builder()
//                        .username("Reza")
//                        .password("123456")
//                        .locked(false)
//                        .roleSet(Set.of(adminRole))
//                        .build();
//        userService.save(userUser);
//        log.info("reza Saved");
//
//        Ticket ticket =
//                Ticket
//                        .builder()
//                        .title("My Ticket")
//                        .user(userUser)
//                        .dateTime(LocalDateTime.now())
//                        .status(TicketStatus.NotSeen)
//                        .build();
//        ticketService.save(ticket);
//        log.info("Ticket Saved");
//
//
//        Message message1 =
//                Message
//                        .builder()
//                        .content("darkhast 1")
//                        .dateTime(LocalDateTime.now())
//                        .user(userUser)
//                        .ticket(ticket)
//                        .build();
//        messageService.save(message1);
//        log.info("Message1 Saved");
//
//        ticket.addMessage(message1);
//        ticket.setStatus(TicketStatus.Seen);
//        ticketService.update(ticket);
//        log.info("Ticket Updated");
//
//        Message message2 =
//                Message
//                        .builder()
//                        .content("Pasokh 1")
//                        .dateTime(LocalDateTime.now())
//                        .user(adminUser)
//                        .ticket(ticket)
//                        .build();
//        messageService.save(message2);
//        log.info("Message2 Saved");
//
//        ticket.addMessage(message2);
//        ticket.setStatus(TicketStatus.Responsed);
//        ticketService.update(ticket);
//        log.info("Ticket Updated");
//
//
//        Message message3 =
//                Message
//                        .builder()
//                        .content("darkhast 2")
//                        .dateTime(LocalDateTime.now())
//                        .user(userUser)
//                        .ticket(ticket)
//                        .build();
//        messageService.update(message3);
//        log.info("Message3 Saved");
//
//        ticket.addMessage(message3);
//        ticketService.update(ticket);
//        log.info("Ticket Updated");
//
//        Message message4 =
//                Message
//                        .builder()
//                        .content("Pasokh 2")
//                        .dateTime(LocalDateTime.now())
//                        .user(adminUser)
//                        .ticket(ticket)
//                        .build();
//        messageService.save(message4);
//        log.info("Message4 Saved");
//
//        ticket.addMessage(message4);
//        ticket.setStatus(TicketStatus.Responsed);
//        ticketService.update(ticket);
//        log.info("Ticket Updated");
//        System.out.println("----------------------------------------------");
//
//        Ticket t1 = ticketService.findById(1L);
//
//        System.out.println(t1.getUser().getUsername() + " Create Ticket " + ticket.getTitle());
//
//        System.out.println("----------------------------------------------");
//
////        List<Message> messageList = messageService.findByTicketId(ticket.getId());
////        for (Message m1 : messageList) {
//
//        for (Message m1 : ticket.getMessageList()) {
//            if (m1.getUser().getRole().getRoleName().equals("ROLE_ADMIN")) {
//                System.out.printf("\t\t\t\t\t\t\t%s : %s%n", m1.getContent(), m1.getUser().getUsername());
//            } else {
//                System.out.printf("%10s : %s%n", m1.getUser().getUsername(), m1.getContent());
//            }
//        }
//        System.out.println("----------------------------------------------");
//

//        Role customerRole = Role.builder().name("ROLE_CUSTOMER").permissionSet(null).build();
//        roleService.save(customerRole);


    }
}
