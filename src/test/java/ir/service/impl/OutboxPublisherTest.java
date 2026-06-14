package ir.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.model.entity.OutboxEvent;
import ir.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxPublisherTest {

    @Mock
    private OutboxRepository outboxRepo;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                publisher,
                "requestTopic",
                "address-topic"
        );
    }

    @Test
    void publish_shouldSendEventsAndMarkAsPublished() throws Exception {

        OutboxEvent event = new OutboxEvent();
        event.setPayload("{\"name\":\"test\"}");
        event.setPublished(false);

        when(outboxRepo.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(event));


        SendResult<String, String> sendResult = mock(SendResult.class);

        CompletableFuture<SendResult<String, String>> future =
                CompletableFuture.completedFuture(sendResult);


        when(kafkaTemplate.send(
                "address-topic",
                event.getPayload()
        )).thenReturn(future);


        publisher.publish();


        assertTrue(event.isPublished());

        verify(kafkaTemplate)
                .send("address-topic", event.getPayload());

        verify(outboxRepo)
                .save(event);
    }


    @Test
    void publish_shouldNotSaveWhenKafkaFails() {

        OutboxEvent event = new OutboxEvent();
        event.setPayload("data");
        event.setPublished(false);


        when(outboxRepo.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(event));


        CompletableFuture<SendResult<String, String>> future =
                new CompletableFuture<>();

        future.completeExceptionally(
                new RuntimeException("Kafka down")
        );


        when(kafkaTemplate.send(
                "address-topic",
                "data"
        )).thenReturn(future);


        publisher.publish();


        assertFalse(event.isPublished());

        verify(outboxRepo, never())
                .save(any(OutboxEvent.class));
    }


    @Test
    void publish_shouldDoNothingWhenNoEventExists() {

        when(outboxRepo.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of());


        publisher.publish();


        verifyNoInteractions(kafkaTemplate);

        verify(outboxRepo, never())
                .save(any());
    }
}
