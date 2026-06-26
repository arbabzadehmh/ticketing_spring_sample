package ir.repository;

import ir.model.entity.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OutboxRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private OutboxRepository repository;

    @Test
    void shouldFindUnpublishedEventsOrderedByCreatedAt() {

        OutboxEvent first =
                OutboxEvent
                        .builder()
                        .published(false)
                        .createdAt(
                                Instant.now().minus(2, ChronoUnit.HOURS)
                        )
                        .build();

        OutboxEvent second =
                OutboxEvent
                        .builder()
                        .published(false)
                        .createdAt(
                                Instant.now().minus(1, ChronoUnit.HOURS)
                        )
                        .build();

        OutboxEvent ignored =
                OutboxEvent
                        .builder()
                        .published(true)
                        .createdAt(
                                Instant.now().minus(3, ChronoUnit.HOURS)
                        )
                        .build();

        repository.save(first);
        repository.save(second);
        repository.save(ignored);

        List<OutboxEvent> result =
                repository.findByPublishedFalseOrderByCreatedAtAsc();

        assertEquals(2, result.size());

        assertEquals(
                first.getCreatedAt(),
                result.get(0).getCreatedAt()
        );

        assertEquals(
                second.getCreatedAt(),
                result.get(1).getCreatedAt()
        );
    }
}
