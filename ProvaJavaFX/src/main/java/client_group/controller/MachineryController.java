package client_group.controller;

import client_group.model.Machinery;
import client_group.service.MachineryService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;

public class MachineryController {

    @FXML
    TableView<Machinery> machineryTable;
    @FXML private TableColumn<Machinery, Integer> idCol;
    @FXML private TableColumn<Machinery, String> nameCol;
    @FXML private TableColumn<Machinery, LocalDate> buyDateCol;
    @FXML private TableColumn<Machinery, String> yearManufactureCol;
    @FXML private TableColumn<Machinery, String> capacityCol;
    @FXML
    Label statusLabel;

    MachineryService machineryService;

    public MachineryController() {}




    @FXML
    public void initialize() {
        if (machineryService == null) {
            machineryService = new MachineryService();
        }
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        buyDateCol.setCellValueFactory(new PropertyValueFactory<>("buyDate"));
        yearManufactureCol.setCellValueFactory(new PropertyValueFactory<>("yearManufacture"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        machineryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        machineryTable.setPlaceholder(new Label("No Machinery Available"));
        loadMachineryList();
    }

    void loadMachineryList() {
        new Thread(() -> {
            try {
                List<Machinery> list = machineryService.loadAllMachinery();
                Platform.runLater(() -> {
                    machineryTable.getItems().setAll(list);
                    statusLabel.setText("Loaded " + list.size() + " Machinery");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error Loading");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    void handleRefresh() {
        statusLabel.setText("Loading...");
        loadMachineryList();
    }

    @FXML
    void handleDelete() {
        Machinery selected = machineryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a machine to delete");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        new Thread(() -> {
            try {
                machineryService.deleteMachinery(Math.toIntExact((long) selected.getId()));
                Platform.runLater(() -> {
                    machineryTable.getItems().remove(selected);
                    statusLabel.setText("Deleted Machinery ID=" + selected.getId());
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error while deleting");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    void handleAdd(ActionEvent event) {
        Dialog<Machinery> dialog = new Dialog<>();
        dialog.setTitle("Add Machinery");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Machinery Name");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Buy Date");

        TextField yearManufactureField = new TextField();
        yearManufactureField.setPromptText("Year");

        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Buy Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Year Manufacture:"), 0, 2);
        grid.add(yearManufactureField, 1, 2);
        grid.add(new Label("Capacity:"), 0, 3);
        grid.add(capacityField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, datePicker, yearManufactureField, capacityField, addButton));
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, datePicker, yearManufactureField, capacityField, addButton));
        yearManufactureField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, datePicker, yearManufactureField, capacityField, addButton));
        capacityField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, datePicker, yearManufactureField, capacityField, addButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Machinery machinery = new Machinery();
                machinery.setName(nameField.getText());
                machinery.setBuyDate(datePicker.getValue());
                machinery.setYearManufacture(yearManufactureField.getText());
                machinery.setCapacity(capacityField.getText());
                return machinery;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(machinery -> {
            new Thread(() -> {
                try {
                    machineryService.saveMachinery(machinery).ifPresent(saved -> {
                        Platform.runLater(() -> {
                            machineryTable.getItems().add(saved);
                            statusLabel.setText("Add Machinery with ID=" + saved.getId());
                            statusLabel.setStyle("-fx-text-fill: green;");
                        });
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("Error while adding");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    });
                }
            }).start();
        });
    }

    @FXML
    void handleEdit(ActionEvent event) {
        Machinery selected = machineryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a machinery to edit");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Dialog<Machinery> dialog = new Dialog<>();
        dialog.setTitle("Edit Machinery");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(selected.getName());
        DatePicker datePicker = new DatePicker(selected.getBuyDate());
        TextField yearManufactureField = new TextField();
        TextField capacityField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Buy Date"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Year Manufacture:"), 0, 2);
        grid.add(yearManufactureField, 1, 2);
        grid.add(new Label("Capacity:"), 0, 3);
        grid.add(capacityField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, datePicker, yearManufactureField, capacityField, saveButton));
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, datePicker, yearManufactureField, capacityField, saveButton));
        yearManufactureField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, datePicker, yearManufactureField, capacityField, saveButton));
        capacityField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, datePicker, yearManufactureField, capacityField, saveButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Machinery updated = new Machinery();
                updated.setId(selected.getId());
                updated.setName(nameField.getText());
                updated.setBuyDate(datePicker.getValue());
                updated.setYearManufacture(yearManufactureField.getText());
                updated.setCapacity(capacityField.getText());
                return updated;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedMachinery -> {
            new Thread(() -> {
                try {
                    machineryService.updateMachinery(updatedMachinery);
                    Platform.runLater(() -> {
                        int index = machineryTable.getItems().indexOf(selected);
                        machineryTable.getItems().set(index, updatedMachinery);
                        statusLabel.setText("Edited Machinery ID=" + updatedMachinery.getId());
                        statusLabel.setStyle("-fx-text-fill: green;");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("Error while editing");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    });
                }
            }).start();
        });
    }


    private void validateForm(TextField nameField, DatePicker datePicker, TextField yearManufactureField, TextField capacityField, Node addButton) {
        boolean valid = !nameField.getText().trim().isEmpty() && datePicker.getValue() != null;
        addButton.setDisable(!valid);
    }

}
