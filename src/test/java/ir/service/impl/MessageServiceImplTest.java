package ir.service.impl;

import ir.controller.exception.FileStorageException;
import ir.controller.exception.TicketClosedException;
import ir.controller.exception.TicketExpiredException;
import ir.model.entity.Attachment;
import ir.model.entity.CustomUserDetails;
import ir.model.entity.Message;
import ir.model.entity.Ticket;
import ir.model.enums.TicketStatus;
import ir.repository.AttachmentRepository;
import ir.repository.MessageRepository;
import ir.repository.TicketRepository;
import ir.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private OcrService ocrService;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Principal createPrincipal(
            String roleName,
            String username
    ) {

        Authentication auth = mock(Authentication.class);

        CustomUserDetails user = mock(CustomUserDetails.class);

        when(auth.getPrincipal())
                .thenReturn(user);

        when(auth.getName())
                .thenReturn(username);

        doReturn(
                List.of(new SimpleGrantedAuthority(roleName))
        ).when(user).getAuthorities();

        return auth;
    }

    @Test
    void save_shouldThrowWhenTicketNotFound() {

        Principal principal = mock(Principal.class);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> messageService.save(1L, "hello", principal, null)
        );
    }

    @Test
    void save_shouldCloseExpiredTicketAndThrowException() {

        Ticket ticket = new Ticket();

        Principal principal = mock(Principal.class);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(messageRepository.findLastMessageTime(1L))
                .thenReturn(LocalDateTime.now().minusDays(8));

        assertThrows(
                TicketExpiredException.class,
                () -> messageService.save(1L, "hello", principal, null)
        );

        assertEquals(TicketStatus.Closed, ticket.getStatus());

        verify(ticketRepository).save(ticket);
    }

    @Test
    void save_shouldThrowWhenTicketClosed() {

        Ticket ticket = new Ticket();

        ticket.setStatus(TicketStatus.Closed);


        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(messageRepository.findLastMessageTime(1L))
                .thenReturn(null);


        assertThrows(
                TicketClosedException.class,
                () -> messageService.save(
                        1L,
                        "test",
                        createPrincipal(
                                "ROLE_CUSTOMER",
                                "customer"
                        ),
                        null
                )
        );
    }

    @Test
    void save_shouldCreateMessageWithoutFile() {

        Ticket ticket = new Ticket();

        ticket.setStatus(TicketStatus.WaitingForCustomer);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(messageRepository.findLastMessageTime(1L))
                .thenReturn(null);

        Message result = messageService.save(
                1L,
                "hello",
                createPrincipal(
                        "ROLE_CUSTOMER",
                        "customer"
                ),
                null
        );

        assertEquals("hello", result.getContent());

        assertEquals("customer", result.getSenderUsername());

        assertEquals("ROLE_CUSTOMER", result.getSenderRoleName());

        assertTrue(result.isSeenByCustomer());

        assertFalse(result.isSeenByAdmin());

        assertEquals(TicketStatus.WaitingForAdmin, ticket.getStatus());

        assertTrue(ticket.isAdminUnread());

        assertFalse(ticket.isCustomerUnread());

        verify(messageRepository)
                .save(any(Message.class));

        verify(ticketRepository)
                .save(ticket);
    }

    @Test
    void save_shouldUpdateTicketStatusForAdmin() {

        Ticket ticket = new Ticket();

        ticket.setStatus(TicketStatus.WaitingForAdmin);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(messageRepository.findLastMessageTime(1L))
                .thenReturn(null);


        Message result = messageService.save(
                1L,
                "answer",
                createPrincipal(
                        "ROLE_ADMIN",
                        "admin"
                ),
                null
        );


        assertEquals(
                "ROLE_ADMIN",
                result.getSenderRoleName()
        );

        assertTrue(result.isSeenByAdmin());

        assertFalse(result.isSeenByCustomer());


        assertEquals(
                TicketStatus.WaitingForCustomer,
                ticket.getStatus()
        );

        assertTrue(ticket.isCustomerUnread());

        assertFalse(ticket.isAdminUnread());


        verify(ticketRepository)
                .save(ticket);
    }

    @Test
    void save_shouldStoreAttachment() throws Exception {

        Ticket ticket = new Ticket();

        ticket.setStatus(TicketStatus.WaitingForCustomer);


        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("test.jpg");

        when(file.getSize())
                .thenReturn(100L);

        when(file.getContentType())
                .thenReturn("image/jpeg");


        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(messageRepository.findLastMessageTime(1L))
                .thenReturn(null);


        when(fileStorageService.store(
                any(MultipartFile.class),
                eq("customer")
        ))
                .thenReturn("mongo123");


        Message result = messageService.save(
                1L,
                "file message",
                createPrincipal(
                        "ROLE_CUSTOMER",
                        "customer"
                ),
                List.of(file)
        );


        assertEquals(1, result.getAttachments().size());


        Attachment attachment = result.getAttachments().get(0);


        assertEquals("test.jpg", attachment.getFileName());

        assertEquals("mongo123", attachment.getMongoFileId());


        verify(fileStorageService)
                .store(file, "customer");


        verify(attachmentRepository)
                .saveAll(anyList());


        verify(messageRepository, times(2))
                .save(any(Message.class));
    }

    @Test
    void save_shouldDeleteStoredFilesWhenAttachmentSaveFails()
            throws Exception {


        Ticket ticket = new Ticket();

        ticket.setStatus(TicketStatus.WaitingForCustomer);


        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty())
                .thenReturn(false);

        when(file.getContentType())
                .thenReturn("image/jpeg");


        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(messageRepository.findLastMessageTime(1L))
                .thenReturn(null);


        when(fileStorageService.store(any(), any()))
                .thenReturn("mongo123");


        doThrow(new RuntimeException())
                .when(attachmentRepository)
                .saveAll(anyList());


        assertThrows(
                FileStorageException.class,
                () -> messageService.save(
                        1L,
                        "test",
                        createPrincipal(
                                "ROLE_CUSTOMER",
                                "customer"
                        ),
                        List.of(file)
                )
        );


        verify(fileStorageService)
                .deleteById("mongo123");
    }

    @Test
    void findByTicketId_shouldReturnMessagesPage() {

        Message message1 = new Message();
        message1.setContent("first");

        Message message2 = new Message();
        message2.setContent("second");

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("dateTime").descending()
        );

        Page<Message> page =
                new PageImpl<>(List.of(message1, message2));

        when(ticketService.findById(1L))
                .thenReturn(new Ticket());

        when(messageRepository.findByTicket_Id(
                1L,
                pageable
        )).thenReturn(page);


        Page<Message> result =
                messageService.findByTicketId(
                        1L,
                        0,
                        10
                );


        assertEquals(2, result.getTotalElements());

        assertEquals(
                "first",
                result.getContent().get(0).getContent()
        );

        assertEquals(
                "second",
                result.getContent().get(1).getContent()
        );


        verify(ticketService)
                .findById(1L);

        verify(messageRepository)
                .findByTicket_Id(
                        1L,
                        pageable
                );
    }
}
