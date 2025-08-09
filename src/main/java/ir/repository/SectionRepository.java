package ir.repository;

import ir.model.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByTitleIsLike(String title);
    List<Section> findByParentSectionId(Long parentSectionId);
    List<Section> findByParentSection_TitleIsLike(String parentSectionTitle);
    Section findById(long id);
    List<Section> findAll();
    Page<Section> findAll(Pageable pageable);
    Optional<Section> findByTitle(String title);
    boolean existsByTitle(String title);
    Page<Section> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Section> findByParentSection_TitleContainingIgnoreCase(String parentTitle, Pageable pageable);

}
