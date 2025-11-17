package ir.controller.api;


import ir.dto.ImageView;
import ir.model.entity.Attachment;
import ir.model.entity.ImageEntity;
import ir.repository.ImageRepository;
import ir.service.impl.FileStorageService;
import ir.service.impl.ImageService;
import ir.service.impl.OcrService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.Base64;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/rest/images")
public class ImageApi {


    private final MessageSource messageSource;
    private final ImageService imageService;

    public ImageApi(MessageSource messageSource, ImageService imageService) {
        this.messageSource = messageSource;
        this.imageService = imageService;
    }

    @GetMapping
    public ResponseEntity<List<ImageView>> getAllImages() throws IOException {
        List<ImageView> views = imageService.getAllImagesWithBase64();
        return ResponseEntity.ok(views);
    }


    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, Locale locale) throws IOException, TesseractException {
        ImageEntity saved = imageService.saveImage(file);
        String message = messageSource.getMessage("image.upload.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));

//        return ResponseEntity.ok(Map.of("message", message, "id", saved.getId()));
    }


    @GetMapping("/{id}/raw")
    public ResponseEntity<Resource> streamImage(@PathVariable String id) throws IOException {
        GridFsResource resource = imageService.getImageResource(id);
        MediaType mt = MediaType.parseMediaType(resource.getContentType());
        return ResponseEntity.ok()
                .contentType(mt)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(new InputStreamResource(resource.getInputStream()));
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadImage(@PathVariable String id) throws IOException {
        GridFsResource resource = imageService.getImageResource(id);
        MediaType mt = MediaType.parseMediaType(resource.getContentType());
        return ResponseEntity.ok()
                .contentType(mt)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(new InputStreamResource(resource.getInputStream()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable String id, Locale locale) {
        imageService.deleteImage(id);

        String message = messageSource.getMessage("image.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }

}
