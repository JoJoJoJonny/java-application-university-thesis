package client_group.controller;

import client_group.dto.ModelWithStepsDTO;
import client_group.model.ProcessStep;
import client_group.model.Raw;
import client_group.model.Session;
import client_group.model.UserModel;
import client_group.service.MachineryService;
import client_group.service.ProcessService;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessControllerTest {

    ProcessController controller;
    ProcessService mockProcessService;
    MachineryService mockMachineryService;

    @BeforeEach
    void setup() throws Exception {
        new JFXPanel();

        mockProcessService = mock(ProcessService.class);
        mockMachineryService = mock(MachineryService.class);

        controller = new ProcessController() {
            {
                this.processService = mockProcessService;
                this.machineryService = mockMachineryService;
                this.modelContainer = new VBox();
            }
        };

        UserModel manager = new UserModel("admin@test.com", "Admin", "User", "123456789", "MANAGER");
        Session.getInstance().setCurrentUser(manager);
    }

    @Test
    void testInitializeWithNoSteps() throws Exception {
        ModelWithStepsDTO model = new ModelWithStepsDTO();
        model.setName("TestModel");
        model.setPrice(100.0);
        Raw raw = new Raw();
        raw.setMaterial("Steel");
        raw.setShape("Sheet");
        raw.setSize("Large");
        model.setRaw(raw);
        model.setProcessSteps(new ArrayList<>());

        when(mockProcessService.getAllModelWithSteps()).thenReturn(List.of(model));

        FxTestUtils.runAndWait(() -> controller.initialize());

        assertTrue(controller.modelContainer.getChildren().size() > 0);

        VBox modelBox = (VBox) controller.modelContainer.getChildren().get(0);


        ScrollPane scroll = null;
        for (var node : modelBox.getChildren()) {
            if (node instanceof ScrollPane) {
                scroll = (ScrollPane) node;
                break;
            }
        }
        assertTrue(scroll != null);

        HBox stepsHBox = (HBox) scroll.getContent();

        assertTrue(stepsHBox.getChildren().get(0) instanceof Button);
    }

    @Test
    void testInitializeWithSteps() throws Exception {
        ModelWithStepsDTO model = new ModelWithStepsDTO();
        model.setName("ModelWithSteps");
        model.setPrice(50.0);
        Raw raw = new Raw();
        raw.setMaterial("Plastic");
        raw.setShape("Block");
        raw.setSize("Small");
        model.setRaw(raw);

        ModelWithStepsDTO.ProcessStepDTO step = new ModelWithStepsDTO.ProcessStepDTO();
        step.setId(1L);
        step.setStepOrder(1);
        step.setSemifinishedName("Semi1");
        step.setDuration(Duration.ofMinutes(10));
        ModelWithStepsDTO.MachineryDTO machinery = new ModelWithStepsDTO.MachineryDTO();
        machinery.setName("Machine1");
        step.setMachinery(machinery);

        model.setProcessSteps(List.of(step));

        when(mockProcessService.getAllModelWithSteps()).thenReturn(List.of(model));

        FxTestUtils.runAndWait(() -> controller.initialize());

        assertTrue(controller.modelContainer.getChildren().size() > 0);

        VBox modelBox = (VBox) controller.modelContainer.getChildren().get(0);

        ScrollPane scroll = null;
        for (var node : modelBox.getChildren()) {
            if (node instanceof ScrollPane) {
                scroll = (ScrollPane) node;
                break;
            }
        }
        assertTrue(scroll != null);

        HBox stepsHBox = (HBox) scroll.getContent();

        assertTrue(stepsHBox.getChildren().get(0) instanceof StackPane);
    }

    @Test
    void testInitializeExceptionHandling() throws Exception {
        when(mockProcessService.getAllModelWithSteps()).thenThrow(new RuntimeException("Boom"));

        FxTestUtils.runAndWait(() -> {
            try {
                controller.initialize();
            } catch (Exception ignored) {
            }
        });
    }

    @Test
    void testHandleAddFirstStepConfirm() throws Exception {
        HBox stepsHBox = new HBox();
        Button addFirstStepButton = new Button("+ Add first step");

        // Mock machineryService e processService
        when(mockMachineryService.getAllMachineryNames()).thenReturn(List.of("Machine1"));
        ProcessStep mockStep = mock(ProcessStep.class);
        when(mockStep.getId()).thenReturn(1L);
        when(mockProcessService.addNewStepToModel(any(), any(), any(), any(), anyInt()))
                .thenReturn(mockStep);

        // Chiamata handleAddFirstStep
        FxTestUtils.runAndWait(() -> controller.handleAddFirstStep("TestModel", stepsHBox, addFirstStepButton));

        StackPane overlay = (StackPane) stepsHBox.getChildren().get(0);

        // Trova il confirm button tramite ID
        StackPane confirmBtn = (StackPane) overlay.lookup("#confirmBtn");
        assertTrue(confirmBtn != null);

        // Simula click sul confirm button
        FxTestUtils.runAndWait(() -> confirmBtn.getOnMouseClicked().handle(null));

        // Verifica che il metodo sia stato chiamato
        verify(mockProcessService, atLeastOnce())
                .addNewStepToModel(eq("TestModel"), any(), any(), any(), anyInt());

        assertTrue(stepsHBox.getChildren().size() == 1);
        assertTrue(stepsHBox.getChildren().get(0) instanceof StackPane);
    }

    @Test
    void testHandleDeleteStepConfirm() throws Exception {
        HBox stepsHBox = new HBox();
        StackPane stepBox = new StackPane();
        VBox innerBox = new VBox(new Label("Step: 1"));
        stepBox.getChildren().add(innerBox);

        StackPane editCircle = new StackPane();
        StackPane deleteCircle = new StackPane();
        StackPane addCircle = new StackPane();
        stepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);

        stepsHBox.getChildren().add(stepBox);

        when(mockProcessService.deleteById(1L)).thenReturn(true);

        FxTestUtils.runAndWait(() ->
                controller.handleDeleteStep(1L, stepBox, stepsHBox, "TestModel", 1,
                        editCircle, deleteCircle, addCircle)
        );

        // Trova il confirmBtn (StackPane diverso da edit/delete/add)
        StackPane confirmBtn = (StackPane) stepBox.getChildren().stream()
                .filter(n -> n instanceof StackPane && n != editCircle && n != deleteCircle && n != addCircle)
                .findFirst()
                .orElseThrow();

        // Simula click sul confirm
        FxTestUtils.runAndWait(() -> confirmBtn.getOnMouseClicked().handle(null));

        // Attendi la rimozione del stepBox
        long start = System.currentTimeMillis();
        while (stepsHBox.getChildren().contains(stepBox)) {
            Thread.sleep(10);
            if (System.currentTimeMillis() - start > 1000) break; // timeout 1s
        }

        // Dopo conferma, stepBox deve essere rimosso e deve comparire il Button per aggiungere il primo step
        assertFalse(stepsHBox.getChildren().contains(stepBox));
        assertTrue(stepsHBox.getChildren().get(0) instanceof Button);
    }
    @Test
    void testHandleEditStepCancel() throws Exception {
        // Preparazione HBox, stepBox e bottoni originali
        HBox stepsHBox = new HBox();
        StackPane stepBox = new StackPane();
        VBox innerBox = new VBox();
        stepBox.getChildren().add(innerBox);

        StackPane editCircle = new StackPane();
        StackPane deleteCircle = new StackPane();
        StackPane addCircle = new StackPane();
        stepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);

        // Popola innerBox con labels simili a quelle del metodo
        Label orderLabel = new Label("Step: 1");
        Label durationLabel = new Label("Duration: PT1H30M");
        Label semiLabel = new Label("Semifinished: Semi1");
        Label machineLabel = new Label("Machinery: Machine1");
        innerBox.getChildren().addAll(orderLabel, durationLabel, semiLabel, machineLabel);

        // Mock machineryService
        when(mockMachineryService.getAllMachineryNames()).thenReturn(List.of("Machine1", "Machine2"));

        FxTestUtils.runAndWait(() ->
                controller.handleEditStep(1L, stepBox, "TestModel", editCircle, deleteCircle, addCircle)
        );

        StackPane cancelBtn = (StackPane) stepBox.getChildren().get(stepBox.getChildren().size() - 1);
        StackPane confirmBtn = (StackPane) stepBox.getChildren().get(stepBox.getChildren().size() - 2);

        assertNotNull(cancelBtn);
        assertNotNull(confirmBtn);

        // Simula click su cancel
        FxTestUtils.runAndWait(() -> cancelBtn.getOnMouseClicked().handle(null));

        // Dopo cancel devono esserci di nuovo i bottoni originali
        assertTrue(stepBox.getChildren().contains(editCircle));
        assertTrue(stepBox.getChildren().contains(deleteCircle));
        assertTrue(stepBox.getChildren().contains(addCircle));

        // innerBox deve avere di nuovo le Label originali
        assertEquals(4, innerBox.getChildren().size());
        assertEquals("Step: 1", ((Label) innerBox.getChildren().get(0)).getText());
        assertEquals("Duration: 01:30:00", ((Label) innerBox.getChildren().get(1)).getText());
        assertEquals("Semifinished: Semi1", ((Label) innerBox.getChildren().get(2)).getText());
        assertEquals("Machinery: Machine1", ((Label) innerBox.getChildren().get(3)).getText());
    }
    @Test
    void testHandleAddStepConfirm() throws Exception {
        // Setup HBox con uno step esistente
        VBox existingBox = new VBox(new Label("Step: 1"));
        StackPane currentStepBox = new StackPane(existingBox);

        StackPane editCircle = new StackPane();
        StackPane deleteCircle = new StackPane();
        StackPane addCircle = new StackPane();
        currentStepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);

        HBox stepsHBox = new HBox(currentStepBox);

        // Mock machineryService e processService
        when(mockMachineryService.getAllMachineryNames()).thenReturn(List.of("Machine1"));
        ProcessStep mockStep = mock(ProcessStep.class);
        when(mockStep.getId()).thenReturn(2L);  // id del nuovo step
        when(mockProcessService.addNewStepToModel(any(), any(), any(), any(), anyInt()))
                .thenReturn(mockStep);

        // Chiamata handleAddStep
        FxTestUtils.runAndWait(() -> controller.handleAddStep(
                currentStepBox, stepsHBox, 1, "TestModel", editCircle, deleteCircle, addCircle
        ));

        StackPane newOverlay = (StackPane) stepsHBox.getChildren().get(1); // insertIndex = 1

        // Trova il confirm button
        StackPane confirmBtn = (StackPane) newOverlay.getChildren().stream()
                .filter(node -> node != newOverlay.getChildren().get(0)) // esclude VBox contenente i campi
                .findFirst()
                .orElseThrow();

        // Simula click sul confirm button
        FxTestUtils.runAndWait(() -> confirmBtn.getOnMouseClicked().handle(null));

        // Verifica il metodo venga chiamato
        verify(mockProcessService, atLeastOnce())
                .addNewStepToModel(eq("TestModel"), any(), any(), any(), eq(1));

        assertEquals(2, stepsHBox.getChildren().size());
        assertTrue(stepsHBox.getChildren().get(1) instanceof StackPane);

        // Controlla che currentStepBox abbia riottenuto i bottoni originali
        assertTrue(currentStepBox.getChildren().contains(editCircle));
        assertTrue(currentStepBox.getChildren().contains(deleteCircle));
        assertTrue(currentStepBox.getChildren().contains(addCircle));
    }

    @Test
    void testHandleAddStepCancel() throws Exception {
        // Setup con uno step esistente
        VBox existingBox = new VBox(new Label("Step: 1"));
        StackPane currentStepBox = new StackPane(existingBox);

        StackPane editCircle = new StackPane();
        StackPane deleteCircle = new StackPane();
        StackPane addCircle = new StackPane();
        currentStepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);

        HBox stepsHBox = new HBox(currentStepBox);

        // Mock machineryService
        when(mockMachineryService.getAllMachineryNames()).thenReturn(List.of("Machine1"));

        // Chiamata handleAddStep
        FxTestUtils.runAndWait(() -> controller.handleAddStep(
                currentStepBox, stepsHBox, 1, "TestModel", editCircle, deleteCircle, addCircle
        ));

        StackPane newOverlay = (StackPane) stepsHBox.getChildren().get(1);

        // Trova il cancel button
        StackPane cancelBtn = (StackPane) newOverlay.getChildren().stream()
                .filter(node -> node != newOverlay.getChildren().get(0))
                .skip(1)
                .findFirst()
                .orElseThrow();

        // Simula click sul cancel button
        FxTestUtils.runAndWait(() -> cancelBtn.getOnMouseClicked().handle(null));

        assertEquals(1, stepsHBox.getChildren().size());
        assertTrue(stepsHBox.getChildren().get(0) instanceof StackPane);

        assertTrue(currentStepBox.getChildren().contains(editCircle));
        assertTrue(currentStepBox.getChildren().contains(deleteCircle));
        assertTrue(currentStepBox.getChildren().contains(addCircle));
    }




}
