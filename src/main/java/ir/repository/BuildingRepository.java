package ir.repository;

import ir.model.entity.Building;
import ir.model.entity.Role;
import ir.model.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    Page<Building> findAll(Pageable pageable);
    List<Building> findAll();
    Page<Building> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    boolean existsByTitle(String title);

    @Query(
            value = """
        SELECT DISTINCT b
        FROM buildingEntity b
        LEFT JOIN FETCH b.sectionList
        LEFT JOIN FETCH b.phoneNumbers
        """,
            countQuery = """
        SELECT COUNT(b)
        FROM buildingEntity b
        """
    )
    Page<Building> findAllWithRelations(Pageable pageable);
}
