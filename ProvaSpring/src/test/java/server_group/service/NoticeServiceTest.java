package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import server_group.dto.NoticeDTO;
import server_group.model.CustomUser;
import server_group.model.Notice;
import server_group.repository.CustomUserRepository;
import server_group.repository.NoticeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private CustomUserRepository customUserRepository;

    @InjectMocks
    private NoticeService noticeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- getAllNoticesFlat ---
    @Test
    void getAllNoticesFlat_success() {
        CustomUser user = new CustomUser();
        user.setEmail("creator@test.com");

        Notice notice = new Notice();
        notice.setId(1L);
        notice.setSubject("Test");
        notice.setDescription("Desc");
        notice.setCreator(user);

        when(noticeRepository.findAll()).thenReturn(List.of(notice));

        List<NoticeDTO> result = noticeService.getAllNoticesFlat("creator@test.com");

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getSubject());
        verify(noticeRepository).findAll();
    }

    // --- saveNotice ---
    @Test
    void saveNotice_success() {
        Notice n = new Notice();
        n.setSubject("Hello");

        when(noticeRepository.save(n)).thenReturn(n);

        Notice saved = noticeService.saveNotice(n);

        assertEquals("Hello", saved.getSubject());
        verify(noticeRepository).save(n);
    }

    // --- saveFromDto ---
    @Test
    void saveFromDto_success() {
        NoticeDTO dto = new NoticeDTO();
        dto.setCreatorEmail("user@test.com");
        dto.setSubject("Subj");
        dto.setDescription("Desc");

        CustomUser creator = new CustomUser();
        creator.setEmail("user@test.com");

        Notice saved = new Notice();
        saved.setId(1L);
        saved.setCreator(creator);
        saved.setSubject("Subj");
        saved.setDescription("Desc");

        when(customUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(creator));
        when(noticeRepository.save(any(Notice.class))).thenReturn(saved);

        Notice result = noticeService.saveFromDto(dto);

        assertNotNull(result);
        assertEquals("Subj", result.getSubject());
        assertEquals("Desc", result.getDescription());
        assertEquals("user@test.com", result.getCreator().getEmail());

        verify(customUserRepository).findByEmail("user@test.com");
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    void saveFromDto_missingCreatorEmail_throws() {
        NoticeDTO dto = new NoticeDTO();
        dto.setCreatorEmail("   "); // empty

        assertThrows(IllegalArgumentException.class, () -> noticeService.saveFromDto(dto));
    }

    @Test
    void saveFromDto_userNotFound_throws() {
        NoticeDTO dto = new NoticeDTO();
        dto.setCreatorEmail("notfound@test.com");
        dto.setSubject("X");

        when(customUserRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> noticeService.saveFromDto(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- deleteNotice ---
    @Test
    void deleteNotice_exists_true() {
        when(noticeRepository.existsById(1L)).thenReturn(true);

        boolean result = noticeService.deleteNotice(1L);

        assertTrue(result);
        verify(noticeRepository).deleteById(1L);
    }

    @Test
    void deleteNotice_notExists_false() {
        when(noticeRepository.existsById(2L)).thenReturn(false);

        boolean result = noticeService.deleteNotice(2L);

        assertFalse(result);
        verify(noticeRepository, never()).deleteById(any());
    }
}
