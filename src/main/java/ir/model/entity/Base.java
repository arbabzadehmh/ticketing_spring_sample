package ir.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;



@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
@MappedSuperclass
public class Base implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @Column(name = "deleted")
    private boolean deleted = false;

}
