package ir.service.impl;


import ir.model.entity.Attachment;
import ir.repository.AttachmentRepository;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

@Service
public class OcrService {

    private final FileStorageService fileStorageService;
    private final AttachmentRepository attachmentRepository;

    public OcrService(FileStorageService fileStorageService, AttachmentRepository attachmentRepository) {
        this.fileStorageService = fileStorageService;
        this.attachmentRepository = attachmentRepository;
    }

    @Value("${app.ocr.tessdata-path}")
    private String tessDataPath;

    @Async
    public CompletableFuture<Void> extractTextForAttachmentAsync(Attachment attachment) {
        try {
            GridFsResource res = fileStorageService.getResource(attachment.getMongoFileId());
            if (res == null) return CompletableFuture.completedFuture(null);

            BufferedImage image = ImageIO.read(res.getInputStream());
            if (image == null) return CompletableFuture.completedFuture(null); // not an image

            Tesseract t = new Tesseract();
            t.setDatapath(tessDataPath);
            t.setLanguage("fas"); // فارسی؛ مطمئن شو fas.traineddata وجود داره

            String text = t.doOCR(image);

            attachment.setExtractedText(text);
            attachmentRepository.save(attachment);
        } catch (Exception e) {
            // لاگ کن و ادامه بده
//            log.error("OCR failed for attachment {}", attachment.getId(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

}
