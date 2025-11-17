package ir.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder

@Document(collection = "images")
public class ImageEntity {

    @Id
    private String id;

    private String storageId;   // ID فایل در GridFS
    private String fileName;
    private String contentType;
    private long size;
    private String extractedText;
}
