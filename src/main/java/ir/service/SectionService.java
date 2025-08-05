package ir.service;

import ir.model.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SectionService {
    Section save(Section section);
    Section update(Long id,Section section);
    void deleteById(Long id);
    List<Section> findAll();
    Page<Section> findAll(Pageable pageable);
//    Optional<List<Section>> findAllSections();
    Section findById(Long id);
    List<Section> findSectionByTitle(String title);
    List<Section> findByParentSection(Section section);


}
