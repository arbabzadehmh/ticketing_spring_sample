package ir.service.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {

    private final GridFsTemplate gridFsTemplate;

    public FileStorageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public String store(MultipartFile file, String uploader) throws IOException {
        DBObject meta = new BasicDBObject();
        meta.put("uploadedBy", uploader);
        meta.put("originalName", file.getOriginalFilename());

        ObjectId fileId = gridFsTemplate.store(file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                meta);
        return fileId.toHexString();
    }

    public GridFsResource getResource(String storageId) {
        if (storageId == null) return null;
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(storageId))));
        if (file == null) return null;
        return gridFsTemplate.getResource(file);
    }

    public void deleteById(String storageId) {
        if (storageId == null) return;
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(new ObjectId(storageId))));
    }
}
