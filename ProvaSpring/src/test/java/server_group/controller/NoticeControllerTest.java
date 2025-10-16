package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import server_group.dto.NoticeDTO;
import server_group.model.CustomUser;
import server_group.model.Notice;
import server_group.security.Role;
import server_group.service.NoticeService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoticeControllerTest {

    @Mock
    private NoticeService noticeService;

    @Mock
    private NoticeEventPublisher noticeEventPublisher;

    @InjectMocks
    private NoticeController noticeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- TEST GET ALL NOTICES ---
    @Test
    void getAllNotices_successful() {
        // Arrange
        NoticeDTO dto1 = new NoticeDTO();
        NoticeDTO dto2 = new NoticeDTO();
        List<NoticeDTO> notices = Arrays.asList(dto1, dto2);
        when(noticeService.getAllNoticesFlat("user@test.com")).thenReturn(notices);

        // Act
        ResponseEntity<List<NoticeDTO>> result = noticeController.getAllNotices("user@test.com");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(notices, result.getBody());
        verify(noticeService, times(1)).getAllNoticesFlat("user@test.com");
    }

    // --- TEST CREATE NOTICE ---
    @Test
    void createNotice_successful() {
        // Arrange
        NoticeDTO inputDto = new NoticeDTO();
        inputDto.setCreatorEmail("creator@test.com");

        // Creo un utente
        CustomUser creator = new CustomUser();
        creator.setEmail("creator@test.com");
        creator.setName("Mario");
        creator.setSurname("Rossi");
        creator.setRole(Role.EMPLOYEE);

        // Creo un Notice
        Notice savedEntity = new Notice();
        savedEntity.setId(1L);
        savedEntity.setCreator(creator);
        savedEntity.setSubject("Test Subject");
        savedEntity.setDescription("Test Description");

        when(noticeService.saveFromDto(inputDto)).thenReturn(savedEntity);

        // Act
        ResponseEntity<NoticeDTO> result = noticeController.createNotice(inputDto);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("creator@test.com", result.getBody().getCreatorEmail());
        assertEquals("Mario Rossi", result.getBody().getCreatorFullName());
        assertEquals("Test Subject", result.getBody().getSubject());
        assertEquals("Test Description", result.getBody().getDescription());
        assertEquals("PersonalNotice", result.getBody().getCategory()); // perché creatorEmail == currentUserEmail

        verify(noticeService, times(1)).saveFromDto(inputDto);
        verify(noticeEventPublisher, times(1)).publishEvent(any(NoticeDTO.class));
    }


    // --- TEST DELETE NOTICE ---
    @Test
    void deleteNotice_successful() {
        // Arrange
        when(noticeService.deleteNotice(1L)).thenReturn(true);

        // Act
        ResponseEntity<Void> result = noticeController.deleteNotice(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(noticeService, times(1)).deleteNotice(1L);
        verify(noticeEventPublisher, times(1)).publishEvent("deleted: 1");
    }

    @Test
    void deleteNotice_notFound() {
        // Arrange
        when(noticeService.deleteNotice(99L)).thenReturn(false);

        // Act
        ResponseEntity<Void> result = noticeController.deleteNotice(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(noticeService, times(1)).deleteNotice(99L);
        verify(noticeEventPublisher, never()).publishEvent(any());
    }

    // --- TEST STREAM NOTICES ---
    @Test
    void streamNotices_successful() {
        // Arrange
        SseEmitter emitter = new SseEmitter();
        when(noticeEventPublisher.registerClient()).thenReturn(emitter);

        // Act
        SseEmitter result = noticeController.streamNotices();

        // Assert
        assertNotNull(result);
        assertEquals(emitter, result);
        verify(noticeEventPublisher, times(1)).registerClient();
    }
}
