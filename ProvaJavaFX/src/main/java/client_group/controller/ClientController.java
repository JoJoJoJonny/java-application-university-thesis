package client_group.controller;

import client_group.model.Client;
import client_group.service.ClientService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;

public class ClientController {

    @FXML
    TableView<Client> clientTable;
    @FXML
    TableColumn<Client, String> pivaCol;
    @FXML
    TableColumn<Client, String> companyNameCol;
    @FXML
    TableColumn<Client, String> emailCol;
    @FXML
    TableColumn<Client, String> phoneCol;
    @FXML
    Label statusLabel;

    private final ClientService clientService;

    //Costruttore con injection
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    //Costruttore di default
    public ClientController() {
        this(new ClientService()); // se non si usa injection, fallback all'implementazione reale
    }

    @FXML
    public void initialize() {
        pivaCol.setCellValueFactory(new PropertyValueFactory<>("piva"));
        companyNameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        clientTable.setPlaceholder(new Label("No clients available"));
        loadClientList();
    }

    private void loadClientList() {
        new Thread(() -> {
            try {
                List<Client> clients = clientService.fetchAllClients();
                Platform.runLater(() -> {
                    clientTable.getItems().setAll(clients);
                    statusLabel.setText("Loaded " + clients.size() + " clients.");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error loading clients.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    void handleRefresh() {
        statusLabel.setText("Refreshing...");
        loadClientList();
    }

    @FXML
    void handleDelete() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a client to delete.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        new Thread(() -> {
            try {
                clientService.deleteClient(selected.getPiva());
                Platform.runLater(() -> {
                    clientTable.getItems().remove(selected);
                    statusLabel.setText("Deleted client with PIVA=" + selected.getPiva());
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error deleting client.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }
    @FXML
    void handleAdd() {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Add Client");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField pivaField = new TextField();
        TextField companyField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();

        pivaField.setPromptText("P.IVA");
        companyField.setPromptText("Company Name");
        emailField.setPromptText("Email");
        phoneField.setPromptText("Phone");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("P.IVA:"), 0, 0);
        grid.add(pivaField, 1, 0);
        grid.add(new Label("Company Name:"), 0, 1);
        grid.add(companyField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // validazione
        Runnable validate = () -> {
            boolean valid = !pivaField.getText().trim().isEmpty()
                    && !companyField.getText().trim().isEmpty();
            addButton.setDisable(!valid);
        };

        pivaField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        companyField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Client(
                        pivaField.getText().trim(),
                        companyField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(client -> {
            new Thread(() -> {
                try {
                    Client created = clientService.createClient(client);
                    Platform.runLater(() -> {
                        clientTable.getItems().add(created);
                        statusLabel.setText("Added client " + created.getCompanyName());
                        statusLabel.setStyle("-fx-text-fill: green;");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("Error while adding client.");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    });
                }
            }).start();
        });
    }

    @FXML
    void handleEdit() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a client to edit.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Edit Client");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField companyField = new TextField(selected.getCompanyName());
        TextField emailField = new TextField(selected.getEmail());
        TextField phoneField = new TextField(selected.getPhone());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Company Name:"), 0, 0);
        grid.add(companyField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Client(
                        selected.getPiva(),
                        companyField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            new Thread(() -> {
                try {
                    Client saved = clientService.updateClient(updated);
                    Platform.runLater(() -> {
                        int index = clientTable.getItems().indexOf(selected);
                        clientTable.getItems().set(index, saved);
                        statusLabel.setText("Updated client " + saved.getPiva());
                        statusLabel.setStyle("-fx-text-fill: green;");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("Error while updating client.");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    });
                }
            }).start();
        });
    }
}
