package server_group.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.model.Machinery;
import server_group.model.Model;
import server_group.model.ProcessStep;
import server_group.repository.MachineryRepository;
import server_group.repository.ModelRepository;
import server_group.repository.ProcessStepRepository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessStepServiceTest {

    @Mock
    private ProcessStepRepository processStepRepository;

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private MachineryRepository machineryRepository;

    @InjectMocks
    private ProcessStepService service;

    private ProcessStep step;
    private Model model;
    private Machinery machinery;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        model = new Model();
        model.setName("ModelX");

        machinery = new Machinery();
        machinery.setId(1L);
        machinery.setName("MachineA");

        step = new ProcessStep();
        step.setId(1L);
        step.setModel(model);
        step.setStepOrder(1);
        step.setDuration(Duration.ofMinutes(30));
        step.setSemifinishedName("Semi1");
        step.setMachinery(machinery);
    }

    // --- findAll ---
    @Test
    void findAll_success() {
        when(processStepRepository.findAll()).thenReturn(List.of(step));

        List<ProcessStep> result = service.findAll();

        assertEquals(1, result.size());
        verify(processStepRepository).findAll();
    }

    // --- findByModelNameOrderByStepOrder ---
    @Test
    void findByModelNameOrderByStepOrder_success() {
        when(processStepRepository.findByModelNameOrderByStepOrder("ModelX")).thenReturn(List.of(step));

        List<ProcessStep> result = service.findByModelNameOrderByStepOrder("ModelX");

        assertEquals(1, result.size());
        verify(processStepRepository).findByModelNameOrderByStepOrder("ModelX");
    }

    // --- deleteStep ---
    @Test
    void deleteStep_success_withReorder() {
        ProcessStep step2 = new ProcessStep();
        step2.setId(2L);
        step2.setModel(model);
        step2.setStepOrder(2);

        when(processStepRepository.findById(1L)).thenReturn(Optional.of(step));
        when(processStepRepository.findByModelName("ModelX")).thenReturn(List.of(step2));

        service.deleteStep(1L);

        verify(processStepRepository).deleteById(1L);
        assertEquals(1, step2.getStepOrder()); // reorder fatto
        verify(processStepRepository).save(step2);
    }

    @Test
    void deleteStep_notFound() {
        when(processStepRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteStep(99L));
    }

    // --- updateStep ---
    @Test
    void updateStep_success() throws Exception {
        ProcessStep updated = new ProcessStep();
        updated.setDuration(Duration.ofHours(1));
        updated.setSemifinishedName("SemiUpdated");
        updated.setMachinery(machinery);

        when(processStepRepository.findById(1L)).thenReturn(Optional.of(step));

        service.updateStep(1L, updated);

        assertEquals(Duration.ofHours(1), step.getDuration());
        assertEquals("SemiUpdated", step.getSemifinishedName());
        assertEquals(machinery, step.getMachinery());
        verify(processStepRepository).save(step);
    }

    @Test
    void updateStep_notFound() {
        ProcessStep updated = new ProcessStep();
        when(processStepRepository.findById(99L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> service.updateStep(99L, updated));
        assertTrue(ex.getMessage().contains("Step not found with id: 99"));
    }

    // --- addStepToModel ---
    @Test
    void addStepToModel_success_withShift() {
        ProcessStep existing = new ProcessStep();
        existing.setId(2L);
        existing.setModel(model);
        existing.setStepOrder(2);

        when(modelRepository.findByName("ModelX")).thenReturn(Optional.of(model));
        when(machineryRepository.findByName("MachineA")).thenReturn(Optional.of(machinery));
        when(processStepRepository.findByModelAndStepOrderGreaterThanEqualOrderByStepOrderDesc(model, 2))
                .thenReturn(List.of(existing));
        when(processStepRepository.save(any(ProcessStep.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcessStep result = service.addStepToModel("ModelX", Duration.ofMinutes(20), "Semi2", "MachineA", 1);

        assertEquals(model, result.getModel());
        assertEquals(2, result.getStepOrder());
        assertEquals("Semi2", result.getSemifinishedName());
        assertEquals(machinery, result.getMachinery());
        verify(processStepRepository).saveAll(List.of(existing));
    }

    @Test
    void addStepToModel_modelNotFound() {
        when(modelRepository.findByName("WrongModel")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.addStepToModel("WrongModel", Duration.ofMinutes(10), "Semi", "MachineA", 1));
    }

    @Test
    void addStepToModel_machineryNotFound() {
        when(modelRepository.findByName("ModelX")).thenReturn(Optional.of(model));
        when(machineryRepository.findByName("WrongMachine")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.addStepToModel("ModelX", Duration.ofMinutes(10), "Semi", "WrongMachine", 1));
    }
}
