package client_group.controller;

import client_group.model.Raw;
import client_group.service.RawService;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.awt.event.ActionEvent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class RawControllerTestFX extends ApplicationTest {

    private RawController controller;
    private RawService mockRawService;

    private Raw raw1, raw2;

    @Override
    public void start(Stage stage) {
        mockRawService = mock(RawService.class);

        // controller con il servizio mock
        controller = new RawController(mockRawService);

        // setup
        controller.rawTable = new TableView<>();
        controller.statusLabel = new Label();

        controller.idCol = new TableColumn<>("id");
        controller.shapeCol = new TableColumn<>("shape");
        controller.materialCol = new TableColumn<>("material");
        controller.sizeCol = new TableColumn<>("size");
        controller.castingNumberCol = new TableColumn<>("castingNumber");
        controller.thicknessCol = new TableColumn<>("thickness");
    }

    @BeforeEach
    void setUp() throws Exception {
        raw1 = new Raw(1L, "shape1", "material1", "size1", "thickness1", "cast1");
        raw2 = new Raw(2L, "shape2", "material2", "size2", "thickness2", "cast2");

        when(mockRawService.fetchAllRaw()).thenReturn(List.of(raw1, raw2));
        doNothing().when(mockRawService).deleteRaw(anyLong());
    }

    @Test
    void testHandleRefresh() throws Exception {
        Platform.runLater(() -> {
            controller.statusLabel.setText("");
            controller.handleRefresh();
            assertEquals("Refreshing...", controller.statusLabel.getText());
        });
    }




    @Test
    void testLoadRawListSuccess() throws Exception {
        Platform.runLater(() -> controller.handleRefresh());
        sleep(500);

        assertEquals(2, controller.rawTable.getItems().size());
        assertEquals("Loaded 2 items", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());
    }

    @Test
    void testHandleDeleteSuccess() throws Exception {
        Platform.runLater(() -> {
            controller.rawTable.getItems().add(raw1);
            controller.rawTable.getSelectionModel().select(raw1);
        });

        Platform.runLater(() -> controller.handleDelete());
        sleep(500);

        assertFalse(controller.rawTable.getItems().contains(raw1));
        assertEquals("Deleted item id=1", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());

        verify(mockRawService, times(1)).deleteRaw(1L);
    }

    @Test
    void testHandleDeleteNoSelection() throws Exception {
        Platform.runLater(() -> {
            controller.rawTable.getSelectionModel().clearSelection();
            controller.handleDelete();
        });
        sleep(200);

        assertEquals("Select an item to delete", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: red;", controller.statusLabel.getStyle());

        verify(mockRawService, never()).deleteRaw(anyLong());
    }
    @Test
    void testHandleAddDialog() throws Exception {
        Raw rawCreated = new Raw(100L, "shapeX", "materialX", "sizeX", "thicknessX", "castX");
        when(mockRawService.createRaw(any(Raw.class))).thenReturn(rawCreated);

        Platform.runLater(() -> controller.rawTable.getItems().clear());

        // Esegue HandleAdd
        Platform.runLater(() -> controller.handleAdd(null));
        sleep(500);

        // Lookup dei campi nel dialog
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertNotNull(dialogPane);

        TextField shapeField = lookup(".text-field").nth(0).query();
        TextField materialField = lookup(".text-field").nth(1).query();
        TextField sizeField = lookup(".text-field").nth(2).query();
        TextField castingField = lookup(".text-field").nth(3).query();
        TextField thicknessField = lookup(".text-field").nth(4).query();

        // Compila i campi
        clickOn(shapeField).write("shapeX");
        clickOn(materialField).write("materialX");
        clickOn(sizeField).write("sizeX");
        clickOn(castingField).write("castX");
        clickOn(thicknessField).write("thicknessX");

        // Preme il pulsante Add
        Button addButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                        .filter(bt -> bt.getText().equals("Add")).findFirst().get()
        );
        clickOn(addButton);
        sleep(500);

        // Verifica che il Raw sia stato aggiunto
        assertEquals(1, controller.rawTable.getItems().size());
        assertEquals(rawCreated, controller.rawTable.getItems().get(0));
        assertEquals("Added Material with ID=100", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());

        // Verifica chiamata al service
        verify(mockRawService, times(1)).createRaw(any(Raw.class));
    }
    @Test
    void testHandleEditDialog() throws Exception {
        // Raw esistente nella tabella
        Raw existingRaw = new Raw(1L, "shapeOld", "materialOld", "sizeOld", "thicknessOld", "castOld");
        Platform.runLater(() -> {
            controller.rawTable.getItems().clear();
            controller.rawTable.getItems().add(existingRaw);
            controller.rawTable.getSelectionModel().select(existingRaw);
        });
        sleep(500);

        // Mock del servizio di update
        Raw updatedRaw = new Raw(1L, "shapeNew", "materialNew", "sizeNew", "thicknessNew", "castNew");
        when(mockRawService.updateRaw(any(Raw.class))).thenReturn(updatedRaw);

        // Chiamata al metodo
        Platform.runLater(() -> controller.handleEdit());
        sleep(500);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertNotNull(dialogPane);

        // Campi del dialog
        TextField shapeField = lookup(".text-field").nth(0).query();
        TextField materialField = lookup(".text-field").nth(1).query();
        TextField sizeField = lookup(".text-field").nth(2).query();
        TextField castingField = lookup(".text-field").nth(3).query();
        TextField thicknessField = lookup(".text-field").nth(4).query();

        // Modifica i valori
        clickOn(shapeField).eraseText(shapeField.getText().length()).write("shapeNew");
        clickOn(materialField).eraseText(materialField.getText().length()).write("materialNew");
        clickOn(sizeField).eraseText(sizeField.getText().length()).write("sizeNew");
        clickOn(castingField).eraseText(castingField.getText().length()).write("castNew");
        clickOn(thicknessField).eraseText(thicknessField.getText().length()).write("thicknessNew");

        // Premi il pulsante Save
        Button saveButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                        .filter(bt -> bt.getText().equals("Save")).findFirst().get()
        );
        clickOn(saveButton);
        sleep(500);

        // Verifica che venga modificato
        assertEquals(1, controller.rawTable.getItems().size());
        Raw result = controller.rawTable.getItems().get(0);
        assertEquals(updatedRaw, result);
        assertEquals("Edit Material with ID=1", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());

        verify(mockRawService, times(1)).updateRaw(any(Raw.class));
    }



}
