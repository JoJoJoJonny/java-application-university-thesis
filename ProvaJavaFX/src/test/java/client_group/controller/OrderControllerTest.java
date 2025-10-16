package client_group.controller;

import client_group.model.*;
import client_group.service.ModelService;
import client_group.service.OrderService;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTestFX extends ApplicationTest {

    private OrderController controller;
    private OrderService mockOrderService;

    private Client client1;
    private Model model1;

    @Override
    public void start(Stage stage) {
        mockOrderService = mock(OrderService.class);
        controller = new OrderController(mockOrderService);

        // Setup UI di base
        controller.statusLabel = new Label();
        controller.orderTable = new TableView<>();
        controller.idColumn = new TableColumn<>();
        controller.deadlineColumn = new TableColumn<>();
        controller.quantityColumn = new TableColumn<>();
        controller.clientPivaColumn = new TableColumn<>();
        controller.modelNameColumn = new TableColumn<>();
        controller.createDateColumn = new TableColumn<>();
        controller.endDateColumn = new TableColumn<>();
        controller.startDateColumn = new TableColumn<>();
        controller.statusColumn = new TableColumn<>();

        controller.addButton = new Button();
        controller.editButton = new Button();
        controller.refreshButton = new Button();

        controller.orderTable.setItems(controller.orderList);
    }

    @BeforeEach
    void setUp() {

        client1 = new Client();
        client1.setPiva("12345678901");

        model1 = new Model();
        model1.setName("Model1");
    }

    @Test
    void testLoadOrderListSuccess() throws Exception {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setClient(client1);
        order1.setModel(model1);
        order1.setDeadline(LocalDate.now());
        order1.setQuantity(5);
        order1.setStatus(OrderStatus.CREATED);

        when(mockOrderService.fetchAll()).thenReturn(List.of(order1));

        // Chiamata loadOrderList
        Platform.runLater(() -> controller.loadOrderList());
        sleep(500);

        assertEquals(1, controller.orderTable.getItems().size());
        assertEquals("Loaded 1 orders", controller.statusLabel.getText());
        assertEquals("-fx-text-fill: green;", controller.statusLabel.getStyle());

        verify(mockOrderService, atLeastOnce()).fetchAll();
    }

    @Test
    void testInitializeSetsColumns() {
        Platform.runLater(() -> controller.initialize());
        sleep(500);

        assertNotNull(controller.idColumn.getCellValueFactory());
        assertNotNull(controller.deadlineColumn.getCellValueFactory());
        assertNotNull(controller.quantityColumn.getCellValueFactory());
        assertNotNull(controller.clientPivaColumn.getCellValueFactory());
        assertNotNull(controller.modelNameColumn.getCellValueFactory());
        assertNotNull(controller.statusColumn.getCellValueFactory());
    }

    @Test
    void testHandleAddConfirm() throws Exception {
        //Campi dell'order
        Order newOrder = new Order();
        newOrder.setId(1L);
        newOrder.setClient(client1);
        newOrder.setModel(model1);
        newOrder.setDeadline(LocalDate.now());
        newOrder.setCreateDate(LocalDate.now());
        newOrder.setQuantity(10);
        newOrder.setStatus(OrderStatus.CREATED);

        when(mockOrderService.createOrder(any(Order.class))).thenReturn(newOrder);

        Dialog<Order> mockDialog = mock(Dialog.class);
        when(mockDialog.showAndWait()).thenReturn(java.util.Optional.of(newOrder));
        controller.setOrderDialogFactory(() -> mockDialog);

        //Chiama handleAdd
        Platform.runLater(() -> controller.handleAdd());
        WaitForAsyncUtils.waitForFxEvents(); // assicura esecuzione di tutti i runLater

        //Verifica aggiunta ordine
        assertEquals(1, controller.orderTable.getItems().size());
        Order added = controller.orderTable.getItems().get(0);
        assertEquals(newOrder.getClient(), added.getClient());
        assertEquals(newOrder.getModel(), added.getModel());
        assertEquals(newOrder.getQuantity(), added.getQuantity());
        assertEquals(newOrder.getStatus(), added.getStatus());

        verify(mockOrderService, times(1)).createOrder(any(Order.class));
    }



    @Test
    void testHandleAddFailure() throws Exception {
        when(mockOrderService.createOrder(any(Order.class))).thenThrow(new RuntimeException("DB error"));

        Platform.runLater(() -> controller.handleAdd());

        Thread.sleep(500);

        //Se errore non viene aggiornata la lista
        assertTrue(controller.orderList.isEmpty());
    }
    @Test
    void testHandleEditDialog() throws Exception {
        //ordine esistente
        Client client = new Client("12345678901", "MyCompany", "email@company.com", "123456");
        Model model = new Model();
        model.setName("ModelA");

        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setClient(client);
        existingOrder.setModel(model);
        existingOrder.setQuantity(50);
        existingOrder.setStatus(OrderStatus.CREATED);
        existingOrder.setCreateDate(LocalDate.now());
        existingOrder.setDeadline(LocalDate.now().plusDays(5));

        // Inserisce i dati
        Platform.runLater(() -> {
            controller.orderTable.getItems().clear();
            controller.orderTable.getItems().add(existingOrder);
            controller.orderTable.getSelectionModel().select(existingOrder);
        });
        sleep(500);

        // Mock del servizio di update
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setClient(client);
        updatedOrder.setModel(model);
        updatedOrder.setQuantity(100);
        updatedOrder.setStatus(OrderStatus.IN_PRODUCTION);
        updatedOrder.setCreateDate(existingOrder.getCreateDate());
        updatedOrder.setDeadline(existingOrder.getDeadline());

        when(mockOrderService.updateOrder(any(Order.class))).thenReturn(updatedOrder);

        // Chiama il metodo reale
        Platform.runLater(() -> controller.handleEdit());
        sleep(500); // aspetta che il dialog sia pronto

        // Lookup del dialog
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertNotNull(dialogPane);

        // Trova i campi del dialog
        TextField quantityField = from(dialogPane).lookup(".text-field").nth(0).query();
        ComboBox<String> statusComboBox = from(dialogPane).lookup(".combo-box").query();

        // Modifica i valori
        clickOn(quantityField).eraseText(quantityField.getText().length()).write("100");

        ;

        // Premi il pulsante Save
        Button saveButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                        .filter(bt -> bt.getText().equals("Save")).findFirst().get()
        );
        clickOn(saveButton);

        sleep(500); // aspetta che il thread async aggiorni la TableView

        // Verifico i valori
        assertEquals(1, controller.orderTable.getItems().size());
        Order result = controller.orderTable.getItems().get(0);
        assertEquals(100, result.getQuantity());
        assertEquals(OrderStatus.IN_PRODUCTION, result.getStatus());

        verify(mockOrderService, times(1)).updateOrder(any(Order.class));
    }





}


