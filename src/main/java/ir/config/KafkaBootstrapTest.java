package ir.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KafkaBootstrapTest implements CommandLineRunner {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    @Override
    public void run(String... args) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Kafka bootstrap-servers = " + bootstrap);
    }
}
