package ir.repository;

import ir.model.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
