package ir.service;

import ir.model.entity.Attachment;

public interface AttachmentService {
    void save(Attachment attachment);
    void update(Attachment attachment);
    void delete(Long id);
    Attachment findById(Long id);
}
