package client_group.controller;

import client_group.controller.ModelController;
import client_group.model.Model;
import client_group.model.Raw;
import client_group.service.ModelService;
import client_group.service.RawService;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModelControllerAddEditTestFX extends ApplicationTest {

    private ModelController controller;
    private ModelService mockModelService;
    private RawService mockRawService;

    private Raw raw1, raw2;

    @Override
    public void start(Stage stage) {
        mockModelService = mock(ModelService.class);
        mockRawService = mock(RawService.class);
        controller = new ModelController(mockModelService, mockRawService);


        controller.statusLabel = new Label();
        controller.modelTable = new TableView<>();
        controller.modelTable.getItems().clear();
    }

    @BeforeEach
    void setUp() throws Exception {
        raw1 = new Raw(1L, "shape1", "material1", "size1", "thickness1", "cast1");
        raw2 = new Raw(2L, "shape2", "material2", "size2", "thickness2", "cast2");

        when(mockRawService.getAllRaws()).thenReturn(List.of(raw1, raw2));
        when(mockRawService.loadAllRaw()).thenReturn(List.of(raw1, raw2));
    }

    @Test
    void testHandleAddConfirm() throws IOException {
        Model savedModel = new Model();
        savedModel.setName("NewModel");
        savedModel.setPrice(10.0);
        savedModel.setRaw(raw1);

        when(mockModelService.saveModel(any(Model.class))).thenReturn(Optional.of(savedModel));

        // Chiamata al metodo
        Platform.runLater(() -> {
            try {
                controller.handleAdd(null);
            } catch (Exception e) {
                fail(e);
            }
        });

        sleep(500);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertNotNull(dialogPane);

        TextField nameField = (TextField) dialogPane.lookup(".text-field");
        TextField priceField = lookup(".text-field").nth(1).query();
        ComboBox<Raw> rawCombo = lookup(".combo-box").query();

        // Compilo i campi
        clickOn(nameField).write("NewModel");
        clickOn(priceField).write("10");
        clickOn(rawCombo).clickOn(raw1.getId().toString());

        Button addButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                        .filter(bt -> bt.getText().equals("Add")).findFirst().get()
        );
        clickOn(addButton);

        sleep(500);

        verify(mockModelService, atLeastOnce()).saveModel(any(Model.class));
        assertEquals(1, controller.modelTable.getItems().size());
        assertEquals("NewModel", controller.modelTable.getItems().get(0).getName());
        assertEquals("Added model: NewModel", controller.statusLabel.getText());
    }

    @Test
    void testHandleEditConfirm() throws IOException {
        Model existingModel = new Model();
        existingModel.setName("OldModel");
        existingModel.setPrice(5.0);
        existingModel.setRaw(raw1);

        controller.modelTable.getItems().add(existingModel);
        controller.modelTable.getSelectionModel().select(existingModel);

        doNothing().when(mockModelService).updateModel(any(Model.class));

        Platform.runLater(() -> {
            try {
                controller.handleEdit(null);
            } catch (Exception e) {
                fail(e);
            }
        });

        sleep(500);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertNotNull(dialogPane);

        TextField priceField = lookup(".text-field").query();
        ComboBox<Raw> rawCombo = lookup(".combo-box").query();

        // Modifica campi
        clickOn(priceField).eraseText(priceField.getText().length()).write("20");

        clickOn(rawCombo).clickOn(raw2.getId().toString());

        Button saveButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                        .filter(bt -> bt.getText().equals("Save")).findFirst().get()
        );
        clickOn(saveButton);

        sleep(500);

        verify(mockModelService, atLeastOnce()).updateModel(any(Model.class));
        assertEquals(1, controller.modelTable.getItems().size());
        Model updated = controller.modelTable.getItems().get(0);
        assertEquals(20.0, updated.getPrice());
        assertEquals(raw2, updated.getRaw());
        assertEquals("Edited model: OldModel", controller.statusLabel.getText());
    }
    @Test
    void testHandleRefresh() throws Exception {
        FxTestUtils.runAndWait(() -> {

            controller.statusLabel.setText("");

            // Chiama handleRefresh
            controller.handleRefresh();

            // Verifica che il testo cambi subito
            assertEquals("Loading...", controller.statusLabel.getText());
        });
    }
    @Test
    void testHandleDeleteSuccess() throws Exception {
        // Crea un Model di test
        Model testModel = new Model();
        testModel.setName("TestModel");
        testModel.setPrice(10.0);

        FxTestUtils.runAndWait(() -> {
            controller.modelTable.getItems().add(testModel);
            controller.modelTable.getSelectionModel().select(testModel);
        });

        doNothing().when(mockModelService).deleteModel("TestModel");

        // Esegui la delete
        FxTestUtils.runAndWait(() -> controller.handleDelete());

        // Aspetta che l’item venga rimosso
        boolean removed = false;
        for (int i = 0; i < 20; i++) {
            FxTestUtils.waitForRunLater();
            if (!controller.modelTable.getItems().contains(testModel)) {
                removed = true;
                break;
            }
            Thread.sleep(100);
        }

        assertTrue(removed, "Il modello non è stato rimosso dalla tabella");
        assertEquals("Deleted model: TestModel", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());


        verify(mockModelService, times(1)).deleteModel("TestModel");
    }

    @Test
    void testHandleDeleteNoSelection() throws Exception {
        FxTestUtils.runAndWait(() -> {
            controller.modelTable.getSelectionModel().clearSelection();
            controller.handleDelete();
        });

        assertEquals("Select a model to delete", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: red;", controller.statusLabel.getStyle());

        verify(mockModelService, never()).deleteModel(anyString());
    }




}
