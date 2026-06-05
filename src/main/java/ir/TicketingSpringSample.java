package ir;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@Slf4j
@EnableJpaRepositories
@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class TicketingSpringSample {


    public TicketingSpringSample() {

    }

    public static void main(String[] args) {
        SpringApplication.run(TicketingSpringSample.class, args);
        log.info(" ---------->  Spring Boot Application Started");

    }
}
