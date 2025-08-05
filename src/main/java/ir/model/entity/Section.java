package ir.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "{validation.section}")
    @Pattern(regexp = "^$|^[A-Za-z0-9_\\s-]{2,50}$", message = "{validation.sectionPattern}")
    @Column(name="title", length = 50, unique = true)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_section_id",nullable = true,foreignKey = @ForeignKey(name = "fk_section_parent_id"))
//    @ToString.Exclude
    private Section parentSection;

    @JsonIgnore
    @OneToMany( fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "parentSection")
    @ToString.Exclude
    private List<Section> childSectionList;

    public void addChildSection(Section childSection){
        if (childSectionList == null){
            childSectionList = new ArrayList<>();
        }
        childSectionList.add(childSection);
    }

}
