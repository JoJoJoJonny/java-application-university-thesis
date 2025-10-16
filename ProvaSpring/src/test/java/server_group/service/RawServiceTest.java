package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.model.Raw;
import server_group.repository.RawRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RawServiceTest {

    @Mock
    private RawRepository rawRepository;

    @InjectMocks
    private RawService rawService;

    private Raw raw;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        raw = new Raw();
        raw.setId(1L);
        raw.setShape("Sheet");
        raw.setMaterial("Steel");
        raw.setSize("100x200");
        raw.setThickness("5mm");
        raw.setCastingNumber("C12345");
    }

    // --- findAll ---
    @Test
    void findAll_success() {
        when(rawRepository.findAll()).thenReturn(List.of(raw));

        List<Raw> result = rawService.findAll();

        assertEquals(1, result.size());
        assertEquals("Steel", result.get(0).getMaterial());
        verify(rawRepository).findAll();
    }

    // --- findById ---
    @Test
    void findById_found() {
        when(rawRepository.findById(1L)).thenReturn(Optional.of(raw));

        Optional<Raw> result = rawService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("C12345", result.get().getCastingNumber());
        verify(rawRepository).findById(1L);
    }

    @Test
    void findById_notFound() {
        when(rawRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Raw> result = rawService.findById(2L);

        assertFalse(result.isPresent());
        verify(rawRepository).findById(2L);
    }

    // --- save ---
    @Test
    void save_success() {
        when(rawRepository.save(raw)).thenReturn(raw);

        Raw result = rawService.save(raw);

        assertEquals("Sheet", result.getShape());
        assertEquals("5mm", result.getThickness());
        verify(rawRepository).save(raw);
    }

    // --- delete ---
    @Test
    void delete_success() {
        doNothing().when(rawRepository).deleteById(1L);

        rawService.delete(1L);

        verify(rawRepository).deleteById(1L);
    }
}
