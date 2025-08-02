package ir.repository;

import ir.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByTitleIsLike(String title);
    List<Section> findByParentSectionId(Long parentSectionId);
    List<Section> findByParentSection_TitleIsLike(String parentSectionTitle);
    Section findById(long id);


}
