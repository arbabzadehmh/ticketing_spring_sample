package ir;

import ir.model.entity.Permission;
import ir.model.entity.Role;
import ir.model.entity.User;
import ir.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.Set;


@Slf4j
@EnableJpaRepositories
@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class TicketingSpringSample {
    private static RoleService roleService;
    private static UserService userService;
    private static PermissionService permissionService;
    private static TicketService ticketService;
    private static MessageService messageService;



    public TicketingSpringSample(RoleService roleService, UserService userService, PermissionService permissionService, TicketService ticketService, MessageService messageService) {
        TicketingSpringSample.roleService = roleService;
        TicketingSpringSample.userService = userService;
        TicketingSpringSample.permissionService = permissionService;
        TicketingSpringSample.ticketService = ticketService;
        TicketingSpringSample.messageService = messageService;
    }

    public static void main(String[] args) {
        SpringApplication.run(TicketingSpringSample.class, args);
        log.info("**************************************************************Spring Boot Application Started");

    }
}
