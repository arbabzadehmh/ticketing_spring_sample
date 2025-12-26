package ir.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.model.entity.OutboxEvent;
import ir.repository.OutboxRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.address-create-request}")
    private String requestTopic;

    public OutboxPublisher(OutboxRepository outboxRepo, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

//    @PostConstruct
//    public void testKafka() {
//        kafkaTemplate.send("test-topic", "hi 2").whenComplete((r, ex) -> {
//            if (ex != null) {
//                ex.printStackTrace();
//            } else {
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>KAFKA SEND OK: " + r.getRecordMetadata());
//            }
//        });
//    }


    @Scheduled(fixedDelayString = "${outbox.publisher.delay:5000}")
    @Transactional
    public void publish() {

//        System.out.println(">>>>>>>>>>>> requestTopic =" +  requestTopic);

        List<OutboxEvent> list = outboxRepo.findByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent e : list) {
            try {
                kafkaTemplate.send(requestTopic, e.getPayload()).get(); // sync send, or use callback
                e.setPublished(true);
                outboxRepo.save(e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
