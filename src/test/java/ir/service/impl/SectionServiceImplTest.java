package ir.service.impl;

import ir.controller.exception.*;
import ir.dto.SectionFilterDto;
import ir.dto.SectionListDto;
import ir.model.entity.Building;
import ir.model.entity.Section;
import ir.repository.BuildingRepository;
import ir.repository.SectionRepository;
import ir.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EntityLockService entityLockService;

    @InjectMocks
    private SectionServiceImpl sectionService;


    @Test
    void save_shouldCreateMainSectionWhenDatabaseIsEmpty() {

        Section section = new Section();
        section.setTitle("support");

        when(sectionRepository.count())
                .thenReturn(0L);

        when(sectionRepository.save(any(Section.class)))
                .thenAnswer(i -> i.getArgument(0));


        Section result = sectionService.save(section);


        assertEquals("SUPPORT", result.getTitle());

        assertNotNull(result.getParentSection());

        assertEquals(
                "MAIN SECTION",
                result.getParentSection().getTitle()
        );


        verify(sectionRepository, times(2))
                .save(any(Section.class));
    }


    @Test
    void save_shouldThrowWhenParentIsNull() {

        Section section = new Section();

        section.setTitle("support");

        when(sectionRepository.count())
                .thenReturn(5L);


        assertThrows(
                SavingSectionWithNoParent.class,
                () -> sectionService.save(section)
        );


        verify(sectionRepository, never())
                .save(any());
    }


    @Test
    void save_shouldThrowWhenParentIdIsNull() {

        Section section = new Section();

        Section parent = new Section();

        section.setParentSection(parent);


        when(sectionRepository.count())
                .thenReturn(2L);


        assertThrows(
                SavingSectionWithNoParent.class,
                () -> sectionService.save(section)
        );
    }


    @Test
    void save_shouldThrowWhenTitleAlreadyExists() {

        Section section = new Section();

        section.setTitle("Support");

        Section parent = new Section();

        parent.setId(1L);

        section.setParentSection(parent);


        when(sectionRepository.count())
                .thenReturn(2L);

        when(sectionRepository.existsByTitle("Support"))
                .thenReturn(true);


        assertThrows(
                DuplicateSectionException.class,
                () -> sectionService.save(section)
        );


        verify(sectionRepository, never())
                .save(any());
    }


    @Test
    void save_shouldThrowWhenParentNotFound() {

        Section section = new Section();

        section.setTitle("Support");

        Section parent = new Section();

        parent.setId(10L);

        section.setParentSection(parent);


        when(sectionRepository.count())
                .thenReturn(2L);

        when(sectionRepository.existsByTitle("Support"))
                .thenReturn(false);

        when(sectionRepository.findById(10L))
                .thenReturn(Optional.empty());


        assertThrows(
                EntityNotFoundException.class,
                () -> sectionService.save(section)
        );
    }


    @Test
    void save_shouldSaveChildSection() {

        Section parent = new Section();

        parent.setId(1L);
        parent.setTitle("MAIN");


        Section child = new Section();

        child.setTitle("technical");
        child.setParentSection(parent);


        when(sectionRepository.count())
                .thenReturn(5L);

        when(sectionRepository.existsByTitle("technical"))
                .thenReturn(false);

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(parent));

        when(sectionRepository.save(any(Section.class)))
                .thenAnswer(i -> i.getArgument(0));


        Section result = sectionService.save(child);


        assertEquals(
                "TECHNICAL",
                result.getTitle()
        );

        assertEquals(
                parent,
                result.getParentSection()
        );


        verify(sectionRepository)
                .save(child);
    }

    @Test
    void update_shouldThrowWhenSectionNotFound() {

        Section input = new Section();

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> sectionService.update(1L, input)
        );

        verify(sectionRepository, never())
                .save(any());
    }

    @Test
    void update_shouldThrowOptimisticLockException() {

        Section existing = new Section();

        existing.setVersion(1L);

        Section input = new Section();

        input.setVersion(2L);


        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(existing));


        assertThrows(
                OptimisticLockException.class,
                () -> sectionService.update(1L, input)
        );


        verify(sectionRepository, never())
                .save(any());
    }

    @Test
    void update_shouldThrowDuplicateSectionException() {

        Section existing = new Section();

        existing.setVersion(1L);
        existing.setTitle("SUPPORT");


        Section input = new Section();

        input.setVersion(1L);
        input.setTitle("TECHNICAL");


        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(sectionRepository.existsByTitle("TECHNICAL"))
                .thenReturn(true);


        assertThrows(
                DuplicateSectionException.class,
                () -> sectionService.update(1L, input)
        );


        verify(sectionRepository, never())
                .save(any());
    }

    @Test
    void update_shouldThrowWhenSectionIsOwnParent() {

        Section existing = new Section();

        existing.setId(1L);
        existing.setVersion(1L);
        existing.setChildSectionList(new ArrayList<>());


        Section input = new Section();

        input.setVersion(1L);

        Section parent = new Section();

        parent.setId(1L);

        input.setParentSection(parent);


        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(existing));


        assertThrows(
                SectionAsOwnParentException.class,
                () -> sectionService.update(1L, input)
        );
    }

    @Test
    void update_shouldThrowWhenDescendantSelectedAsParent() {

        Section child = new Section();

        child.setId(2L);
        child.setChildSectionList(new ArrayList<>());


        Section existing = new Section();

        existing.setId(1L);
        existing.setVersion(1L);
        existing.setChildSectionList(
                List.of(child)
        );


        Section input = new Section();

        input.setVersion(1L);

        Section newParent = new Section();

        newParent.setId(2L);

        input.setParentSection(newParent);


        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(existing));


        assertThrows(
                DescendantsSectionsAsParent.class,
                () -> sectionService.update(1L, input)
        );
    }

    @Test
    void update_shouldUpdateTitle() {

        Section parent = new Section();
        parent.setId(10L);

        Section existing = new Section();
        existing.setId(1L);
        existing.setTitle("OLD");
        existing.setVersion(1L);
        existing.setParentSection(parent);
        existing.setChildSectionList(new ArrayList<>());

        Section input = new Section();
        input.setTitle("support");
        input.setVersion(1L);
        input.setParentSection(parent);


        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(sectionRepository.existsByTitle("support"))
                .thenReturn(false);

        when(sectionRepository.save(existing))
                .thenReturn(existing);


        Section result =
                sectionService.update(1L, input);


        assertEquals("SUPPORT", result.getTitle());

        verify(sectionRepository)
                .save(existing);

        verify(ticketRepository)
                .updateSectionTitleBySectionId(
                        1L,
                        "SUPPORT"
                );
    }

    @Test
    void update_shouldChangeParent() {

        Section oldParent = new Section();
        oldParent.setId(10L);
        oldParent.setChildSectionList(new ArrayList<>());


        Section newParent = new Section();
        newParent.setId(20L);
        newParent.setChildSectionList(new ArrayList<>());


        Section existing = new Section();
        existing.setId(1L);
        existing.setTitle("OLD");
        existing.setVersion(1L);
        existing.setParentSection(oldParent);
        existing.setChildSectionList(new ArrayList<>());


        oldParent.getChildSectionList().add(existing);


        Section input = new Section();
        input.setTitle("NEW");
        input.setVersion(1L);
        input.setParentSection(newParent);


        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(sectionRepository.findById(20L))
                .thenReturn(Optional.of(newParent));

        when(sectionRepository.existsByTitle("NEW"))
                .thenReturn(false);

        when(sectionRepository.save(existing))
                .thenReturn(existing);


        Section result =
                sectionService.update(1L, input);


        assertEquals(newParent, result.getParentSection());

        assertFalse(
                oldParent.getChildSectionList()
                        .contains(existing)
        );

        assertTrue(
                newParent.getChildSectionList()
                        .contains(existing)
        );

        verify(ticketRepository)
                .updateSectionTitleBySectionId(
                        1L,
                        "NEW"
                );
    }

    @Test
    void update_shouldThrowWhenNewParentNotFound() {

        Section existing = new Section();
        existing.setId(1L);
        existing.setVersion(1L);
        existing.setChildSectionList(new ArrayList<>());


        Section newParent = new Section();
        newParent.setId(20L);


        Section input = new Section();
        input.setVersion(1L);
        input.setTitle("NEW");
        input.setParentSection(newParent);


        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(sectionRepository.existsByTitle("NEW"))
                .thenReturn(false);

        when(sectionRepository.findById(20L))
                .thenReturn(Optional.empty());


        assertThrows(
                EntityNotFoundException.class,
                () -> sectionService.update(1L, input)
        );


        verify(sectionRepository, never())
                .save(any());
    }

    @Test
    void deleteById_shouldThrowWhenSectionIsLocked() {

        when(entityLockService.getLockOwner("section", 1L))
                .thenReturn("user1");


        assertThrows(
                EntityLockedException.class,
                () -> sectionService.deleteById(1L)
        );


        verify(sectionRepository, never())
                .findById(anyLong());
    }

    @Test
    void deleteById_shouldThrowWhenSectionNotFound() {

        when(entityLockService.getLockOwner("section", 1L))
                .thenReturn(null);

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                EntityNotFoundException.class,
                () -> sectionService.deleteById(1L)
        );


        verify(sectionRepository, never())
                .save(any());
    }

    @Test
    void deleteById_shouldThrowWhenSectionHasChildren() {

        Section child = new Section();

        Section section = new Section();
        section.setChildSectionList(
                List.of(child)
        );


        when(entityLockService.getLockOwner("section", 1L))
                .thenReturn(null);

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(section));


        assertThrows(
                RemovingParentSectionException.class,
                () -> sectionService.deleteById(1L)
        );


        verify(sectionRepository, never())
                .save(any());

        verify(ticketRepository, never())
                .markTicketsSectionDeleted(anyLong());
    }

    @Test
    void deleteById_shouldDeleteSectionWithoutBuilding() {

        Section section = new Section();

        section.setChildSectionList(new ArrayList<>());


        when(entityLockService.getLockOwner("section", 1L))
                .thenReturn(null);

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(section));


        sectionService.deleteById(1L);


        assertTrue(section.isDeleted());

        assertNull(section.getBuilding());


        verify(ticketRepository)
                .markTicketsSectionDeleted(1L);

        verify(sectionRepository)
                .save(section);
    }

    @Test
    void deleteById_shouldRemoveSectionFromParent() {

        Section parent = new Section();

        parent.setChildSectionList(
                new ArrayList<>()
        );


        Section section = new Section();

        section.setParentSection(parent);
        section.setChildSectionList(
                new ArrayList<>()
        );


        parent.getChildSectionList()
                .add(section);


        when(entityLockService.getLockOwner("section", 1L))
                .thenReturn(null);

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(section));


        sectionService.deleteById(1L);


        assertFalse(
                parent.getChildSectionList()
                        .contains(section)
        );


        verify(sectionRepository)
                .save(section);
    }

    @Test
    void deleteById_shouldRemoveSectionFromBuilding() {

        Building building = new Building();

        building.setSectionList(
                new ArrayList<>()
        );


        Section section = new Section();

        section.setBuilding(building);
        section.setChildSectionList(
                new ArrayList<>()
        );


        building.getSectionList()
                .add(section);


        when(entityLockService.getLockOwner("section", 1L))
                .thenReturn(null);

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(section));


        sectionService.deleteById(1L);


        assertTrue(section.isDeleted());

        assertNull(section.getBuilding());

        assertFalse(
                building.getSectionList()
                        .contains(section)
        );


        verify(buildingRepository)
                .save(building);

        verify(sectionRepository)
                .save(section);
    }

    @Test
    void deleteById_shouldDeleteSectionSuccessfully() {

        Section section = new Section();
        section.setId(1L);
        section.setChildSectionList(new ArrayList<>());

        Building building = new Building();
        building.setSectionList(new ArrayList<>());

        section.setBuilding(building);
        building.getSectionList().add(section);

        when(entityLockService.getLockOwner("section", 1L))
                .thenReturn(null);

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(section));

        sectionService.deleteById(1L);

        assertTrue(section.isDeleted());
        assertNull(section.getBuilding());

        verify(ticketRepository)
                .markTicketsSectionDeleted(1L);

        verify(buildingRepository)
                .save(building);

        verify(sectionRepository)
                .save(section);
    }

    @Test
    void saveAll_shouldReturnSavedSections() {

        List<Section> sections = List.of(
                new Section(),
                new Section()
        );

        when(sectionRepository.saveAll(sections))
                .thenReturn(sections);

        List<Section> result =
                sectionService.saveAll(sections);

        assertEquals(2, result.size());

        verify(sectionRepository)
                .saveAll(sections);
    }

    @Test
    void findAll_shouldReturnSections() {

        List<Section> sections = List.of(
                new Section(),
                new Section()
        );

        when(sectionRepository.findAll())
                .thenReturn(sections);

        List<Section> result =
                sectionService.findAll();

        assertEquals(2, result.size());

        verify(sectionRepository)
                .findAll();
    }

    @Test
    void findAllPageable_shouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        SectionListDto dto = new SectionListDto(
                1L,
                "IT",
                10L,
                "MAIN",
                100L,
                "Building A",
                0L
        );

        Page<SectionListDto> page = new PageImpl<>(List.of(dto));

        when(sectionRepository.findAllDto(pageable))
                .thenReturn(page);

        Page<SectionListDto> result =
                sectionService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("IT", result.getContent().get(0).title());

        verify(sectionRepository)
                .findAllDto(pageable);
    }

    @Test
    void findAllForFilter_shouldReturnList() {

        List<SectionFilterDto> list =
                List.of(new SectionFilterDto(
                        1L,
                        "IT"
                ));

        when(sectionRepository.findAllForFilter())
                .thenReturn(list);

        List<SectionFilterDto> result =
                sectionService.findAllForFilter();

        assertEquals(1, result.size());

        verify(sectionRepository)
                .findAllForFilter();
    }

    @Test
    void findById_shouldReturnSection() {

        Section section = new Section();
        section.setId(1L);

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.of(section));

        Section result = sectionService.findById(1L);

        assertEquals(1L, result.getId());

        verify(sectionRepository)
                .findById(1L);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException() {

        when(sectionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> sectionService.findById(1L));

        verify(sectionRepository)
                .findById(1L);
    }

    @Test
    void findSectionByTitle_shouldReturnSections() {

        List<Section> sections = List.of(
                new Section(),
                new Section()
        );

        when(sectionRepository.findByTitleIsLike("IT"))
                .thenReturn(sections);

        List<Section> result =
                sectionService.findSectionByTitle("IT");

        assertEquals(2, result.size());

        verify(sectionRepository)
                .findByTitleIsLike("IT");
    }

    @Test
    void findByParentSection_shouldReturnChildren() {

        Section parent = new Section();
        parent.setId(10L);

        List<Section> children = List.of(
                new Section(),
                new Section()
        );

        when(sectionRepository.findByParentSectionId(10L))
                .thenReturn(children);

        List<Section> result =
                sectionService.findByParentSection(parent);

        assertEquals(2, result.size());

        verify(sectionRepository)
                .findByParentSectionId(10L);
    }

    @Test
    void findByTitleContaining_shouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 5);

        SectionListDto dto = new SectionListDto(
                1L,
                "IT",
                10L,
                "MAIN",
                100L,
                "Building A",
                0L
        );

        Page<SectionListDto> page =
                new PageImpl<>(List.of(dto));

        when(sectionRepository
                .findByTitleContainingDto("IT", pageable))
                .thenReturn(page);

        Page<SectionListDto> result =
                sectionService.findByTitleContaining("IT", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("IT", result.getContent().get(0).title());

        verify(sectionRepository)
                .findByTitleContainingDto("IT", pageable);
    }

    @Test
    void findByParentSectionTitleContaining_shouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 5);

        SectionListDto dto = new SectionListDto(
                1L,
                "IT",
                10L,
                "MAIN",
                100L,
                "Building A",
                0L
        );

        Page<SectionListDto> page =
                new PageImpl<>(List.of(dto));

        when(sectionRepository
                .findByParentSectionTitleContainingDto("MAIN", pageable))
                .thenReturn(page);

        Page<SectionListDto> result =
                sectionService.findByParentSectionTitleContaining(
                        "MAIN",
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("MAIN",
                result.getContent().get(0).parentSectionTitle());

        verify(sectionRepository)
                .findByParentSectionTitleContainingDto("MAIN", pageable);
    }
}
