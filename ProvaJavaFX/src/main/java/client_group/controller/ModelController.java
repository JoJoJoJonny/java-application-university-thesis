package client_group.controller;

import client_group.model.Model;
import client_group.model.Raw;
import client_group.service.ModelService;
import client_group.service.RawService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class ModelController {

    @FXML
    public TableView<Model> modelTable;
    @FXML
    TableColumn<Model, String> nameCol;
    @FXML
    TableColumn<Model, Double> priceCol;
    @FXML
    TableColumn<Model, Long> rawIdCol;
    @FXML
    public Label statusLabel;

    private final ModelService modelService;
    private final RawService rawService;

    public ModelController(ModelService modelService, RawService rawService) {
        this.modelService = modelService;
        this.rawService = rawService;
    }

    public ModelController() {
        this(new ModelService(), new RawService());
    }



    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        rawIdCol.setCellValueFactory(cell -> {
            Raw raw = cell.getValue().getRaw();
            return new javafx.beans.property.SimpleObjectProperty<>(raw != null ? raw.getId() : null);
        });

        modelTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        modelTable.setPlaceholder(new Label("No model available"));

        loadModelList();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    void loadModelList() {
        new Thread(() -> {
            try {
                List<Model> models = modelService.loadAllModels();
                Platform.runLater(() -> {
                    modelTable.getItems().setAll(models);
                    statusLabel.setText("Loaded " + models.size() + " models");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error while loading");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    void handleRefresh() {
        statusLabel.setText("Loading...");
        loadModelList();
    }

    @FXML
    public void handleDelete() {
        Model selected = modelTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a model to delete");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        new Thread(() -> {
            try {
                modelService.deleteModel(selected.getName());
                Platform.runLater(() -> {
                    modelTable.getItems().remove(selected);
                    statusLabel.setText("Deleted model: " + selected.getName());
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
    public void handleAdd(ActionEvent event) throws Exception {
        Dialog<Model> dialog = new Dialog<>();
        dialog.setTitle("Add Model");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField priceField = new TextField();
        ComboBox<Raw> rawCombo = new ComboBox<>();

        List<Raw> raws = rawService.getAllRaws();

        rawCombo.getItems().addAll(raws);
        //rawCombo.getItems().addAll(rawService.loadAllRaw());


        // serve per far vedere i Raw come id e non come oggetti
        rawCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Raw raw, boolean empty) {
                super.updateItem(raw, empty);
                setText(empty || raw == null ? null : raw.getId().toString());
            }
        });
        rawCombo.setButtonCell(rawCombo.getCellFactory().call(null));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Raw ID:"), 0, 2);
        grid.add(rawCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, priceField, rawCombo, addButton));
        priceField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, priceField, rawCombo, addButton));
        rawCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(nameField, priceField, rawCombo, addButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Model model = new Model();
                model.setName(nameField.getText());
                model.setPrice(Double.parseDouble(priceField.getText()));
                model.setRaw(rawCombo.getValue());
                return model;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(model -> {
            new Thread(() -> {
                try {
                    modelService.saveModel(model).ifPresent(saved -> {
                        Platform.runLater(() -> {
                            modelTable.getItems().add(saved);
                            statusLabel.setText("Added model: " + saved.getName());
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
    void handleEdit(ActionEvent event) throws Exception {
        Model selected = modelTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a model to edit");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Dialog<Model> dialog = new Dialog<>();
        dialog.setTitle("Edit model");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField priceField = new TextField(String.valueOf(selected.getPrice()));
        ComboBox<Raw> rawCombo = new ComboBox<>();
        rawCombo.getItems().addAll(rawService.loadAllRaw());
        //rawCombo.setValue(selected.getRaw());

        // serve per far vedere i Raw come id e non come oggetti
        rawCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Raw raw, boolean empty) {
                super.updateItem(raw, empty);
                setText(empty || raw == null ? null : raw.getId().toString());
            }
        });
        rawCombo.setButtonCell(rawCombo.getCellFactory().call(null));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Price:"), 0, 0);
        grid.add(priceField, 1, 0);
        grid.add(new Label("Raw ID:"), 0, 1);
        grid.add(rawCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        priceField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(new TextField("dummy"), priceField, rawCombo, saveButton));
        rawCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(new TextField("dummy"), priceField, rawCombo, saveButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Model updated = new Model();
                updated.setName(selected.getName()); // chiave primaria non cambia
                updated.setPrice(Double.parseDouble(priceField.getText()));
                updated.setRaw(rawCombo.getValue());
                return updated;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedModel -> {
            new Thread(() -> {
                try {
                    modelService.updateModel(updatedModel);
                    Platform.runLater(() -> {
                        int index = modelTable.getItems().indexOf(selected);
                        modelTable.getItems().set(index, updatedModel);
                        statusLabel.setText("Edited model: " + updatedModel.getName());
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

    void validateForm(TextField nameField, TextField priceField, ComboBox<Raw> rawCombo, Node button) {
        boolean valid = !priceField.getText().trim().isEmpty() && rawCombo.getValue() != null;
        if (nameField.getText() != null)
            valid = valid && !nameField.getText().trim().isEmpty();
        try {
            Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            valid = false;
        }
        button.setDisable(!valid);
    }
}
