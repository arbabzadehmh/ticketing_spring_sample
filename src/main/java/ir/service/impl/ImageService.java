package ir.service.impl;

import ir.dto.ImageView;
import ir.model.entity.Attachment;
import ir.model.entity.ImageEntity;
import ir.repository.ImageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final FileStorageService fileStorageService;
    private final OcrService ocrService;
    private final ImageRepository imageRepository;

    public ImageService(FileStorageService fileStorageService, OcrService ocrService, ImageRepository imageRepository) {
        this.fileStorageService = fileStorageService;
        this.ocrService = ocrService;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public ImageEntity saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("فایلی انتخاب نشده است");
        }

        String storageId = fileStorageService.store(file, "anonymous");

        ImageEntity imageEntity = ImageEntity.builder()
                .id(storageId)
                .storageId(storageId)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .extractedText(null)
                .build();
        imageRepository.save(imageEntity);

        ocrService.extractTextForAttachmentAsync(
                Attachment.builder().mongoFileId(storageId).build()
        );

        return imageEntity;
    }

    @Transactional(readOnly = true)
    public List<ImageView> getAllImagesWithBase64() throws IOException {
        return imageRepository.findAll().stream()
                .map(img -> {
                    try {
                        GridFsResource res = fileStorageService.getResource(img.getStorageId());
                        String base64 = null;
                        if (res != null) {
                            base64 = Base64.getEncoder().encodeToString(res.getInputStream().readAllBytes());
                        }
                        return ImageView.builder()
                                .id(img.getId())
                                .fileName(img.getFileName())
                                .contentType(img.getContentType())
                                .extractedText(img.getExtractedText())
                                .imageBase64(base64)
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read image " + img.getId(), e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GridFsResource getImageResource(String id) {
        GridFsResource res = fileStorageService.getResource(id);
        if (res == null) {
            throw new EntityNotFoundException();
        }
        return res;
    }

    @Transactional
    public void deleteImage(String id) {
        ImageEntity img = imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("تصویر یافت نشد"));

        fileStorageService.deleteById(id);
        imageRepository.delete(img);
    }

}
