package ir.service;

import ir.model.entity.Attachment;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface AttachmentService {
    void save(Attachment attachment);
    void update(Attachment attachment);
    void delete(Long id);
    List<Attachment> findAll();
    Attachment findById(Long id);
    List<Attachment> findByTicketId(Long id);
    List<Attachment> findByUserName(String username);
    List<Attachment> findByUserNameAndTicketId(String username, Long id);
    List<Attachment> findByAttachTimeOrderByAttachTimeDesc(LocalDateTime attachTime);
}
