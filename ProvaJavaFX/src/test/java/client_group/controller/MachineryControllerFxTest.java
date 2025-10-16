package client_group.controller;

import client_group.controller.MachineryController;
import client_group.model.Machinery;
import client_group.service.MachineryService;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import static org.junit.jupiter.api.Assertions.*;
import org.testfx.service.query.PointQuery;

import java.awt.*;
import javafx.scene.control.Button;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MachineryControllerTestFX extends ApplicationTest {

    private MachineryController controller;
    private MachineryService mockService;

    private Machinery m1, m2;

    @Override
    public void start(Stage stage) {
        mockService = mock(MachineryService.class);
        controller = new MachineryController();
        controller.machineryService = mockService;

        controller.statusLabel = new Label();
        controller.machineryTable = new TableView<>();
    }

    @BeforeEach
    void setUp() {
        m1 = new Machinery();
        m1.setId(1);
        m1.setName("Machine1");
        m1.setBuyDate(LocalDate.of(2020,1,1));
        m1.setYearManufacture("2020");
        m1.setCapacity("100L");

        m2 = new Machinery();
        m2.setId(2);
        m2.setName("Machine2");
        m2.setBuyDate(LocalDate.of(2021,2,2));
        m2.setYearManufacture("2021");
        m2.setCapacity("200L");
    }

    @Test
    void testLoadMachineryListSuccess() throws Exception {
        when(mockService.loadAllMachinery()).thenReturn(List.of(m1, m2));

        Platform.runLater(() -> controller.loadMachineryList());
        Thread.sleep(500);

        assertEquals(2, controller.machineryTable.getItems().size());
        assertEquals("Loaded 2 Machinery", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());
    }

    @Test
    void testHandleDeleteSuccess() throws Exception {
        Platform.runLater(() -> {
            controller.machineryTable.getItems().add(m1);
            controller.machineryTable.getSelectionModel().select(m1);
        });
        Thread.sleep(200);

        doNothing().when(mockService).deleteMachinery(anyInt());

        Platform.runLater(() -> controller.handleDelete());
        Thread.sleep(500);

        assertFalse(controller.machineryTable.getItems().contains(m1));
        assertEquals("Deleted Machinery ID=1", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());
        verify(mockService, times(1)).deleteMachinery(1);
    }

    @Test
    void testHandleDeleteNoSelection() throws Exception {
        Platform.runLater(() -> {
            controller.machineryTable.getSelectionModel().clearSelection();
            controller.handleDelete();
        });
        Thread.sleep(200);

        assertEquals("Select a machine to delete", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: red;", controller.statusLabel.getStyle());
        verify(mockService, never()).deleteMachinery(anyInt());
    }
    @Test
    void testHandleAddDialog() throws Exception {
        Machinery machineryCreated = new Machinery();
        machineryCreated.setId(1);
        machineryCreated.setName("Excavator");
        machineryCreated.setBuyDate(LocalDate.of(2023, 5, 1));
        machineryCreated.setYearManufacture("2023");
        machineryCreated.setCapacity("500L");

        when(mockService.saveMachinery(any(Machinery.class))).thenReturn(Optional.of(machineryCreated));

        // Pulisce la tabella prima di iniziare
        Platform.runLater(() -> controller.machineryTable.getItems().clear());

        // Esegui il metodo reale
        Platform.runLater(() -> controller.handleAdd(null));
        sleep(500); // aspetta che il dialog sia pronto

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertNotNull(dialogPane);

        TextField nameField = lookup(".text-field").nth(0).query();        // name
        DatePicker datePicker = lookup(".date-picker").query();            // buyDate
        TextField yearField = lookup(".text-field").nth(1).query();        // yearManufacture
        TextField capacityField = lookup(".text-field").nth(2).query();    // capacity

        // Compila i campi
        clickOn(nameField).write("Excavator");

        Platform.runLater(() -> datePicker.setValue(LocalDate.of(2023, 5, 1)));
        sleep(200);

        clickOn(yearField).write("2023");
        clickOn(capacityField).write("500L");

        // Pulsante Add
        ButtonType addButtonType = dialogPane.getButtonTypes().stream()
                .filter(bt -> bt.getText().equals("Add"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Add button not found"));
        Button addButton = (Button) dialogPane.lookupButton(addButtonType);
        clickOn(addButton);
        sleep(500);

        // Verifica tabella e status
        assertEquals(1, controller.machineryTable.getItems().size());
        assertEquals(machineryCreated, controller.machineryTable.getItems().get(0));
        assertEquals("Add Machinery with ID=1", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());

        verify(mockService, times(1)).saveMachinery(any(Machinery.class));
    }
    @Test
    void testHandleEditDialog() throws Exception {
        // Machinery esistente nella tabella
        Machinery existing = new Machinery();
        existing.setId(1);
        existing.setName("Bulldozer");
        existing.setBuyDate(LocalDate.of(2020, 1, 1));
        existing.setYearManufacture("2020");
        existing.setCapacity("1000L");

        Platform.runLater(() -> controller.machineryTable.getItems().add(existing));
        sleep(200);

        // Mock del service
        doAnswer(invocation -> {
            Machinery updated = invocation.getArgument(0);
            return null;
        }).when(mockService).updateMachinery(any(Machinery.class));

        // Seleziona l'elemento da modificare
        Platform.runLater(() -> controller.machineryTable.getSelectionModel().select(existing));
        sleep(200);

        // Esegui handleEdit
        Platform.runLater(() -> controller.handleEdit(null));
        sleep(500);


        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertNotNull(dialogPane);


        GridPane grid = (GridPane) dialogPane.getContent();
        TextField nameField = (TextField) grid.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == 0 && node instanceof TextField)
                .findFirst().orElseThrow();
        DatePicker datePicker = (DatePicker) grid.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == 1 && node instanceof DatePicker)
                .findFirst().orElseThrow();
        TextField yearField = (TextField) grid.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == 2 && node instanceof TextField)
                .findFirst().orElseThrow();
        TextField capacityField = (TextField) grid.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == 3 && node instanceof TextField)
                .findFirst().orElseThrow();

        // Simula la modifica da parte dell'utente
        clickOn(nameField).eraseText(nameField.getText().length()).write("Excavator");
        Platform.runLater(() -> datePicker.setValue(LocalDate.of(2023, 5, 1)));
        sleep(200);
        clickOn(yearField).eraseText(yearField.getText().length()).write("2023");
        clickOn(capacityField).eraseText(capacityField.getText().length()).write("500L");


        ButtonType saveButtonType = dialogPane.getButtonTypes().stream()
                .filter(bt -> bt.getText().equals("Save"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Save button not found"));
        Node saveButton = dialogPane.lookupButton(saveButtonType);
        clickOn(saveButton);
        sleep(500);

        // Verifica che la tabella sia aggiornata e lo statusLabel sia corretto
        Machinery updated = controller.machineryTable.getItems().get(0);
        assertEquals("Excavator", updated.getName());
        assertEquals(LocalDate.of(2023, 5, 1), updated.getBuyDate());
        assertEquals("2023", updated.getYearManufacture());
        assertEquals("500L", updated.getCapacity());
        assertEquals("Edited Machinery ID=1", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());


        verify(mockService, times(1)).updateMachinery(any(Machinery.class));
    }





}
