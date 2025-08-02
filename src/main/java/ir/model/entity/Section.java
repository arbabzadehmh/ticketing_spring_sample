package ir.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;


import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name="sectionEntity")
@Table(name="section_table")
@Where(clause = "deleted = false")
public class Section extends Base {
    @Id
    @SequenceGenerator(name = "sectionSeq",sequenceName = "section_seq" , allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "sectionSeq")
    @Column(name="id")
    private Long id;

    @Column(name="title", length = 50, unique = true)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_section_id",nullable = true,foreignKey = @ForeignKey(name = "fk_section_parent_id"))
    private Section parentSection;

    @ToString.Exclude
    @OneToMany( fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "parentSection")
    private List<Section> childSectionList;

    public void addChildSection(Section childSection){
        if (childSectionList == null){
            childSectionList = new ArrayList<>();
        }
        childSectionList.add(childSection);
    }

}
