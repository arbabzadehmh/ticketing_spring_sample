package ir.service.impl;


import ir.controller.exception.OcrException;
import ir.model.entity.Attachment;
import ir.repository.AttachmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
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
    public CompletableFuture<String> extractTextForAttachmentAsync(Attachment attachment) {
        try {
            GridFsResource res = fileStorageService.getResource(attachment.getMongoFileId());
            if (res == null) return CompletableFuture.completedFuture("");

            BufferedImage image = ImageIO.read(res.getInputStream());
            if (image == null) return CompletableFuture.completedFuture(""); // not an image

            Tesseract t = new Tesseract();
            t.setDatapath(tessDataPath);
            t.setLanguage("fas"); // فارسی؛ مطمئن شو fas.traineddata وجود داره

            String text = t.doOCR(image);

            attachment.setExtractedText(text);
            attachmentRepository.save(attachment);

            return CompletableFuture.completedFuture(text);

        } catch (Exception e) {
            throw new OcrException();
        }
    }

    @Async("ocrExecutor")
    @Transactional
    public void extractTextAsync(Long attachmentId) {
        try {

            Attachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));

            GridFsResource res = fileStorageService.getResource(attachment.getMongoFileId());
            if (res == null) {
                return;
            }

            BufferedImage image = ImageIO.read(res.getInputStream());
            if (image == null) {
                return; // not an image
            }

            Tesseract t = new Tesseract();
            t.setDatapath(tessDataPath);
            t.setLanguage("fas"); // فارسی

            String text = t.doOCR(image);

            attachment.setExtractedText(text);
            attachmentRepository.save(attachment);

        } catch (Exception e) {
            log.error("OCR FAILED attachmentId={}",
                    attachmentId, e);

        }
    }

}
