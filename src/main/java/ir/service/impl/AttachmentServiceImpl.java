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
//        if (!Objects.equals(attachment.getTicket().getStatus().toString(), "closed")){
//            attachmentRepository.save(attachment);
//        };
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
    public List<Attachment> findAll() {
        return attachmentRepository.findAllByOrderByAttachTimeDesc();
    }

    @Override
    public Attachment findById(Long id) {
        return attachmentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Attachment> findByTicketId(Long id) {
        return attachmentRepository.findByTicketIdOrderByAttachTime(id);
    }

    @Override
    public List<Attachment> findByUserName(String username) {
        return attachmentRepository.findByUserUsernameOrderByAttachTime(username);
    }

    @Override
    public List<Attachment> findByUserNameAndTicketId(String username, Long id) {
        return attachmentRepository.findByUser_UsernameAndTicket_IdOrderByAttachTime(username, id);
    }


    @Override
    public List<Attachment> findByAttachTimeOrderByAttachTimeDesc(LocalDateTime attachTime) {
        return attachmentRepository.findByAttachTimeOrderByAttachTimeDesc(attachTime);
    }

}
