package ir.service.impl;


import ir.model.entity.Attachment;
import ir.model.enums.TicketStatus;
import ir.repository.AttachmentRepository;
import ir.service.AttachmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }
    @Override
    public void save(Attachment attachment) {
        attachmentRepository.save(attachment);
    }

    @Override
    public void update(Attachment attachment) {
        attachmentRepository.save(attachment);
    }

    @Override
    public void delete(Long id) {
        attachmentRepository.deleteById(id);
    }


    @Override
    public Attachment findById(Long id) {
        return attachmentRepository.findById(id).orElse(null);
    }

}
