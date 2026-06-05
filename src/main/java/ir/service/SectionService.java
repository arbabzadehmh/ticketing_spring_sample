package ir.service;

import ir.dto.SectionFilterDto;
import ir.dto.SectionListDto;
import ir.model.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SectionService {
    Section save(Section section);
    Section update(Long id,Section section);
    void deleteById(Long id);
    List<Section> findAll();
    Page<SectionListDto> findAll(Pageable pageable);
    List<SectionFilterDto> findAllForFilter();
    Section findById(Long id);
    List<Section> findSectionByTitle(String title);
    List<Section> findByParentSection(Section section);
    Page<SectionListDto> findByTitleContaining(String title, Pageable pageable);
    Page<SectionListDto> findByParentSectionTitleContaining(String parentTitle, Pageable pageable);
    List<Section> saveAll(List<Section> sections);

}
