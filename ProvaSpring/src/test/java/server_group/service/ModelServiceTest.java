package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.dto.ModelWithStepsDTO;
import server_group.model.Machinery;
import server_group.model.Model;
import server_group.model.ProcessStep;
import server_group.model.Raw;
import server_group.repository.ModelRepository;
import server_group.repository.ProcessStepRepository;
import server_group.repository.RawRepository;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelServiceTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private ProcessStepRepository processStepRepository;

    @Mock
    private RawRepository rawRepository;

    @InjectMocks
    private ModelService modelService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- getAllModels ---
    @Test
    void getAllModels_success() {
        Model m1 = new Model();
        m1.setName("M1");
        Model m2 = new Model();
        m2.setName("M2");

        when(modelRepository.findAll()).thenReturn(Arrays.asList(m1, m2));

        List<Model> result = modelService.getAllModels();

        assertEquals(2, result.size());
        verify(modelRepository).findAll();
    }

    // --- getAllModelsWithProcess ---
    @Test
    void getAllModelsWithProcess_success() {
        Raw raw = new Raw();
        raw.setId(1L);
        raw.setMaterial("Steel");
        raw.setShape("Block");
        raw.setSize("10x10");

        Model model = new Model();
        model.setName("ModelX");
        model.setPrice(99.9);
        model.setRaw(raw);

        Machinery mach = new Machinery();
        mach.setName("CNC");

        ProcessStep step = new ProcessStep();
        step.setId(100L);
        step.setStepOrder(1);
        step.setDuration(Duration.ofHours(2));
        step.setSemifinishedName("Cut");
        step.setMachinery(mach);

        when(modelRepository.findAll()).thenReturn(List.of(model));
        when(processStepRepository.findByModelNameOrderByStepOrder("ModelX"))
                .thenReturn(List.of(step));

        List<ModelWithStepsDTO> dtos = modelService.getAllModelsWithProcess();

        assertEquals(1, dtos.size());
        ModelWithStepsDTO dto = dtos.get(0);
        assertEquals("ModelX", dto.getName());
        assertEquals(99.9, dto.getPrice());
        assertEquals("Steel", dto.getRaw().getMaterial());
        assertEquals(1, dto.getProcessSteps().size());
        assertEquals("CNC", dto.getProcessSteps().get(0).getMachinery().getName());

        verify(modelRepository).findAll();
        verify(processStepRepository).findByModelNameOrderByStepOrder("ModelX");
    }

    // --- getModelByName ---
    @Test
    void getModelByName_found() {
        Model m = new Model();
        m.setName("ModelY");
        when(modelRepository.findById("ModelY")).thenReturn(Optional.of(m));

        Optional<Model> result = modelService.getModelByName("ModelY");

        assertTrue(result.isPresent());
        assertEquals("ModelY", result.get().getName());
    }

    @Test
    void getModelByName_notFound() {
        when(modelRepository.findById("Unknown")).thenReturn(Optional.empty());

        Optional<Model> result = modelService.getModelByName("Unknown");

        assertFalse(result.isPresent());
    }

    // --- saveModel ---
    @Test
    void saveModel_withRawId_fetchesRaw() {
        Raw raw = new Raw();
        raw.setId(5L);
        raw.setMaterial("Iron");

        Model model = new Model();
        model.setName("M");
        model.setRaw(raw);

        when(rawRepository.findById(5L)).thenReturn(Optional.of(raw));
        when(modelRepository.save(model)).thenReturn(model);

        Model saved = modelService.saveModel(model);

        assertEquals("Iron", saved.getRaw().getMaterial());
        verify(rawRepository).findById(5L);
        verify(modelRepository).save(model);
    }

    @Test
    void saveModel_withRawId_notFound_throws() {
        Raw raw = new Raw();
        raw.setId(99L);

        Model model = new Model();
        model.setRaw(raw);

        when(rawRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> modelService.saveModel(model));
    }

    @Test
    void saveModel_withoutRawId_savesDirectly() {
        Model model = new Model();
        model.setName("NoRaw");

        when(modelRepository.save(model)).thenReturn(model);

        Model saved = modelService.saveModel(model);

        assertEquals("NoRaw", saved.getName());
        verify(modelRepository).save(model);
    }

    // --- updateModel ---
    @Test
    void updateModel_success_withRaw() {
        Raw raw = new Raw();
        raw.setId(1L);
        raw.setMaterial("Wood");

        Model existing = new Model();
        existing.setName("M");
        existing.setPrice(50.0);

        Model update = new Model();
        update.setPrice(75.0);
        update.setRaw(raw);

        when(modelRepository.findById("M")).thenReturn(Optional.of(existing));
        when(rawRepository.findById(1L)).thenReturn(Optional.of(raw));
        when(modelRepository.save(any(Model.class))).thenReturn(existing);

        Optional<Model> result = modelService.updateModel("M", update);

        assertTrue(result.isPresent());
        assertEquals(75.0, result.get().getPrice());
        assertEquals("Wood", result.get().getRaw().getMaterial());
    }

    @Test
    void updateModel_notFound_returnsEmpty() {
        Model update = new Model();
        when(modelRepository.findById("X")).thenReturn(Optional.empty());

        Optional<Model> result = modelService.updateModel("X", update);

        assertFalse(result.isPresent());
    }

    @Test
    void updateModel_rawNotFound_throws() {
        Raw raw = new Raw();
        raw.setId(100L);

        Model existing = new Model();
        existing.setName("M");

        Model update = new Model();
        update.setPrice(70.0);
        update.setRaw(raw);

        when(modelRepository.findById("M")).thenReturn(Optional.of(existing));
        when(rawRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> modelService.updateModel("M", update));
    }

    // --- deleteModel ---
    @Test
    void deleteModel_exists_true() {
        when(modelRepository.existsById("M")).thenReturn(true);

        boolean result = modelService.deleteModel("M");

        assertTrue(result);
        verify(modelRepository).deleteById("M");
    }

    @Test
    void deleteModel_notExists_false() {
        when(modelRepository.existsById("Unknown")).thenReturn(false);

        boolean result = modelService.deleteModel("Unknown");

        assertFalse(result);
        verify(modelRepository, never()).deleteById(any());
    }
}
