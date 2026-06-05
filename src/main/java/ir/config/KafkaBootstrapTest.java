package ir.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaBootstrapTest implements CommandLineRunner {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    @Override
    public void run(String... args) {
        log.info(" ---------->  Kafka bootstrap-servers = " + bootstrap);
    }
}
