package ir.service.impl;

import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.data.mongodb.core.query.Query;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

    @Mock
    private GridFsTemplate gridFsTemplate;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private FileStorageService fileStorageService;

    @Test
    void store_shouldThrowWhenFileIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.store(null, "admin")
        );
    }

    @Test
    void store_shouldThrowWhenFileIsEmpty() {

        when(file.isEmpty()).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.store(file, "admin")
        );
    }

    @Test
    void store_shouldThrowWhenExtensionIsInvalid() {

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("virus.exe");

        assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.store(file, "admin")
        );
    }

    @Test
    void store_shouldThrowWhenExtensionMissing() {

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("file");

        assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.store(file, "admin")
        );
    }

    @Test
    void store_shouldSavePdf() throws Exception {

        byte[] pdf =
                """
                %PDF-1.4
                test
                """
                        .getBytes();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("doc.pdf");

        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(pdf));

        ObjectId id = new ObjectId();

        when(gridFsTemplate.store(
                any(InputStream.class),
                eq("doc.pdf"),
                eq("application/pdf"),
                any(DBObject.class)
        )).thenReturn(id);


        String result =
                fileStorageService.store(file, "admin");


        assertEquals(id.toHexString(), result);
    }

    @Test
    void store_shouldRejectInvalidContentType() throws Exception {

        byte[] data = "hello".getBytes();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");

        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(data));

        assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.store(file, "admin")
        );
    }

    @Test
    void getResource_shouldReturnNullWhenIdIsNull() {

        GridFsResource result =
                fileStorageService.getResource(null);

        assertNull(result);
    }

    @Test
    void getResource_shouldReturnNullWhenFileNotFound() {

        when(gridFsTemplate.findOne(any()))
                .thenReturn(null);


        GridFsResource result =
                fileStorageService.getResource(
                        new ObjectId().toHexString()
                );


        assertNull(result);
    }

    @Test
    void getResource_shouldReturnResource() {

        GridFSFile gridFile = mock(GridFSFile.class);

        GridFsResource resource =
                mock(GridFsResource.class);


        when(gridFsTemplate.findOne(any()))
                .thenReturn(gridFile);

        when(gridFsTemplate.getResource(gridFile))
                .thenReturn(resource);


        GridFsResource result =
                fileStorageService.getResource(
                        new ObjectId().toHexString()
                );


        assertEquals(resource, result);
    }

    @Test
    void deleteById_shouldDoNothingWhenIdIsNull() {

        fileStorageService.deleteById(null);

        verify(gridFsTemplate, never())
                .delete(any());
    }

    @Test
    void deleteById_shouldDeleteFile() {

        String id = new ObjectId().toHexString();

        fileStorageService.deleteById(id);

        verify(gridFsTemplate)
                .delete(any(Query.class));
    }
}
