package ir.model.entity;

import ir.model.enums.TicketStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class TicketSpecifications {
    public static Specification<Ticket> build(LocalDate dateFrom,
                                              LocalDate dateTo,
                                              TicketStatus status,
                                              Integer scoreLessThan,
                                              String customerUsername,
                                              Long sectionId,
                                              String title) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateTime"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateTime"), dateTo.atTime(LocalTime.MAX)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (scoreLessThan != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("score"), scoreLessThan));
            }
            if (customerUsername != null && !customerUsername.isBlank()) {
                // join به customer برای فیلتر نام کاربری
                Join<Ticket, User> customerJoin = root.join("customer", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(customerJoin.get("username")), "%" + customerUsername.toLowerCase() + "%"));
            }
            if (sectionId != null) {
                Join<Ticket, Section> sectionJoin = root.join("section", JoinType.LEFT);
                predicates.add(cb.equal(sectionJoin.get("id"), sectionId));
            }
            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
