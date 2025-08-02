package ir.repository;

import ir.model.entity.Attachment;
import ir.model.entity.Ticket;
import ir.model.entity.User;
import ir.model.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByFileNameIsLikeOrderByAttachTime(String title);
    List<Attachment> findByAttachTimeOrderByAttachTimeDesc(LocalDateTime attachTime); ;
    List<Attachment> findByFileType(FileType fileType);
    List<Attachment> findAllByOrderByAttachTimeDesc();
    List<Attachment> findByUserOrderByAttachTime(User user);
    List<Attachment> findByUserUsernameOrderByAttachTime(String username);
    List<Attachment> findByTicketIdOrderByAttachTime(Long ticketId);
    List<Attachment> findByTicketOrderByAttachTime(Ticket ticket);
    List<Attachment> findByUser_UsernameAndTicket_IdOrderByAttachTime(String username, Long ticketId);


}
