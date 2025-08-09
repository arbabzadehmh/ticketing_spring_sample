package ir.service.impl;

import ir.controller.exception.DuplicateSectionException;
import ir.controller.exception.RemovingParentSectionException;
import ir.controller.exception.SavingSectionWithNoParent;
import ir.model.entity.Section;
import ir.repository.SectionRepository;
import ir.service.SectionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @CacheEvict(cacheNames = {"sections", "sectionsPageable"}, allEntries = true)
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

        System.out.println(">>>>>>>>>>>>>>>>>>>>>> parent : " + parentSection);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + section);

        return sectionRepository.save(section); // فقط فرزند را ذخیره می‌کنیم بخاطر cascade
    }


    @Transactional
    @CacheEvict(cacheNames = {"sections", "sectionsPageable"}, allEntries = true)
    @Override
    public Section update(Long id, Section section) {
        Section existingSection = sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        if (sectionRepository.existsByTitle(section.getTitle())
                && !existingSection.getTitle().equals(section.getTitle())) {
            throw new DuplicateSectionException();
        }

        // بررسی تغییر والد
        Long newParentId = section.getParentSection() != null ? section.getParentSection().getId() : null;
        Long oldParentId = existingSection.getParentSection() != null ? existingSection.getParentSection().getId() : null;

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
    @CacheEvict(cacheNames = {"sections", "sectionsPageable"}, allEntries = true)
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

        sectionRepository.save(section); // اگر والد ندارد، فقط خودش را ذخیره کن
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
    public Page<Section> findAll(Pageable pageable) {
        return sectionRepository.findAll(pageable);
    }

//    public Optional<List<Section>> findAllSections() {
//        return Optional.of(sectionRepository.findAll());
//    }

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
    public Page<Section> findByTitleContaining(String title, Pageable pageable) {
        return sectionRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    @Override
    public Page<Section> findByParentSectionTitleContaining(String parentTitle, Pageable pageable) {
        return sectionRepository.findByParentSection_TitleContainingIgnoreCase(parentTitle, pageable);
    }

}
