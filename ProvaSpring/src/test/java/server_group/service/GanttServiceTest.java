package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.dto.GanttBlockDTO;
import server_group.model.*;
import server_group.repository.CustomUserRepository;
import server_group.repository.ProcessStepExecutionRepository;
import server_group.security.Role;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GanttServiceTest {

    @Mock
    private ProcessStepExecutionRepository executionRepository;

    @Mock
    private CustomUserRepository customUserRepository;

    @InjectMocks
    private GanttService ganttService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- getAllScheduledGanttBlocks ---
    @Test
    void getAllScheduledGanttBlocks_success() {
        ProcessStepExecution exec = new ProcessStepExecution();
        exec.setId(1L);
        exec.setOrder(new Order());
        exec.setMachineryName("CNC");
        exec.setStepIndex(1);
        exec.setScheduledStart(LocalDate.of(2025, 1, 1));
        exec.setScheduledEnd(LocalDate.of(2025, 1, 2));
        exec.setActualStart(LocalDate.of(2025, 1, 1));
        exec.setActualEnd(LocalDate.of(2025, 1, 2));

        CustomUser employee = new CustomUser();
        employee.setEmail("emp@test.com");
        employee.setName("Mario");
        employee.setSurname("Rossi");
        employee.setRole(Role.EMPLOYEE);
        exec.setAssignedEmployee(employee);

        when(executionRepository.findByOrderStatus(OrderStatus.IN_PRODUCTION))
                .thenReturn(Collections.singletonList(exec));

        List<GanttBlockDTO> blocks = ganttService.getAllScheduledGanttBlocks();

        assertEquals(1, blocks.size());
        GanttBlockDTO block = blocks.get(0);
        assertEquals(1L, block.getExecutionId());
        assertEquals("CNC", block.getMachineryName());
        assertEquals("Step 1", block.getStepName());
        assertEquals("emp@test.com", block.getAssignedEmployeeEmail());
        assertEquals("Mario Rossi", block.getAssignedEmployeeFullName());

        verify(executionRepository, times(1)).findByOrderStatus(OrderStatus.IN_PRODUCTION);
    }

    // --- updateBlocks con Employee ---
    @Test
    void updateBlocks_assignEmployee_success() {
        GanttBlockDTO dto = new GanttBlockDTO();
        dto.setExecutionId(1L);
        dto.setActualStart(LocalDate.of(2025, 2, 1));
        dto.setActualEnd(LocalDate.of(2025, 2, 2));
        dto.setAssignedEmployeeEmail("emp@test.com");

        ProcessStepExecution exec = new ProcessStepExecution();
        exec.setId(1L);

        CustomUser employee = new CustomUser();
        employee.setEmail("emp@test.com");
        employee.setRole(Role.EMPLOYEE);

        when(executionRepository.findById(1L)).thenReturn(Optional.of(exec));
        when(customUserRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));

        ganttService.updateBlocks(Collections.singletonList(dto));

        assertEquals(employee, exec.getAssignedEmployee());
        assertEquals(LocalDate.of(2025, 2, 1), exec.getActualStart());
        assertEquals(LocalDate.of(2025, 2, 2), exec.getActualEnd());

        verify(executionRepository).save(exec);
    }

    // --- updateBlocks senza assegnazione ---
    @Test
    void updateBlocks_removeAssignment_success() {
        GanttBlockDTO dto = new GanttBlockDTO();
        dto.setExecutionId(2L);

        ProcessStepExecution exec = new ProcessStepExecution();
        exec.setId(2L);
        exec.setAssignedEmployee(new CustomUser()); // prima aveva un dipendente

        when(executionRepository.findById(2L)).thenReturn(Optional.of(exec));

        ganttService.updateBlocks(Collections.singletonList(dto));

        assertNull(exec.getAssignedEmployee());
        verify(executionRepository).save(exec);
    }

    // --- updateBlocks: execution non trovato ---
    @Test
    void updateBlocks_executionNotFound_throws() {
        GanttBlockDTO dto = new GanttBlockDTO();
        dto.setExecutionId(99L);

        when(executionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                ganttService.updateBlocks(Collections.singletonList(dto)));

        assertTrue(ex.getMessage().contains("Step non trovato per ID"));
    }

    // --- updateBlocks: utente non trovato ---
    @Test
    void updateBlocks_userNotFound_throws() {
        GanttBlockDTO dto = new GanttBlockDTO();
        dto.setExecutionId(3L);
        dto.setAssignedEmployeeEmail("missing@test.com");

        ProcessStepExecution exec = new ProcessStepExecution();
        exec.setId(3L);

        when(executionRepository.findById(3L)).thenReturn(Optional.of(exec));
        when(customUserRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                ganttService.updateBlocks(Collections.singletonList(dto)));

        assertTrue(ex.getMessage().contains("Utente non trovato"));
    }

    // --- updateBlocks: utente NON Employee ---
    @Test
    void updateBlocks_userNotEmployee_throws() {
        GanttBlockDTO dto = new GanttBlockDTO();
        dto.setExecutionId(4L);
        dto.setAssignedEmployeeEmail("manager@test.com");

        ProcessStepExecution exec = new ProcessStepExecution();
        exec.setId(4L);

        CustomUser manager = new CustomUser();
        manager.setEmail("manager@test.com");
        manager.setRole(Role.MANAGER);

        when(executionRepository.findById(4L)).thenReturn(Optional.of(exec));
        when(customUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                ganttService.updateBlocks(Collections.singletonList(dto)));

        assertTrue(ex.getMessage().contains("non è un Employee"));
    }
}
