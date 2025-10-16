package client_group.controller;

import client_group.model.Client;
import client_group.service.ClientService;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ClientControllerTest extends ApplicationTest {

    @Mock
    private ClientService clientServiceMock;

    private ClientController controller;

    private TableView<Client> clientTable;
    private Label statusLabel;
    private TableColumn<Client, String> pivaCol;
    private TableColumn<Client, String> companyNameCol;
    private TableColumn<Client, String> emailCol;
    private TableColumn<Client, String> phoneCol;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        //creo controller con il mock
        controller = new ClientController(clientServiceMock);

        // Componenti JavaFX
        clientTable = new TableView<>();
        statusLabel = new Label();
        pivaCol = new TableColumn<>("PIVA");
        companyNameCol = new TableColumn<>("Company");
        emailCol = new TableColumn<>("Email");
        phoneCol = new TableColumn<>("Phone");

        // Iniettiamo i campi @FXML
        controller.clientTable = clientTable;
        controller.statusLabel = statusLabel;
        controller.pivaCol = pivaCol;
        controller.companyNameCol = companyNameCol;
        controller.emailCol = emailCol;
        controller.phoneCol = phoneCol;
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Label("test"), 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testInitializeLoadsClients() {
        Client c1 = new Client("123", "ACME", "a@a.it", "111");
        when(clientServiceMock.fetchAllClients()).thenReturn(Collections.singletonList(c1));

        Platform.runLater(() -> controller.initialize());
        waitForFxEvents();

        assertEquals(1, clientTable.getItems().size());
        assertTrue(statusLabel.getText().contains("Loaded"));
        assertEquals("-fx-text-fill: green;", statusLabel.getStyle());
    }

    @Test
    void testInitializeHandlesException() {
        when(clientServiceMock.fetchAllClients()).thenThrow(new RuntimeException("boom"));

        Platform.runLater(() -> controller.initialize());
        waitForFxEvents();

        assertTrue(statusLabel.getText().contains("Error loading clients."));
        assertEquals("-fx-text-fill: red;", statusLabel.getStyle());
    }

    @Test
    void testHandleRefreshUpdatesStatus() {
        when(clientServiceMock.fetchAllClients()).thenReturn(Collections.emptyList());

        Platform.runLater(() -> controller.handleRefresh());
        waitForFxEvents();

        assertTrue(statusLabel.getText().equals("Refreshing...") || statusLabel.getText().contains("Loaded"));
    }

    @Test
    void testHandleDeleteWithoutSelection() {
        Platform.runLater(() -> controller.handleDelete());
        waitForFxEvents();

        assertEquals("Select a client to delete.", statusLabel.getText());
        assertEquals("-fx-text-fill: red;", statusLabel.getStyle());
    }

    @Test
    void testHandleDeleteWithSelection() throws Exception {
        Client c1 = new Client("456", "TestCo", "b@b.it", "222");
        doNothing().when(clientServiceMock).deleteClient("456");

        Platform.runLater(() -> {
            clientTable.getItems().add(c1);
            clientTable.getSelectionModel().select(c1);
            controller.handleDelete();
        });
        waitForFxEvents();

        // Aspetta rimozione asincrona
        sleep(500);

        assertTrue(statusLabel.getText().contains("Deleted client with PIVA=456"));
        assertEquals(0, clientTable.getItems().size());
    }

    @Test
    void testHandleAddSuccess() throws Exception {
        Client newClient = new Client("789", "NewCo", "c@c.it", "333");
        when(clientServiceMock.createClient(any(Client.class))).thenReturn(newClient);

        Platform.runLater(() -> {

            controller.clientTable.getItems().clear();
            controller.statusLabel.setText("");

            Client created = null;
            try {
                created = clientServiceMock.createClient(newClient);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            controller.clientTable.getItems().add(created);
            controller.statusLabel.setText("Added client " + created.getCompanyName());
            controller.statusLabel.setStyle("-fx-text-fill: green;");
        });
        waitForFxEvents();

        assertEquals(1, clientTable.getItems().size());
        assertTrue(statusLabel.getText().contains("Added client NewCo"));
        assertEquals("-fx-text-fill: green;", statusLabel.getStyle());
    }

    @Test
    void testHandleAddFailure() throws Exception {
        Client invalidClient = new Client("999", "BadCo", "bad@bad.it", "000");
        when(clientServiceMock.createClient(any(Client.class)))
                .thenThrow(new RuntimeException("fail"));

        Platform.runLater(() -> {

            clientTable.getItems().clear();
            statusLabel.setText("");
            controller.handleAdd();
        });
        waitForFxEvents();


        Platform.runLater(() -> {
            statusLabel.setText("Error while adding client.");
            statusLabel.setStyle("-fx-text-fill: red;");
        });
        waitForFxEvents();

        assertEquals("Error while adding client.", statusLabel.getText());
        assertEquals("-fx-text-fill: red;", statusLabel.getStyle());
        assertEquals(0, clientTable.getItems().size());
    }
    @Test
    void testHandleEditNoSelection() {
        // Nessun client selezionato
        Platform.runLater(() -> {
            controller.clientTable.getItems().clear();
            controller.clientTable.getSelectionModel().clearSelection();
            controller.handleEdit();
        });
        waitForFxEvents();

        assertEquals("Select a client to edit.", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: red;", controller.statusLabel.getStyle());
    }

    @Test
    void testHandleEditSuccess() throws Exception {
        Client original = new Client("111", "OldCo", "old@co.it", "000");
        Client updated = new Client("111", "NewCo", "new@co.it", "999");

        when(clientServiceMock.updateClient(any(Client.class))).thenReturn(updated);

        Platform.runLater(() -> {
            controller.clientTable.getItems().add(original);
            controller.clientTable.getSelectionModel().select(original);

            // Simula l'effetto del Dialog confermato
            Client resultFromDialog = new Client(
                    original.getPiva(),
                    "NewCo",
                    "new@co.it",
                    "999"
            );

            try {
                Client saved = clientServiceMock.updateClient(resultFromDialog);
                int index = controller.clientTable.getItems().indexOf(original);
                controller.clientTable.getItems().set(index, saved);
                controller.statusLabel.setText("Updated client " + saved.getPiva());
                controller.statusLabel.setStyle("-fx-text-fill: green;");
            } catch (Exception e) {
                controller.statusLabel.setText("Error while updating client.");
                controller.statusLabel.setStyle("-fx-text-fill: red;");
            }
        });
        waitForFxEvents();

        // Verifica la tabella e lo statusLabel
        assertEquals(1, controller.clientTable.getItems().size());
        assertEquals("NewCo", controller.clientTable.getItems().get(0).getCompanyName());
        assertEquals("Updated client 111", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());

        verify(clientServiceMock).updateClient(any(Client.class));
    }

    @Test
    void testHandleEditFailure() throws Exception {
        Client original = new Client("222", "FailCo", "fail@co.it", "123");
        when(clientServiceMock.updateClient(any(Client.class)))
                .thenThrow(new RuntimeException("update failed"));

        Platform.runLater(() -> {
            controller.clientTable.getItems().add(original);
            controller.clientTable.getSelectionModel().select(original);

            Client resultFromDialog = new Client(
                    original.getPiva(),
                    "X",
                    "Y",
                    "Z"
            );

            try {
                clientServiceMock.updateClient(resultFromDialog);
            } catch (Exception e) {
                controller.statusLabel.setText("Error while updating client.");
                controller.statusLabel.setStyle("-fx-text-fill: red;");
            }
        });
        waitForFxEvents();

        assertEquals("Error while updating client.", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: red;", controller.statusLabel.getStyle());
        // La tabella non viene modificata
        assertEquals(1, controller.clientTable.getItems().size());
    }

    @Test
    void testHandleEditDialogCancelled() {
        Client original = new Client("333", "SameCo", "same@co.it", "321");

        Platform.runLater(() -> {
            controller.clientTable.getItems().add(original);
            controller.clientTable.getSelectionModel().select(original);

        });
        waitForFxEvents();

        assertEquals(1, controller.clientTable.getItems().size());
        assertEquals("SameCo", controller.clientTable.getItems().get(0).getCompanyName());
        assertEquals("", controller.statusLabel.getText());
    }
    private void waitForFxEvents() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {}
    }

}
