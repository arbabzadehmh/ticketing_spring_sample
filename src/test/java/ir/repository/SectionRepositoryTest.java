package ir.repository;

import ir.dto.SectionDto;
import ir.dto.SectionFilterDto;
import ir.dto.SectionListDto;
import ir.model.entity.Building;
import ir.model.entity.Section;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SectionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    SectionRepository repository;

    @Autowired
    BuildingRepository buildingRepository;


    @Test
    void shouldFindSectionByTitle() {

        Section section =
                Section
                        .builder()
                        .title("Finance")
                        .build();

        repository.save(section);

        Optional<Section> result =
                repository.findByTitle("Finance");

        assertTrue(result.isPresent());
        assertEquals(
                "Finance",
                result.get().getTitle()
        );
    }

    @Test
    void shouldCheckTitleExists() {

        repository.save(
                Section
                        .builder()
                        .title("Finance")
                        .build()
        );

        assertTrue(
                repository.existsByTitle("Finance")
        );
    }

    @Test
    void shouldFindByTitleLike() {

        repository.save(
                Section
                        .builder()
                        .title("Finance")
                        .build()
        );

        List<Section> result =
                repository.findByTitleIsLike("%nan%");

        assertEquals(1, result.size());
    }

    @Test
    void shouldFindByParentSectionId() {

        Section parent =
                repository.save(
                        Section
                                .builder()
                                .title("Parent")
                                .build()
                );

        repository.save(
                Section
                        .builder()
                        .title("Child")
                        .parentSection(parent)
                        .build()
        );

        List<Section> result =
                repository.findByParentSectionId(
                        parent.getId()
                );

        assertEquals(1, result.size());
    }

    @Test
    void shouldFindByParentTitleLike() {

        Section parent =
                repository.save(
                        Section
                                .builder()
                                .title("Parent")
                                .build()
                );

        repository.save(
                Section
                        .builder()
                        .title("Child")
                        .parentSection(parent)
                        .build()
        );

        List<Section> result =
                repository.findByParentSection_TitleIsLike(
                        "%Par%"
                );

        assertEquals(1, result.size());
    }

    @Test
    void shouldFindAllDto() {

        Building building =
                buildingRepository.save(
                        Building
                                .builder()
                                .title("Building A")
                                .build()
                );

        Section section =
                Section
                        .builder()
                        .title("Finance")
                        .building(building)
                        .build();

        repository.save(section);

        List<SectionDto> result =
                repository.findAllDto();

        assertFalse(result.isEmpty());

        assertEquals(
                "Finance",
                result.get(0).title()
        );
    }

    @Test
    void shouldFindAllDtoPaged() {

        repository.save(
                Section
                        .builder()
                        .title("Finance")
                        .build()
        );

        Page<SectionListDto> result =
                repository.findAllDto(
                        PageRequest.of(0, 10)
                );

        assertEquals(1,
                result.getTotalElements());
    }

    @Test
    void shouldFindAllForFilter() {

        repository.save(
                Section
                        .builder()
                        .title("Finance")
                        .build()
        );

        List<SectionFilterDto> result =
                repository.findAllForFilter();

        assertEquals(1, result.size());

        assertEquals(
                "Finance",
                result.get(0).title()
        );
    }

    @Test
    void shouldFindByTitleContainingDto() {

        repository.save(
                Section
                        .builder()
                        .title("Finance")
                        .build()
        );

        Page<SectionListDto> result =
                repository.findByTitleContainingDto(
                        "nan",
                        PageRequest.of(0, 10)
                );

        assertEquals(1,
                result.getTotalElements());
    }

    @Test
    void shouldFindByParentSectionTitleContainingDto() {

        Section parent =
                repository.save(
                        Section
                                .builder()
                                .title("Parent")
                                .build()
                );

        repository.save(
                Section.builder()
                        .title("Child")
                        .parentSection(parent)
                        .build()
        );

        Page<SectionListDto> result =
                repository.findByParentSectionTitleContainingDto(
                        "Par",
                        PageRequest.of(0, 10)
                );

        assertEquals(1,
                result.getTotalElements());
    }

    @Test
    void shouldFindByTitleContainingIgnoreCase() {

        repository.save(
                Section
                        .builder()
                        .title("Finance")
                        .build()
        );

        Page<Section> result =
                repository.findByTitleContainingIgnoreCase(
                        "FIN",
                        PageRequest.of(0, 10)
                );

        assertEquals(1,
                result.getTotalElements());
    }

    @Test
    void shouldFindByParentTitleContainingIgnoreCase() {

        Section parent =
                repository.save(
                        Section
                                .builder()
                                .title("Parent")
                                .build()
                );

        repository.save(
                Section
                        .builder()
                        .title("Child")
                        .parentSection(parent)
                        .build()
        );

        Page<Section> result =
                repository.findByParentSection_TitleContainingIgnoreCase(
                        "PAR",
                        PageRequest.of(0, 10)
                );

        assertEquals(1,
                result.getTotalElements());
    }
}
