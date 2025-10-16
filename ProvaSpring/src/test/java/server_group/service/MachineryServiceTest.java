package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.model.Machinery;
import server_group.repository.MachineryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MachineryServiceTest {

    @Mock
    private MachineryRepository machineryRepository;

    @InjectMocks
    private MachineryService machineryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- findAll ---
    @Test
    void findAll_success() {
        Machinery m1 = new Machinery();
        m1.setId(1L);
        m1.setName("CNC");

        Machinery m2 = new Machinery();
        m2.setId(2L);
        m2.setName("Laser");

        when(machineryRepository.findAll()).thenReturn(Arrays.asList(m1, m2));

        List<Machinery> result = machineryService.findAll();

        assertEquals(2, result.size());
        assertEquals("CNC", result.get(0).getName());
        verify(machineryRepository, times(1)).findAll();
    }

    // --- findById ---
    @Test
    void findById_found() {
        Machinery m = new Machinery();
        m.setId(1L);
        m.setName("CNC");

        when(machineryRepository.findById(1L)).thenReturn(Optional.of(m));

        Optional<Machinery> result = machineryService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("CNC", result.get().getName());
        verify(machineryRepository).findById(1L);
    }

    @Test
    void findById_notFound() {
        when(machineryRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Machinery> result = machineryService.findById(99L);

        assertFalse(result.isPresent());
        verify(machineryRepository).findById(99L);
    }

    // --- findByName ---
    @Test
    void findByName_found() {
        Machinery m = new Machinery();
        m.setId(3L);
        m.setName("Pressa");

        when(machineryRepository.findByName("Pressa")).thenReturn(Optional.of(m));

        Optional<Machinery> result = machineryService.findByName("Pressa");

        assertTrue(result.isPresent());
        assertEquals("Pressa", result.get().getName());
        verify(machineryRepository).findByName("Pressa");
    }

    @Test
    void findByName_notFound() {
        when(machineryRepository.findByName("Inesistente")).thenReturn(Optional.empty());

        Optional<Machinery> result = machineryService.findByName("Inesistente");

        assertFalse(result.isPresent());
        verify(machineryRepository).findByName("Inesistente");
    }

    // --- save ---
    @Test
    void save_success() {
        Machinery m = new Machinery();
        m.setName("Robot");

        when(machineryRepository.save(m)).thenReturn(m);

        Machinery saved = machineryService.save(m);

        assertEquals("Robot", saved.getName());
        verify(machineryRepository).save(m);
    }

    // --- delete ---
    @Test
    void delete_success() {
        machineryService.delete(10L);
        verify(machineryRepository, times(1)).deleteById(10L);
    }
}
