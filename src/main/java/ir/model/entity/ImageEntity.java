package ir.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder

@Document(collection = "images")
public class ImageEntity {

    @Id
    private String id;

    private byte[] imageData;

    private String extractedText;
}
