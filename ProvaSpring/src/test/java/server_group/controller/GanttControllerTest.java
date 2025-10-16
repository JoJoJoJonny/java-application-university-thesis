package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server_group.dto.GanttBlockDTO;
import server_group.service.GanttService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GanttControllerTest {

    @Mock
    private GanttService ganttService;

    @InjectMocks
    private GanttController ganttController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- TEST GET ALL GANTT ---
    @Test
    void getAllGantt_successful() {
        // Arrange
        GanttBlockDTO block1 = new GanttBlockDTO();
        GanttBlockDTO block2 = new GanttBlockDTO();
        List<GanttBlockDTO> blocks = Arrays.asList(block1, block2);

        when(ganttService.getAllScheduledGanttBlocks()).thenReturn(blocks);

        // Act
        ResponseEntity<List<GanttBlockDTO>> result = ganttController.getAllGantt();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(blocks, result.getBody());
        verify(ganttService, times(1)).getAllScheduledGanttBlocks();
    }

    // --- TEST UPDATE BLOCKS ---
    @Test
    void updateBlocks_successful() {
        // Arrange
        GanttBlockDTO block1 = new GanttBlockDTO();
        GanttBlockDTO block2 = new GanttBlockDTO();
        List<GanttBlockDTO> modifiedBlocks = Arrays.asList(block1, block2);

        doNothing().when(ganttService).updateBlocks(modifiedBlocks);

        // Act
        ResponseEntity<?> result = ganttController.updateBlocks(modifiedBlocks);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(modifiedBlocks, result.getBody());
        verify(ganttService, times(1)).updateBlocks(modifiedBlocks);
    }

    @Test
    void updateBlocks_failure() {
        // Arrange
        List<GanttBlockDTO> modifiedBlocks = Arrays.asList(new GanttBlockDTO());
        doThrow(new RuntimeException("DB error")).when(ganttService).updateBlocks(modifiedBlocks);

        // Act
        ResponseEntity<?> result = ganttController.updateBlocks(modifiedBlocks);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
}
