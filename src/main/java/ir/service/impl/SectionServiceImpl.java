package ir.service.impl;

import ir.model.entity.Section;
import ir.repository.SectionRepository;
import ir.service.SectionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SectionServiceImpl implements SectionService {
    private final SectionRepository sectionRepository;

//    @PostConstruct
//    public void init() {
//        findAllSections();
//    }


    @Transactional
    @CacheEvict(cacheNames = "sections", allEntries = true)
    @Override
    public void addSection(Section section) {
        sectionRepository.save(section);

    }


    @Transactional
    @CacheEvict(cacheNames = "sections", allEntries = true)
    @Override
    public void updateSection(Section section) {
        sectionRepository.save(section);

    }


    @Transactional
    @CacheEvict(cacheNames = "sections", allEntries = true)
    @Override
    public void deleteSection(Section section) {
        if(section.getChildSectionList().isEmpty()) {
            sectionRepository.delete(section);
        }
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "sections")
    @Override
    public List<Section> findAllSections() {
        return sectionRepository.findAll();
    }

//    public Optional<List<Section>> findAllSections() {
//        return Optional.of(sectionRepository.findAll());
//    }

    @Override
    public Section findSectionById(Long id) {
        return sectionRepository.findById(id).orElse(null);
    }

    @Override
    public List<Section> findSectionByTitle(String title) {
        return sectionRepository.findByTitleIsLike(title);
    }

    @Override
    public List<Section> findByParentSection(Section section) {
        return sectionRepository.findByParentSectionId(section.getId());
    }

    @Override
    public List<Section> findSectionByTitleParentSection(String title) {
        return sectionRepository.findByParentSection_TitleIsLike(title);
    }


}
