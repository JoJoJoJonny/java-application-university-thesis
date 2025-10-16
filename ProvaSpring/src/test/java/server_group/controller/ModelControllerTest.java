package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server_group.dto.ModelWithStepsDTO;
import server_group.model.Model;
import server_group.service.ModelService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ModelControllerTest {

    @Mock
    private ModelService modelService;

    @InjectMocks
    private ModelController modelController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- TEST GET ALL ---
    @Test
    void getAll_successful() {
        // Arrange
        Model m1 = new Model();
        Model m2 = new Model();
        List<Model> models = Arrays.asList(m1, m2);
        when(modelService.getAllModels()).thenReturn(models);

        // Act
        List<Model> result = modelController.getAll();

        // Assert
        assertEquals(models, result);
        verify(modelService, times(1)).getAllModels();
    }

    // --- TEST GET MODELS WITH PROCESSES ---
    @Test
    void getModelsWithProcesses_successful() {
        // Arrange
        ModelWithStepsDTO dto1 = new ModelWithStepsDTO();
        ModelWithStepsDTO dto2 = new ModelWithStepsDTO();
        List<ModelWithStepsDTO> dtos = Arrays.asList(dto1, dto2);
        when(modelService.getAllModelsWithProcess()).thenReturn(dtos);

        // Act
        ResponseEntity<List<ModelWithStepsDTO>> result = modelController.getModelsWithProcesses();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(dtos, result.getBody());
        verify(modelService, times(1)).getAllModelsWithProcess();
    }

    // --- TEST GET BY NAME ---
    @Test
    void getByName_found() {
        // Arrange
        Model model = new Model();
        model.setName("TestModel");
        when(modelService.getModelByName("TestModel")).thenReturn(Optional.of(model));

        // Act
        ResponseEntity<Model> result = modelController.getByName("TestModel");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(model, result.getBody());
        verify(modelService, times(1)).getModelByName("TestModel");
    }

    @Test
    void getByName_notFound() {
        // Arrange
        when(modelService.getModelByName("Unknown")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Model> result = modelController.getByName("Unknown");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // --- TEST CREATE ---
    @Test
    void create_successful() {
        // Arrange
        Model model = new Model();
        model.setName("NewModel");
        when(modelService.saveModel(model)).thenReturn(model);

        // Act
        ResponseEntity<Model> result = modelController.create(model);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(model, result.getBody());
        assertEquals(URI.create("/api/models/NewModel"), result.getHeaders().getLocation());
        verify(modelService, times(1)).saveModel(model);
    }

    // --- TEST UPDATE ---
    @Test
    void update_found() {
        // Arrange
        Model model = new Model();
        when(modelService.updateModel("TestModel", model)).thenReturn(Optional.of(model));

        // Act
        ResponseEntity<Model> result = modelController.update("TestModel", model);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(model, result.getBody());
        verify(modelService, times(1)).updateModel("TestModel", model);
    }

    @Test
    void update_notFound() {
        // Arrange
        Model model = new Model();
        when(modelService.updateModel("Unknown", model)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Model> result = modelController.update("Unknown", model);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // --- TEST DELETE ---
    @Test
    void delete_successful() {
        // Arrange
        when(modelService.deleteModel("TestModel")).thenReturn(true);

        // Act
        ResponseEntity<Void> result = modelController.delete("TestModel");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(modelService, times(1)).deleteModel("TestModel");
    }

    @Test
    void delete_notFound() {
        // Arrange
        when(modelService.deleteModel("Unknown")).thenReturn(false);

        // Act
        ResponseEntity<Void> result = modelController.delete("Unknown");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}
