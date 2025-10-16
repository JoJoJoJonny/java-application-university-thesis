package client_group.controller;

import client_group.dto.AssignedTaskDTO;
import client_group.dto.ProfileDTO;
import client_group.model.Session;
import client_group.service.ProfileService;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileControllerTestFX extends ApplicationTest {

    private ProfileController controller;
    private ProfileService mockProfileService;

    private ProfileDTO testProfile;

    @Override
    public void start(Stage stage) {
        mockProfileService = mock(ProfileService.class);
        controller = new ProfileController(() -> mockProfileService);

        // Setup UI base
        controller.emailLabel = new Label();
        controller.nameLabel = new Label();
        controller.surnameLabel = new Label();
        controller.phoneLabel = new Label();
        controller.statusLabel = new Label();
        controller.welcomeLabel = new Label();
        controller.assignedTaskLabel = new Label();

        controller.tasksTable = new TableView<>();
        controller.orderIdCol = new TableColumn<>();
        controller.machineryCol = new TableColumn<>();
        controller.stepCol = new TableColumn<>();
        controller.startCol = new TableColumn<>();
        controller.endCol = new TableColumn<>();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Profilo del test
        testProfile = new ProfileDTO();
        testProfile.setEmail("john.doe@test.com");
        testProfile.setName("John");
        testProfile.setSurname("Doe");
        testProfile.setPhone("1234567890");
        testProfile.setRole("EMPLOYEE");

        when(mockProfileService.getProfileByEmail("john.doe@test.com"))
                .thenReturn(testProfile);

        // Setup session utente loggato
        Session.getInstance().setCurrentUser(testProfile);
    }

    @Test
    void testInitializeLoadsProfileAndTasks() throws Exception {
        // Mock del profilo
        ProfileDTO testProfile = new ProfileDTO();
        testProfile.setEmail("john.doe@test.com");
        testProfile.setName("John");
        testProfile.setSurname("Doe");
        testProfile.setPhone("1234567890");
        testProfile.setRole("EMPLOYEE");

        when(mockProfileService.getProfileByEmail("john.doe@test.com")).thenReturn(testProfile);

        // Mock dei task
        AssignedTaskDTO task = new AssignedTaskDTO();
        task.setOrderId(1L);
        task.setMachineryName("MachineA");
        task.setStepIndex(2);
        task.setScheduledStart(LocalDate.now());
        task.setScheduledEnd(LocalDate.now().plusDays(1));

        when(mockProfileService.getAssignedTasks("john.doe@test.com")).thenReturn(List.of(task));

        // Mock Session
        Session.getInstance().setCurrentUser(testProfile);

        Platform.runLater(() -> controller.initialize());

        sleep(500);

        // Verifica caricamento profilo
        assertEquals("john.doe@test.com", controller.emailLabel.getText());
        assertEquals("John", controller.nameLabel.getText());
        assertEquals("Doe", controller.surnameLabel.getText());
        assertEquals("1234567890", controller.phoneLabel.getText());
        assertEquals("Here is your profile, John", controller.welcomeLabel.getText());

        // Verifica caricamento task
        assertEquals(1, controller.tasksTable.getItems().size());
        AssignedTaskDTO loaded = controller.tasksTable.getItems().get(0);
        assertEquals(1L, loaded.getOrderId());
        assertEquals("MachineA", loaded.getMachineryName());
        assertEquals(2, loaded.getStepIndex());
    }


    @Test
    void testInitializeAsNonEmployeeHidesTasks() {
        testProfile.setRole("MANAGER");
        Session.getInstance().setCurrentUser(testProfile);

        Platform.runLater(() -> controller.initialize());

        sleep(500);

        assertFalse(controller.tasksTable.isManaged(),
                "La tabella deve essere nascosta per MANAGER");
        assertFalse(controller.assignedTaskLabel.isManaged(),
                "Il label dei task deve essere nascosto per MANAGER");
    }

    @Test
    void testInitializeWithServiceException() throws Exception {
        // Mock lancia eccezione
        when(mockProfileService.getProfileByEmail(anyString()))
                .thenThrow(new RuntimeException("Service error"));

        Platform.runLater(() -> controller.initialize());

        sleep(500);

        assertEquals("Error while loading profile", controller.emailLabel.getText());
    }
    @Test
    void testHandleEditConfirm() throws Exception {
        // Setup profilo esistente
        ProfileDTO existingProfile = new ProfileDTO();
        existingProfile.setEmail("john.doe@test.com");
        existingProfile.setName("John");
        existingProfile.setSurname("Doe");
        existingProfile.setPhone("1234567890");
        existingProfile.setRole("EMPLOYEE");

        controller.currentProfile = existingProfile;
        Session.getInstance().setCurrentUser(existingProfile);

        // Mock updateProfile del servizio
        when(mockProfileService.updateProfile(any(ProfileDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Esegui handleEdit
        Platform.runLater(() -> controller.handleEdit());

        sleep(500);

        // Trova il dialog
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertNotNull(dialogPane);

        TextField nameField = (TextField) dialogPane.lookup(".text-field");
        TextField surnameField = lookup(".text-field").nth(1).query();
        TextField phoneField = lookup(".text-field").nth(2).query();

        // Modifica i valori
        clickOn(nameField).eraseText(nameField.getText().length()).write("Jane");
        clickOn(surnameField).eraseText(surnameField.getText().length()).write("Smith");
        clickOn(phoneField).eraseText(phoneField.getText().length()).write("5555555555");

        // Trova e clicca il pulsante Save
        Button saveButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                        .filter(bt -> bt.getText().equals("Save")).findFirst().get()
        );
        clickOn(saveButton);

        sleep(500);

        verify(mockProfileService, atLeastOnce()).updateProfile(any(ProfileDTO.class));

        // Verifica aggiornamento dati
        assertEquals("Jane", controller.nameLabel.getText());
        assertEquals("Smith", controller.surnameLabel.getText());
        assertEquals("5555555555", controller.phoneLabel.getText());
        assertEquals("Profile updated successfully", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());
    }

    @Test
    void testHandleDeleteNoProfile() throws Exception {
        controller.currentProfile = null;

        Platform.runLater(() -> controller.handleDelete());

        sleep(500);

        // Verifica che mostri messaggio di errore
        assertEquals("Profile not uploaded", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: red;", controller.statusLabel.getStyle());

        verify(mockProfileService, never()).deleteProfile(anyString());
    }



}
