package ir.service.impl;

import ir.controller.exception.*;
import ir.dto.SectionDto;
import ir.dto.SectionFilterDto;
import ir.dto.SectionListDto;
import ir.model.entity.Building;
import ir.model.entity.Section;
import ir.repository.BuildingRepository;
import ir.repository.SectionRepository;
import ir.service.SectionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class SectionServiceImpl implements SectionService {
    private final SectionRepository sectionRepository;
    private final BuildingRepository buildingRepository;


    @Transactional
    @CacheEvict(cacheNames = {"sections", "sectionsPageable", "sectionsFilter"}, allEntries = true)
    @Override
    public Section save(Section section) {
        // اگر هیچ سکشنی در دیتابیس نیست، سکشن اصلی را بساز
        if (sectionRepository.count() == 0) {
            Section mainSection = Section.builder()
                    .title("MAIN SECTION")
                    .parentSection(null)
                    .build();

            sectionRepository.save(mainSection);
            section.setParentSection(mainSection);
            section.setTitle(section.getTitle().toUpperCase());
            return sectionRepository.save(section);
        }

        // اگر سکشن بدون والد ارسال شده، خطا بده
        if (section.getParentSection() == null || section.getParentSection().getId() == null) {
            throw new SavingSectionWithNoParent();
        }

        // بررسی تکراری بودن عنوان
        if (sectionRepository.existsByTitle(section.getTitle())) {
            throw new DuplicateSectionException();
        }

        // پیدا کردن والد
        Section parentSection = sectionRepository.findById(section.getParentSection().getId())
                .orElseThrow(() -> new EntityNotFoundException("Parent section not found"));

        // اضافه کردن فرزند به والد (فقط برای هماهنگی در حافظه)
        parentSection.addChildSection(section);
        section.setParentSection(parentSection);
        section.setTitle(section.getTitle().toUpperCase());

        return sectionRepository.save(section); // فقط فرزند را ذخیره می‌کنیم بخاطر cascade
    }


    @Transactional
    @CacheEvict(cacheNames = {"sections", "sectionsPageable", "sectionsFilter"}, allEntries = true)
    @Override
    public Section update(Long id, Section section) {

        Section existingSection = sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));


        if (!Objects.equals(existingSection.getVersion(), section.getVersion())) {
            throw new OptimisticLockException();
        }


        if (sectionRepository.existsByTitle(section.getTitle())
                && !existingSection.getTitle().equals(section.getTitle())) {
            throw new DuplicateSectionException();
        }

        // بررسی تغییر والد
        Long newParentId = section.getParentSection() != null ? section.getParentSection().getId() : null;
        Long oldParentId = existingSection.getParentSection() != null ? existingSection.getParentSection().getId() : null;

        if (newParentId != null && newParentId.equals(existingSection.getId())) {
            throw new SectionAsOwnParentException();
        }

// جمع‌آوری همه بچه‌ها و زیر بچه‌ها
        Set<Long> childIds = new HashSet<>();
        collectChildIds(existingSection, childIds);

        if (newParentId != null && childIds.contains(newParentId)) {
            throw new DescendantsSectionsAsParent();
        }


        if ((newParentId != null && !newParentId.equals(oldParentId))
                || (newParentId == null && oldParentId != null)) {

            // حذف از پدر قبلی اگر وجود داشت
            if (existingSection.getParentSection() != null) {
                existingSection.getParentSection().getChildSectionList().remove(existingSection);
            }

            // ست کردن پدر جدید
            if (newParentId != null) {
                Section newParent = sectionRepository.findById(newParentId)
                        .orElseThrow(() -> new EntityNotFoundException("Parent section not found"));
                existingSection.setParentSection(newParent);
                newParent.addChildSection(existingSection); // فقط برای هماهنگی در حافظه
            } else {
                existingSection.setParentSection(null);
            }
        }

        existingSection.setTitle(section.getTitle().toUpperCase());

        return sectionRepository.save(existingSection);

    }


    @Transactional
    @CacheEvict(cacheNames = {"sections", "sectionsPageable", "sectionsFilter"}, allEntries = true)
    @Override
    public void deleteById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        // جلوگیری از حذف والد دارای فرزند
        if (section.getChildSectionList() != null && !section.getChildSectionList().isEmpty()) {
            throw new RemovingParentSectionException();
        }

        section.setDeleted(true);

        // حذف از لیست فرزندان والد
        if (section.getParentSection() != null) {
            section.getParentSection().getChildSectionList().remove(section);
        }

        Building building = section.getBuilding();  // نگه داشتن مرجع قبل از null کردن
        if (building != null) {
            building.getSectionList().remove(section);
            buildingRepository.save(building);
        }

        // قطع ارتباط Section با Building
        section.setBuilding(null);

        sectionRepository.save(section); // اگر والد ندارد، فقط خودش را ذخیره کن
    }

    @Transactional
    @CacheEvict(cacheNames = {"sections", "sectionsPageable", "sectionsFilter"}, allEntries = true)
    @Override
    public List<Section> saveAll(List<Section> sections) {
        return sectionRepository.saveAll(sections);
    }




    @Transactional(readOnly = true)
    @Cacheable(value = "sections")
    @Override
    public List<Section> findAll() {
        return sectionRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "sectionsPageable")
    @Override
    public Page<SectionListDto> findAll(Pageable pageable) {
        return sectionRepository.findAllDto(pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "sectionsFilter")
    @Override
    public List<SectionFilterDto> findAllForFilter() {
        return sectionRepository.findAllForFilter();
    }


    @Override
    public Section findById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
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
    public Page<SectionListDto> findByTitleContaining(String title, Pageable pageable) {
        return sectionRepository.findByTitleContainingDto(title, pageable);
    }

    @Override
    public Page<SectionListDto> findByParentSectionTitleContaining(String parentTitle, Pageable pageable) {
        return sectionRepository.findByParentSectionTitleContainingDto(parentTitle, pageable);
    }

    private void collectChildIds(Section section, Set<Long> ids) {
        for (Section child : section.getChildSectionList()) {
            ids.add(child.getId());
            collectChildIds(child, ids);
        }
    }


}
