package ir.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

@Entity(name="buildingEntity")
@Table(name="building_table")
@Where(clause = "deleted = false")
public class Building extends Base{

    @Id
    @SequenceGenerator(name = "buildingSeq",sequenceName = "building_seq" , allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "buildingSeq")
    @Column(name="id")
    private Long id;

    @NotBlank(message = "{validation.building}")
    @Pattern(regexp = "^$|^[A-Za-z\\u0600-\\u06FF0-9_\\u06F0-\\u06F9\\s-]{2,50}$", message = "{validation.buildingPattern}")
    @Column(name="title", length = 50)
    private String title;

    @ElementCollection
    @CollectionTable(name = "building_phones", joinColumns = @JoinColumn(name = "building_id"))
    @Column(name = "phone")
    private List<String> phoneNumbers;


    @Column(name = "address_id")
    private Long addressId;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "building")
    @ToString.Exclude
    private List<Section> sectionList = new ArrayList<>();

    @Version
    @Column(name = "version")
    private Long version;

    public void addSection(Section section) {
        if (sectionList == null) sectionList = new ArrayList<>();
        sectionList.add(section);
        section.setBuilding(this);
    }

}
