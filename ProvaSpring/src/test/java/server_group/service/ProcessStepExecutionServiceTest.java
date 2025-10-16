package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.dto.AssignedTaskDTO;
import server_group.model.Order;
import server_group.model.ProcessStepExecution;
import server_group.repository.ProcessStepExecutionRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessStepExecutionServiceTest {

    @Mock
    private ProcessStepExecutionRepository repository;

    @InjectMocks
    private ProcessStepExecutionService service;

    private ProcessStepExecution task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Order order = new Order();
        order.setId(10L);

        task = new ProcessStepExecution();
        task.setId(1L);
        task.setOrder(order);
        task.setMachineryName("MachineA");
        task.setStepIndex(2);
        task.setScheduledStart(LocalDate.now());
        task.setScheduledEnd(LocalDate.now().plusDays(1));
    }

    // --- getAssignedTasksToday ---
    @Test
    void getAssignedTasksToday_success() {
        String email = "user@test.com";
        LocalDate today = LocalDate.now();

        when(repository.findAssignedToday(email, today)).thenReturn(List.of(task));

        List<AssignedTaskDTO> result = service.getAssignedTasksToday(email);

        assertEquals(1, result.size());
        AssignedTaskDTO dto = result.get(0);
        assertEquals(10L, dto.getOrderId());
        assertEquals("MachineA", dto.getMachineryName());
        assertEquals(2, dto.getStepIndex());
        assertNotNull(dto.getScheduledStart());
        assertNotNull(dto.getScheduledEnd());
        verify(repository).findAssignedToday(email, today);
    }

    @Test
    void getAssignedTasksToday_empty() {
        String email = "user@test.com";
        LocalDate today = LocalDate.now();

        when(repository.findAssignedToday(email, today)).thenReturn(List.of());

        List<AssignedTaskDTO> result = service.getAssignedTasksToday(email);

        assertTrue(result.isEmpty());
        verify(repository).findAssignedToday(email, today);
    }

    // --- getAssignedTasks ---
    @Test
    void getAssignedTasks_success() {
        String email = "user@test.com";

        when(repository.findAssigned(email)).thenReturn(List.of(task));

        List<AssignedTaskDTO> result = service.getAssignedTasks(email);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getOrderId());
        verify(repository).findAssigned(email);
    }

    @Test
    void getAssignedTasks_empty() {
        String email = "user@test.com";

        when(repository.findAssigned(email)).thenReturn(List.of());

        List<AssignedTaskDTO> result = service.getAssignedTasks(email);

        assertTrue(result.isEmpty());
        verify(repository).findAssigned(email);
    }

    // --- direct DTO test ---
    @Test
    void assignedTaskDTO_gettersAndSetters() {
        AssignedTaskDTO dto = new AssignedTaskDTO(task);

        // test costruttore
        assertEquals(10L, dto.getOrderId());
        assertEquals("MachineA", dto.getMachineryName());
        assertEquals(2, dto.getStepIndex());

        // test setters
        dto.setOrderId(20L);
        dto.setMachineryName("MachineB");
        dto.setStepIndex(5);
        LocalDate newStart = LocalDate.now().plusDays(3);
        LocalDate newEnd = LocalDate.now().plusDays(7);
        dto.setScheduledStart(newStart);
        dto.setScheduledEnd(newEnd);

        assertEquals(20L, dto.getOrderId());
        assertEquals("MachineB", dto.getMachineryName());
        assertEquals(5, dto.getStepIndex());
        assertEquals(newStart, dto.getScheduledStart());
        assertEquals(newEnd, dto.getScheduledEnd());
    }
}
