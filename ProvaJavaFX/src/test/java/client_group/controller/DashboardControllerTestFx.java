package client_group.controller;

import client_group.controller.DashboardController;
import client_group.model.Session;
import client_group.model.UserModel;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardControllerTestFX extends ApplicationTest {

    private DashboardController controller;

    @Override
    public void start(Stage stage) {
        controller = new DashboardController();
        controller.contentPane = new AnchorPane();
        controller.btnProfile = new Button();
        controller.btnEmployees = new Button();
        controller.btnGantt = new Button();
        controller.btnMachinery = new Button();
        controller.btnInventory = new Button();
        controller.btnClients = new Button();

        // dummy scene
        stage.setScene(new Scene(controller.btnProfile));
        stage.show();
    }

    @BeforeEach
    void setup() {
        controller = new DashboardController();

        // inizializza tutti i bottoni
        controller.btnProfile = new Button();
        controller.btnNoticeBoard = new Button();
        controller.btnEmployees = new Button();
        controller.btnGantt = new Button();
        controller.btnClients = new Button();
        controller.btnOrders = new Button();
        controller.btnModels = new Button();
        controller.btnProcesses = new Button();
        controller.btnMachinery = new Button();
        controller.btnInventory = new Button();

        controller.contentPane = new AnchorPane();
    }
    @Test
    void testInitializeEmployeeRole() {
        Session.getInstance().setCurrentUser(new UserModel("email", "Name", "Surname", "123", "EMPLOYEE"));

        controller.initialize();

        assertFalse(controller.btnEmployees.isManaged());
        assertFalse(controller.btnMachinery.isManaged());
        assertFalse(controller.btnInventory.isManaged());
        assertFalse(controller.btnClients.isManaged());
    }

    @Test
    void testInitializeManager() {
        Session.getInstance().setCurrentUser(new UserModel("m@x.com", "Max", "Manager", "123", "MANAGER"));
        Platform.runLater(controller::initialize);
        waitForFxEvents();

        assertTrue(controller.btnEmployees.isManaged());
        assertTrue(controller.btnMachinery.isManaged());
        assertTrue(controller.btnInventory.isManaged());
        assertTrue(controller.btnClients.isManaged());
        assertTrue(controller.btnGantt.isManaged());
    }

    @Test
    void testInitializeEmployee() {
        Session.getInstance().setCurrentUser(new UserModel("e@x.com", "Elena", "Emp", "456", "EMPLOYEE"));
        Platform.runLater(controller::initialize);
        waitForFxEvents();

        assertFalse(controller.btnEmployees.isManaged());
        assertFalse(controller.btnMachinery.isManaged());
        assertFalse(controller.btnInventory.isManaged());
        assertFalse(controller.btnClients.isManaged());
    }

    @Test
    void testInitializeAccountant() {
        Session.getInstance().setCurrentUser(new UserModel("a@x.com", "Alice", "Acc", "789", "ACCOUNTANT"));
        Platform.runLater(controller::initialize);
        waitForFxEvents();

        assertFalse(controller.btnGantt.isManaged());
        assertFalse(controller.btnProcesses.isManaged());
        assertFalse(controller.btnMachinery.isManaged());
    }

    @Test
    void testInitializeUnknownRole() {
        Session.getInstance().setCurrentUser(new UserModel("x@x.com", "Unknown", "User", "000", "UNKNOWN"));
        Platform.runLater(controller::initialize);
        waitForFxEvents();

        assertFalse(controller.btnEmployees.isManaged());
        assertFalse(controller.btnMachinery.isManaged());
        assertFalse(controller.btnInventory.isManaged());
        assertFalse(controller.btnClients.isManaged());
        assertFalse(controller.btnGantt.isManaged());
    }
    @Test
    void testHandleNoticeBoard() {
        Platform.runLater(controller::handleNoticeBoard);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }

    @Test
    void testHandleProfile() {
        Platform.runLater(controller::handleProfile);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    @Test
    void testHandleEmployees() {
        Platform.runLater(controller::handleEmployees);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    @Test
    void testHandleGantt() {
        Platform.runLater(controller::handleGantt);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    @Test
    void testHandleClients() {
        Platform.runLater(controller::handleClients);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    @Test
    void testHandleOrders() {
        Platform.runLater(controller::handleOrders);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    @Test
    void testHandleModels() {
        Platform.runLater(controller::handleModels);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    @Test
    void testHandleProcesses() {
        Platform.runLater(controller::handleProcesses);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    @Test
    void testHandleMachinery() {
        Platform.runLater(controller::handleMachinery);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    @Test
    void testHandleInventory() {
        Platform.runLater(controller::handleInventory);
        waitForFxEvents();
        assertNotNull(controller.contentPane.getChildren());
    }
    private void waitForFxEvents() {
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
    }
}
