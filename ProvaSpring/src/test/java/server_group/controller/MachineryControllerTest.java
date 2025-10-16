package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server_group.model.Machinery;
import server_group.service.MachineryService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MachineryControllerTest {

    @Mock
    private MachineryService machineryService;

    @InjectMocks
    private MachineryController machineryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- TEST GET ALL MACHINERY ---
    @Test
    void getAllMachinery_successful() {
        // Arrange
        Machinery m1 = new Machinery();
        Machinery m2 = new Machinery();
        List<Machinery> list = Arrays.asList(m1, m2);
        when(machineryService.findAll()).thenReturn(list);

        // Act
        ResponseEntity<?> result = machineryController.getAllMachinery();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(list, result.getBody());
        verify(machineryService, times(1)).findAll();
    }

    // --- TEST GET MACHINERY BY ID ---
    @Test
    void getMachineryById_found() {
        // Arrange
        Machinery machinery = new Machinery();
        machinery.setId(1L);
        when(machineryService.findById(1L)).thenReturn(Optional.of(machinery));

        // Act
        ResponseEntity<?> result = machineryController.getMachineryById(1L);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(machinery, result.getBody());
        verify(machineryService, times(1)).findById(1L);
    }

    @Test
    void getMachineryById_notFound() {
        // Arrange
        when(machineryService.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = machineryController.getMachineryById(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Machinery not found", result.getBody());
    }

    // --- TEST GET MACHINERY BY NAME ---
    @Test
    void getMachineryByName_found() {
        // Arrange
        Machinery machinery = new Machinery();
        machinery.setName("Lathe");
        when(machineryService.findByName("Lathe")).thenReturn(Optional.of(machinery));

        // Act
        ResponseEntity<?> result = machineryController.getMachineryByName("Lathe");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(machinery, result.getBody());
        verify(machineryService, times(1)).findByName("Lathe");
    }

    @Test
    void getMachineryByName_notFound() {
        // Arrange
        when(machineryService.findByName("Lathe")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = machineryController.getMachineryByName("Lathe");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Machinery with name 'Lathe' not found", result.getBody());
    }

    // --- TEST CREATE MACHINERY ---
    @Test
    void createMachinery_successful() {
        // Arrange
        Machinery machinery = new Machinery();
        when(machineryService.save(machinery)).thenReturn(machinery);

        // Act
        ResponseEntity<?> result = machineryController.createMachinery(machinery);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(machinery, result.getBody());
        verify(machineryService, times(1)).save(machinery);
    }

    @Test
    void createMachinery_failure() {
        // Arrange
        Machinery machinery = new Machinery();
        when(machineryService.save(machinery)).thenThrow(new RuntimeException("DB error"));

        // Act
        ResponseEntity<?> result = machineryController.createMachinery(machinery);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Failed to create machinery: DB error", result.getBody());
    }

    // --- TEST UPDATE MACHINERY ---
    @Test
    void updateMachinery_found() {
        // Arrange
        Machinery machinery = new Machinery();
        machinery.setId(1L);
        when(machineryService.findById(1L)).thenReturn(Optional.of(machinery));
        when(machineryService.save(machinery)).thenReturn(machinery);

        // Act
        ResponseEntity<?> result = machineryController.updateMachinery(1L, machinery);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(machinery, result.getBody());
        verify(machineryService, times(1)).findById(1L);
        verify(machineryService, times(1)).save(machinery);
    }

    @Test
    void updateMachinery_notFound() {
        // Arrange
        Machinery machinery = new Machinery();
        when(machineryService.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = machineryController.updateMachinery(1L, machinery);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Machinery not found", result.getBody());
    }

    // --- TEST DELETE MACHINERY ---
    @Test
    void deleteMachinery_found() {
        // Arrange
        Machinery machinery = new Machinery();
        machinery.setId(1L);
        when(machineryService.findById(1L)).thenReturn(Optional.of(machinery));
        doNothing().when(machineryService).delete(1L);

        // Act
        ResponseEntity<?> result = machineryController.deleteMachinery(1L);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Machinery deleted successfully", result.getBody());
        verify(machineryService, times(1)).delete(1L);
    }

    @Test
    void deleteMachinery_notFound() {
        // Arrange
        when(machineryService.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = machineryController.deleteMachinery(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Machinery not found", result.getBody());
    }
}
