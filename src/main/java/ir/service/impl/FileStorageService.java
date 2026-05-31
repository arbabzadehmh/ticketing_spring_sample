package ir.service.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.tika.Tika;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

@Service
public class FileStorageService {

    private final GridFsTemplate gridFsTemplate;

    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "gif",
            "pdf"
    );

    public FileStorageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public String store(MultipartFile file, String uploader) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalName =
                sanitizeFileName(file.getOriginalFilename());

        validateExtension(originalName);

        String detectedType =
                tika.detect(file.getInputStream());

        if (detectedType == null ||
                !ALLOWED_CONTENT_TYPES.contains(detectedType)) {

            throw new IllegalArgumentException(
                    "Unsupported file type: " + detectedType
            );
        }

        // فقط برای تصاویر
        if (detectedType.startsWith("image/")) {

            BufferedImage image =
                    ImageIO.read(file.getInputStream());

            if (image == null) {
                throw new IllegalArgumentException(
                        "Invalid image file"
                );
            }

            int width = image.getWidth();
            int height = image.getHeight();

            if (width > 5000 || height > 5000) {

                throw new IllegalArgumentException(
                        "Image dimensions are too large"
                );
            }
        }

        DBObject meta = new BasicDBObject();
        meta.put("uploadedBy", uploader);
        meta.put("originalName", originalName);

        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(),
                originalName,
                detectedType,
                meta
        );

        return fileId.toHexString();
    }

    private String sanitizeFileName(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            return "unknown";
        }

        String cleaned = StringUtils.cleanPath(fileName);

        return cleaned.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void validateExtension(String fileName) {

        int dot = fileName.lastIndexOf('.');

        if (dot < 0) {
            throw new IllegalArgumentException(
                    "File extension missing"
            );
        }

        String ext = fileName.substring(dot + 1)
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext)) {

            throw new IllegalArgumentException(
                    "Unsupported extension: " + ext
            );
        }
    }

    public GridFsResource getResource(String storageId) {

        if (storageId == null) {
            return null;
        }

        GridFSFile file = gridFsTemplate.findOne(
                Query.query(
                        Criteria.where("_id")
                                .is(new ObjectId(storageId))
                )
        );

        if (file == null) {
            return null;
        }

        return gridFsTemplate.getResource(file);
    }

    public void deleteById(String storageId) {

        if (storageId == null) {
            return;
        }

        gridFsTemplate.delete(
                Query.query(
                        Criteria.where("_id")
                                .is(new ObjectId(storageId))
                )
        );
    }
}