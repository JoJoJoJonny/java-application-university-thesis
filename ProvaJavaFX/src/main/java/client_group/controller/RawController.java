package client_group.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import client_group.model.Raw;
import client_group.service.RawService;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;

public class RawController {

    @FXML
    TableView<Raw> rawTable;
    @FXML
    TableColumn<Raw, Long> idCol;
    @FXML
    TableColumn<Raw, String> shapeCol;
    @FXML
    TableColumn<Raw, String> materialCol;
    @FXML
    TableColumn<Raw, String> sizeCol;
    @FXML
    TableColumn<Raw, String> castingNumberCol;
    @FXML
    TableColumn<Raw, String> thicknessCol;
    @FXML
    Label statusLabel;

    private RawService rawService;

    public RawController() {
        this(new RawService()); // default
    }

    // costruttore per testing
    public RawController(RawService rawService) {
        this.rawService = rawService;
    }

    public RawService getRawService() {
        return rawService;
    }

    private void validateForm(TextField shapeField, TextField materialField, TextField shape, TextField material, TextField size, Node addButton) {
        boolean valid = !shape.getText().trim().isEmpty() &&
                !material.getText().trim().isEmpty() &&
                !size.getText().trim().isEmpty();
        addButton.setDisable(!valid);
    }

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        shapeCol.setCellValueFactory(new PropertyValueFactory<>("shape"));
        materialCol.setCellValueFactory(new PropertyValueFactory<>("material"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        castingNumberCol.setCellValueFactory(new PropertyValueFactory<>("CastingNumber"));
        thicknessCol.setCellValueFactory(new PropertyValueFactory<>("Thickness"));

        rawTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        rawTable.setPlaceholder(new Label("No items available"));
        loadRawList();
    }

    private void loadRawList() {
        new Thread(() -> {
            try {
                List<Raw> list = rawService.fetchAllRaw();
                Platform.runLater(() -> {
                    rawTable.getItems().setAll(list);
                    statusLabel.setText("Loaded " + list.size() + " items");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error loading data");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    void handleRefresh() {
        statusLabel.setText("Refreshing...");
        loadRawList();
    }

    @FXML
    void handleDelete() {
        Raw selected = rawTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an item to delete");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        new Thread(() -> {
            try {
                rawService.deleteRaw(selected.getId());
                Platform.runLater(() -> {
                    rawTable.getItems().remove(selected);
                    statusLabel.setText("Deleted item id=" + selected.getId());
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Delete failed");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    void handleAdd(ActionEvent event) {
        Dialog<Raw> dialog = new Dialog<>();
        dialog.setTitle("Add Raw Material");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // crea i campi input
        TextField shapeField = new TextField();
        shapeField.setPromptText("Shape");

        TextField materialField = new TextField();
        materialField.setPromptText("Material");

        TextField sizeField = new TextField();
        sizeField.setPromptText("Size");

        TextField castingNumberField = new TextField();
        castingNumberField.setPromptText("Casting Number");

        TextField thicknessField = new TextField();
        thicknessField.setPromptText("Thickness");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Shape:"), 0, 0);
        grid.add(shapeField, 1, 0);
        grid.add(new Label("Material:"), 0, 1);
        grid.add(materialField, 1, 1);
        grid.add(new Label("Size:"), 0, 2);
        grid.add(sizeField, 1, 2);
        grid.add(new Label("Casting Number:"), 0, 3);
        grid.add(castingNumberField, 1, 3);
        grid.add(new Label("Thickness:"), 0, 4);
        grid.add(thicknessField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // abilita/disabilita il pulsante in base ai dati
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        shapeField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(shapeField, materialField, sizeField, castingNumberField, thicknessField, addButton));
        materialField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(shapeField, materialField, sizeField, castingNumberField, thicknessField, addButton));
        sizeField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(shapeField, materialField, sizeField, castingNumberField, thicknessField, addButton));
        castingNumberField.textProperty().addListener((obs, oldVal, newVal) -> validateForm( shapeField, materialField, sizeField, castingNumberField, thicknessField, addButton));
        thicknessField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(shapeField, materialField, sizeField, castingNumberField, thicknessField, addButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Raw raw = new Raw();
                raw.setShape(shapeField.getText());
                raw.setMaterial(materialField.getText());
                raw.setSize(sizeField.getText());
                raw.setCastingNumber(castingNumberField.getText());
                raw.setThickness(thicknessField.getText());
                return raw;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(raw -> {
            new Thread(() -> {
                try {
                    Raw created = rawService.createRaw(raw);
                    Platform.runLater(() -> {
                        rawTable.getItems().add(created);
                        statusLabel.setText("Added Material with ID=" + created.getId());
                        statusLabel.setStyle("-fx-text-fill: green;");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("error while adding");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    });
                }
            }).start();
        });
    }

    @FXML
    void handleEdit() {
        Raw selected = rawTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a raw to edit");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Dialog<Raw> dialog = new Dialog<>();
        dialog.setTitle("Edit Raw Material");

        ButtonType updateButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // campi con valori precompilati
        TextField shapeField = new TextField(selected.getShape());
        TextField materialField = new TextField(selected.getMaterial());
        TextField sizeField = new TextField(selected.getSize());
        TextField castingNumberField = new TextField(selected.getCastingNumber());
        TextField thicknessField = new TextField(selected.getThickness());

        shapeField.setPromptText("Shape");
        materialField.setPromptText("Material");
        sizeField.setPromptText("Size");
        castingNumberField.setPromptText("Casting Number");
        thicknessField.setPromptText("Thickness");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Shape:"), 0, 0);
        grid.add(shapeField, 1, 0);
        grid.add(new Label("Material:"), 0, 1);
        grid.add(materialField, 1, 1);
        grid.add(new Label("Size:"), 0, 2);
        grid.add(sizeField, 1, 2);
        grid.add(new Label("Casting Number:"), 0, 3);
        grid.add(castingNumberField, 1, 3);
        grid.add(new Label("Thickness:"), 0, 4);
        grid.add(thicknessField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Node updateButton = dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.setDisable(false);

        // validazione campi
        shapeField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(shapeField, materialField, sizeField, castingNumberField, thicknessField, updateButton));
        materialField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(shapeField, materialField, sizeField, castingNumberField, thicknessField, updateButton));
        sizeField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(shapeField, materialField, sizeField, castingNumberField, thicknessField, updateButton));
        castingNumberField.textProperty().addListener((obs, oldVal, newVal) -> validateForm( shapeField, materialField, sizeField, castingNumberField, thicknessField, updateButton));
        thicknessField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(shapeField, materialField, sizeField, castingNumberField, thicknessField, updateButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                Raw updatedRaw = new Raw();
                updatedRaw.setId(selected.getId()); // importante!
                updatedRaw.setShape(shapeField.getText());
                updatedRaw.setMaterial(materialField.getText());
                updatedRaw.setSize(sizeField.getText());
                updatedRaw.setCastingNumber(castingNumberField.getText());
                updatedRaw.setThickness(thicknessField.getText());
                return updatedRaw;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedRaw -> {
            new Thread(() -> {
                try {
                    Raw updated = rawService.updateRaw(updatedRaw);
                    Platform.runLater(() -> {
                        int index = rawTable.getItems().indexOf(selected);
                        rawTable.getItems().set(index, updated);
                        statusLabel.setText("Edit Material with ID=" + updated.getId());
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
}
