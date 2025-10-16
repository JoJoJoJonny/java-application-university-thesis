package client_group.controller;

import client_group.model.Client;
import client_group.model.Model;
import client_group.model.Order;
import client_group.service.ClientService;
import client_group.service.ModelService;
import client_group.service.OrderService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import client_group.model.OrderStatus;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class OrderController {

    //public ModelService modelService;
    @FXML
    TableView<Order> orderTable;
    @FXML
    TableColumn<Order, Long> idColumn;
    @FXML
    TableColumn<Order, LocalDate> deadlineColumn;
    @FXML
    TableColumn<Order, Integer> quantityColumn;
    @FXML
    TableColumn<Order, String> clientPivaColumn;
    @FXML
    TableColumn<Order, String> modelNameColumn;
    @FXML
    TableColumn<Order, LocalDate> createDateColumn;
    @FXML
    TableColumn<Order, LocalDate> endDateColumn;
    @FXML
    TableColumn<Order, LocalDateTime> startDateColumn;
    @FXML
    TableColumn<Order, String> statusColumn;
    @FXML
    Label statusLabel;



    @FXML
    Button addButton;
    @FXML
    Button editButton;
    @FXML
    Button refreshButton;
    @FXML
    Button inProductionButton;
    @FXML
    Button completeButton;
    @FXML
    Button cancelButton;

    OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderController() {
        this(new OrderService());
    }

    final ObservableList<Order> orderList = FXCollections.observableArrayList();

    // Factory iniettabile
    private Supplier<Dialog<Order>> orderDialogFactory = this::buildOrderDialog;

    public void setOrderDialogFactory(Supplier<Dialog<Order>> factory) {
        this.orderDialogFactory = factory;
    }


    @FXML
    public void initialize() {
        // imposta le factory delle colonne per la tabella
        idColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        deadlineColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDeadline()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        clientPivaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClient().getPiva()));
        modelNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getModel().getName()));
        createDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreateDate()));
        endDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEndDate()));
        startDateColumn.setCellValueFactory(cellData -> {
            LocalDate startDate = cellData.getValue().getStartDate();
            return new ReadOnlyObjectWrapper<>(startDate != null ? startDate.atStartOfDay() : null);
        });

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderTable.setPlaceholder(new Label("No orders available"));
        orderTable.setItems(orderList);

        loadOrderList();

        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
    }

    void loadOrderList() {
        new Thread(() -> {
            try {
                List<Order> orders = orderService.fetchAll();
                Platform.runLater(() -> {
                    orderList.setAll(orders);
                    statusLabel.setText("Loaded " + orders.size() + " orders");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Unable to load orders.");
                    statusLabel.setText("Failed to load orders");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    void handleAdd() {
        Dialog<Order> dialog = orderDialogFactory.get(); // usa la factory

        dialog.showAndWait().ifPresent(order -> {
            new Thread(() -> {
                try {
                    Order created = orderService.createOrder(order);
                    Platform.runLater(() -> {
                        orderList.add(created);
                        showAlert("Success", "Add order with ID: " + created.getId());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Error", "Error while creating order"));
                }
            }).start();
        });
    }

    Dialog<Order> buildOrderDialog() {
        Dialog<Order> dialog = new Dialog<>();
        dialog.setTitle("Add Order");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        DatePicker createDatePicker = new DatePicker();
        DatePicker deadlinePicker = new DatePicker();

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        ComboBox<Client> clientComboBox = new ComboBox<>();
        ComboBox<Model> modelComboBox = new ComboBox<>();
        ComboBox<String> statusComboBox = new ComboBox<>();

        List<Client> clients = new ClientService().fetchAllClients();
        List<Model> models = new ModelService().fetchAllModels();
        clientComboBox.getItems().addAll(clients);
        modelComboBox.getItems().addAll(models);

        clientComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Client client, boolean empty) {
                super.updateItem(client, empty);
                setText(empty || client == null ? null : client.getPiva());
            }
        });
        clientComboBox.setButtonCell(clientComboBox.getCellFactory().call(null));

        modelComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Model model, boolean empty) {
                super.updateItem(model, empty);
                setText(empty || model == null ? null : model.getName());
            }
        });
        modelComboBox.setButtonCell(modelComboBox.getCellFactory().call(null));

        statusComboBox.getItems().addAll("CREATED");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Create Date:"), 0, 0);
        grid.add(createDatePicker, 1, 0);
        grid.add(new Label("Deadline:"), 0, 1);
        grid.add(deadlinePicker, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Client (P.IVA):"), 0, 3);
        grid.add(clientComboBox, 1, 3);
        grid.add(new Label("Model Name:"), 0, 4);
        grid.add(modelComboBox, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusComboBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        Runnable validate = () -> {
            boolean valid = createDatePicker.getValue() != null &&
                    deadlinePicker.getValue() != null &&
                    !quantityField.getText().trim().isEmpty() &&
                    clientComboBox.getValue() != null &&
                    modelComboBox.getValue() != null &&
                    statusComboBox.getValue() != null;
            addButton.setDisable(!valid);
        };

        createDatePicker.valueProperty().addListener((obs, o, n) -> validate.run());
        deadlinePicker.valueProperty().addListener((obs, o, n) -> validate.run());
        quantityField.textProperty().addListener((obs, o, n) -> validate.run());
        clientComboBox.valueProperty().addListener((obs, o, n) -> validate.run());
        modelComboBox.valueProperty().addListener((obs, o, n) -> validate.run());
        statusComboBox.valueProperty().addListener((obs, o, n) -> validate.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Order order = new Order();
                    order.setCreateDate(createDatePicker.getValue());
                    order.setDeadline(deadlinePicker.getValue());
                    order.setQuantity(Integer.parseInt(quantityField.getText()));
                    order.setClient(clientComboBox.getValue());
                    order.setModel(modelComboBox.getValue());
                    order.setStatus(OrderStatus.valueOf(statusComboBox.getValue()));
                    return order;
                } catch (Exception e) {
                    showAlert("Error", "Invalid data " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }


    @FXML
    void handleEdit() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Missing selection", "Select a order to edit");
            return;
        }

        Dialog<Order> dialog = new Dialog<>();
        dialog.setTitle("Edit order");

        ButtonType updateButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // campi precompilati
        DatePicker createDatePicker = new DatePicker(selected.getCreateDate());
        DatePicker deadlinePicker = new DatePicker(selected.getDeadline());

        TextField quantityField = new TextField(String.valueOf(selected.getQuantity()));
        quantityField.setPromptText("Quantity");

        // mostriamo solo la partita IVA, disabilitata
        TextField clientPivaField = new TextField(selected.getClient().getPiva());
        clientPivaField.setDisable(true);

        ComboBox<Model> modelComboBox = new ComboBox<>();
        List<Model> models = new ModelService().fetchAllModels();
        modelComboBox.getItems().addAll(models);
        modelComboBox.setValue(selected.getModel());

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("CREATED", "IN_PRODUCTION", "COMPLETED", "CANCELLED");
        statusComboBox.setValue(selected.getStatus().name());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Create Date:"), 0, 0);
        grid.add(createDatePicker, 1, 0);
        grid.add(new Label("Deadline:"), 0, 1);
        grid.add(deadlinePicker, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Client P.IVA:"), 0, 3);
        grid.add(clientPivaField, 1, 3);
        grid.add(new Label("Model Name:"), 0, 4);
        grid.add(modelComboBox, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusComboBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Node updateButton = dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.setDisable(false);

        // Validazione minima
        Runnable validate = () -> {
            boolean valid = createDatePicker.getValue() != null &&
                    deadlinePicker.getValue() != null &&
                    !quantityField.getText().trim().isEmpty() &&
                    modelComboBox.getValue() != null &&
                    statusComboBox.getValue() != null;
            updateButton.setDisable(!valid);
        };

        createDatePicker.valueProperty().addListener((obs, o, n) -> validate.run());
        deadlinePicker.valueProperty().addListener((obs, o, n) -> validate.run());
        quantityField.textProperty().addListener((obs, o, n) -> validate.run());
        modelComboBox.valueProperty().addListener((obs, o, n) -> validate.run());
        statusComboBox.valueProperty().addListener((obs, o, n) -> validate.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    Order updated = new Order();
                    updated.setId(selected.getId());
                    updated.setCreateDate(createDatePicker.getValue());
                    updated.setDeadline(deadlinePicker.getValue());
                    updated.setQuantity(Integer.parseInt(quantityField.getText()));
                    updated.setClient(selected.getClient());
                    //updated.setModel(modelComboBox.getValue());
                    updated.setModel(modelComboBox.getSelectionModel().getSelectedItem());
                    //updated.getModel().setRaw(modelComboBox.getValue().getRaw());
                    //updated.getModel().getRaw().setId(modelComboBox.getValue().getRaw().getId());
                    updated.setStatus(OrderStatus.valueOf(statusComboBox.getValue()));
                    updated.setStartDate(selected.getStartDate());
                    updated.setEndDate(selected.getEndDate());
                    return updated;
                } catch (Exception e) {
                    showAlert("Error", "Invalid data " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedOrder -> {
            new Thread(() -> {
                try {
                    Order updated = orderService.updateOrder(updatedOrder);
                    Platform.runLater(() -> {
                        int index = orderTable.getItems().indexOf(selected);
                        orderTable.getItems().set(index, updated);
                        showAlert("Success", "Order updated with ID " + updated.getId());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Error", "Error while updating the order"));
                }
            }).start();
        });
    }

    @FXML
    private void handleRefresh() {
        refreshButton.setDisable(true);
        loadOrderList();
        Platform.runLater(() -> refreshButton.setDisable(false));
    }

    @FXML
    private void handleInProduction() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                orderService.updateOrderState(selectedOrder.getId(), "in-production");
                handleRefresh();
            } catch (IOException e) {
                showAlert("Error while putting in production", e.getMessage());
            }
        }
    }

    @FXML
    private void handleComplete() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                orderService.updateOrderState(selectedOrder.getId(), "complete");
                handleRefresh();
            } catch (IOException e) {
                showAlert("Error while completing the order", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                orderService.updateOrderState(selectedOrder.getId(), "cancel");
                handleRefresh();
            } catch (IOException e) {
                showAlert("Error while cancelling the order", e.getMessage());
            }
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
