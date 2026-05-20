package ir.repository;

import ir.dto.SectionDto;
import ir.dto.SectionFilterDto;
import ir.dto.SectionListDto;
import ir.model.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByTitleIsLike(String title);
    List<Section> findByParentSectionId(Long parentSectionId);
    List<Section> findByParentSection_TitleIsLike(String parentSectionTitle);
    List<Section> findAll();

    @Query("""
SELECT new ir.dto.SectionDto(
    s.id,
    s.title,
    ps.id,
    ps.title,
    b.id,
    b.title,
    s.version
)
FROM sectionEntity s
LEFT JOIN s.parentSection ps
LEFT JOIN s.building b ON b.deleted = false
WHERE s.deleted = false
ORDER BY s.title ASC
""")
    List<SectionDto> findAllDto();



    @Query("""
select new ir.dto.SectionListDto(
    s.id,
    s.title,

    ps.id,
    ps.title,

    b.id,
    b.title,

    s.version
)
from sectionEntity s
left join s.parentSection ps
left join s.building b ON b.deleted = false
where s.deleted = false
""")
    Page<SectionListDto> findAllDto(Pageable pageable);

    @Query("SELECT new  ir.dto.SectionFilterDto(s.id, s.title) FROM sectionEntity s where s.deleted = false")
    List<SectionFilterDto> findAllForFilter();

    Optional<Section> findByTitle(String title);
    boolean existsByTitle(String title);

    @Query("""
SELECT new ir.dto.SectionListDto(
    s.id,
    s.title,
    ps.id,
    ps.title,
    b.id,
    b.title,
    s.version
)
FROM sectionEntity s
LEFT JOIN s.parentSection ps
LEFT JOIN s.building b ON b.deleted = false
WHERE s.deleted = false
AND LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%'))
""")
    Page<SectionListDto> findByTitleContainingDto(
            @Param("title") String title,
            Pageable pageable
    );


    @Query("""
SELECT new ir.dto.SectionListDto(
    s.id,
    s.title,
    ps.id,
    ps.title,
    b.id,
    b.title,
    s.version
)
FROM sectionEntity s
LEFT JOIN s.parentSection ps
LEFT JOIN s.building b ON b.deleted = false
WHERE s.deleted = false
AND LOWER(ps.title) LIKE LOWER(CONCAT('%', :parentTitle, '%'))
""")
    Page<SectionListDto> findByParentSectionTitleContainingDto(
            @Param("parentTitle") String parentTitle,
            Pageable pageable
    );



    Page<Section> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Section> findByParentSection_TitleContainingIgnoreCase(String parentTitle, Pageable pageable);
    List<Section> findAllById(Iterable<Long> ids);

}
