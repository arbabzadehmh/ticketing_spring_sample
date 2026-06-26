package ir.repository;

import ir.model.entity.Building;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import static org.junit.jupiter.api.Assertions.*;

public class BuildingRepositoryTest extends BaseRepositoryTest {

    @Autowired
    BuildingRepository buildingRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void shouldExistByTitle() {

        Building building = Building.builder()
                .title("Building A")
                .build();

        buildingRepository.saveAndFlush(building);

        boolean exists =
                buildingRepository.existsByTitle("Building A");

        assertTrue(exists);
    }

    @Test
    void shouldExistByTitleAndIdNot() {

        Building building1 = buildingRepository.saveAndFlush(
                Building.builder()
                        .title("Building A")
                        .build()
        );

        Building building2 = buildingRepository.saveAndFlush(
                Building.builder()
                        .title("Building B")
                        .build()
        );

        boolean exists =
                buildingRepository.existsByTitleAndIdNot(
                        "Building A",
                        building2.getId()
                );

        assertTrue(exists);
    }

    @Test
    void shouldFindByTitleContainingIgnoreCase() {

        buildingRepository.saveAndFlush(
                Building.builder()
                        .title("Main Building")
                        .build()
        );

        Page<Building> result =
                buildingRepository.findByTitleContainingIgnoreCase(
                        "main",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }

}
