package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.dto.ModelWithStepsDTO;
import server_group.model.*;
import server_group.repository.OrderRepository;
import server_group.repository.ProcessStepExecutionRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderProductionServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelService modelService;

    @Mock
    private ProcessStepExecutionRepository executionRepository;

    @InjectMocks
    private OrderProductionService service;

    private Order order;
    private Model model;
    private ModelWithStepsDTO modelDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        model = new Model();
        model.setName("ModelX");

        order = new Order();
        order.setId(1L);
        order.setModel(model);
        order.setQuantity(2);
        order.setStatus(OrderStatus.CREATED);
        order.setDeadline(LocalDate.now().plusDays(10));

        // Step DTO
        ModelWithStepsDTO.ProcessStepDTO step = new ModelWithStepsDTO.ProcessStepDTO();
        step.setStepOrder(1);
        step.setDuration(Duration.ofHours(2));
        ModelWithStepsDTO.MachineryDTO mach = new ModelWithStepsDTO.MachineryDTO();
        mach.setName("MachineA");
        step.setMachinery(mach);

        modelDto = new ModelWithStepsDTO();
        modelDto.setName("ModelX");
        modelDto.setPrice(100.0);
        modelDto.setProcessSteps(List.of(step));
    }

    // --- startProduction ---
    @Test
    void startProduction_success() {
        when(modelService.getAllModelsWithProcess()).thenReturn(List.of(modelDto));
        when(executionRepository.findAll()).thenReturn(List.of());

        service.startProduction(order);

        assertEquals(OrderStatus.IN_PRODUCTION, order.getStatus());
        assertNotNull(order.getStartDate());
        verify(orderRepository).save(order);
        verify(executionRepository).save(any(ProcessStepExecution.class));
    }

    @Test
    void startProduction_alreadyInProduction_throws() {
        order.setStatus(OrderStatus.IN_PRODUCTION);
        assertThrows(IllegalStateException.class, () -> service.startProduction(order));
    }

    @Test
    void startProduction_alreadyCompleted_throws() {
        order.setStatus(OrderStatus.COMPLETED);
        assertThrows(IllegalStateException.class, () -> service.startProduction(order));
    }

    @Test
    void startProduction_alreadyCancelled_throws() {
        order.setStatus(OrderStatus.CANCELLED);
        assertThrows(IllegalStateException.class, () -> service.startProduction(order));
    }
    // --- updateExecutionDates ---
    @Test
    void updateExecutionDates_success() {
        ProcessStepExecution exec = new ProcessStepExecution();
        exec.setId(100L);

        when(executionRepository.findById(100L)).thenReturn(Optional.of(exec));

        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(2);

        service.updateExecutionDates(100L, start, end);

        assertEquals(start, exec.getActualStart());
        assertEquals(end, exec.getActualEnd());
        verify(executionRepository).save(exec);
    }

    // --- deleteAllExecutions ---
    @Test
    void deleteAllExecutions_success() {
        order.setStatus(OrderStatus.IN_PRODUCTION);

        service.deleteAllExecutions(order);

        assertNotNull(order.getEndDate());
        verify(executionRepository).deleteByOrderId(order.getId());
        verify(orderRepository).save(order);
    }
}
