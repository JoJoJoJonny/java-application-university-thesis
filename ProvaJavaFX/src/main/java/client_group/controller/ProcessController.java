package client_group.controller;

import client_group.dto.ModelWithStepsDTO;
import client_group.model.ProcessStep;
import client_group.model.Session;
import client_group.service.MachineryService;
import client_group.service.ProcessService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class ProcessController {
    ProcessService processService = new ProcessService();
    MachineryService machineryService = new MachineryService();


    @FXML
    VBox modelContainer;

    @FXML
    public void initialize() {
        try {
            List<ModelWithStepsDTO> models = processService.getAllModelWithSteps();

            for (ModelWithStepsDTO model : models) {
                VBox modelBox = createModelBox(
                        model.getName(),
                        model.getPrice() + "€",
                        model.getRaw().getMaterial(),
                        model.getRaw().getShape(),
                        model.getRaw().getSize()
                );

                HBox stepsHBox = new HBox(15);
                stepsHBox.setPadding(new Insets(10));
                stepsHBox.setPrefHeight(250); // altezza costante per non comprimere i stepBox
                stepsHBox.setFillHeight(false); // evita che crescano in altezza

                if (model.getProcessSteps().isEmpty()) {
                    Button addFirstStepButton = new Button("+ Add first step");

                    addFirstStepButton.setStyle(
                            "-fx-font-size: 14px;" +
                                    "-fx-background-color: #4CAF50;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-padding: 10 20 10 20;" +
                                    "-fx-background-radius: 10;"
                    );

                    //il tasto di aggiunta deve esserci solo se l'utente è manager
                    if(Session.getInstance().getCurrentUser().getRole().equals("MANAGER")){
                        addFirstStepButton.setOnAction(e -> {
                            handleAddFirstStep(model.getName(), stepsHBox, addFirstStepButton);
                        });

                        stepsHBox.getChildren().add(addFirstStepButton);
                    }

                } else {
                    for (ModelWithStepsDTO.ProcessStepDTO step : model.getProcessSteps()) {
                        // qui duration la usiamo come stringa già pronta
                        String durationStr = step.getDuration() != null ? String.valueOf(step.getDuration()) : "N/A";

                        StackPane stepBox = createStepBox(
                                step.getId(),
                                model.getName(),
                                step.getStepOrder(),
                                durationStr,
                                step.getSemifinishedName(),
                                step.getMachinery().getName(),
                                stepsHBox
                        );
                        stepsHBox.getChildren().add(stepBox);
                    }

                }
                    // ScrollPane per abilitare lo scroll orizzontale
                    ScrollPane scrollPane = new ScrollPane(stepsHBox);
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    scrollPane.setFitToHeight(true);
                    scrollPane.setFitToWidth(false);
                    scrollPane.setPannable(true); // permette il drag per scrollare

                    modelBox.getChildren().add(scrollPane);
                    modelContainer.getChildren().add(modelBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void handleAddFirstStep(String modelName, HBox stepsHBox, Button addFirstStepButton){
        setAllStepButtonsDisabled(true);
        stepsHBox.getChildren().clear(); // rimuove il bottone stesso

        VBox newStepBox = new VBox(5);
        newStepBox.setPadding(new Insets(20));
        newStepBox.setPrefWidth(300);
        newStepBox.setAlignment(Pos.CENTER_LEFT);
        newStepBox.setStyle("-fx-background-color: #eaf8e3; -fx-border-color: #a1d99b; -fx-border-radius: 20; -fx-background-radius: 20;");

        Label orderLabel = new Label("Step: 1");
        Label durationLabel = new Label("Duration:");
        HBox durationField = buildDurationHBox("PT0H0M0S");

        Label semiLabel = new Label("Semifinished:");
        TextField semiField = new TextField();

        Label machineLabel = new Label("Machinery:");
        ComboBox<String> machineComboBox = new ComboBox<>();
        try {
            machineComboBox.getItems().addAll(machineryService.getAllMachineryNames());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        newStepBox.getChildren().addAll(
                orderLabel,
                new HBox(5, durationLabel, durationField),
                new HBox(5, semiLabel, semiField),
                new HBox(5, machineLabel, machineComboBox)
        );

        StackPane newStepOverlay = new StackPane(newStepBox);
        newStepOverlay.setPadding(new Insets(10));
        newStepOverlay.setPrefSize(300, 150);

        StackPane confirmBtn = createCircleButton("check.png", "green");
        StackPane.setMargin(confirmBtn, new Insets(0, -230, -150, 0));
        confirmBtn.setId("confirmBtn");

        StackPane cancelBtn = createCircleButton("close.png", "red");
        StackPane.setMargin(cancelBtn, new Insets(0, -230, 150, 0));
        confirmBtn.setId("confirmBtn");

        newStepOverlay.getChildren().addAll(confirmBtn, cancelBtn);
        stepsHBox.getChildren().add(newStepOverlay);

        cancelBtn.setOnMouseClicked(ev -> {
            stepsHBox.getChildren().clear();
            stepsHBox.getChildren().add(addFirstStepButton);
            setAllStepButtonsDisabled(false);
        });

        confirmBtn.setOnMouseClicked(ev -> {
            @SuppressWarnings("unchecked")
            List<TextField> fields = (List<TextField>) durationField.getUserData();

            int h = Integer.parseInt(fields.get(0).getText());
            int m = Integer.parseInt(fields.get(1).getText());
            int s = Integer.parseInt(fields.get(2).getText());

            String newDuration = Duration.ofHours(h).plusMinutes(m).plusSeconds(s).toString();
            String newSemi = semiField.getText();
            String newMachine = machineComboBox.getValue();

            ProcessStep newStep = null;
            try{
                newStep = processService.addNewStepToModel(modelName, Duration.parse(newDuration), newSemi, newMachine, 0);
            }catch(Exception exception){
                exception.printStackTrace();
            }
            final Long newStepId = newStep!=null ?  newStep.getId() : null;

            // costruzione step definitivo
            VBox finalizedBox = new VBox(5);
            finalizedBox.setPadding(new Insets(20));
            finalizedBox.setPrefWidth(300);
            finalizedBox.setAlignment(Pos.CENTER_LEFT);
            finalizedBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 20; -fx-background-radius: 20;");

            finalizedBox.getChildren().addAll(
                    new Label("Step: 1"),
                    new Label("Duration: " + formatDurationForDisplay(newDuration)),
                    new Label("Semifinished: " + newSemi),
                    new Label("Machinery: " + newMachine)
            );

            StackPane finalizedOverlay = new StackPane(finalizedBox);
            finalizedOverlay.setPadding(new Insets(10));
            finalizedOverlay.setPrefSize(300, 150);

            StackPane newEdit = createCircleButton("edit.png", "blue");
            StackPane.setMargin(newEdit, new Insets(0, -230, 0, 0));
            StackPane.setAlignment(newEdit, Pos.TOP_RIGHT);

            StackPane newDelete = createCircleButton("delete.png", "red");
            StackPane.setMargin(newDelete, new Insets(0, -230, 150, 0));
            StackPane.setAlignment(newDelete, Pos.TOP_LEFT);

            StackPane newAdd = createCircleButton("add.png", "green");
            StackPane.setMargin(newAdd, new Insets(0, -230, -150, 0));
            StackPane.setAlignment(newAdd, Pos.BOTTOM_RIGHT);

            finalizedOverlay.getChildren().addAll(newEdit, newDelete, newAdd);

            newEdit.setOnMouseClicked(editEvent -> handleEditStep(newStepId, finalizedOverlay, modelName, newEdit, newDelete, newAdd));
            newDelete.setOnMouseClicked(delEvent -> handleDeleteStep(newStepId, finalizedOverlay, stepsHBox, modelName, 1, newEdit, newDelete, newAdd));
            newAdd.setOnMouseClicked(addEvent -> handleAddStep(finalizedOverlay, stepsHBox, 1, modelName, newEdit, newDelete, newAdd));

            Platform.runLater(() -> {
                stepsHBox.getChildren().clear();
                stepsHBox.getChildren().add(finalizedOverlay);
                setAllStepButtonsDisabled(false);
            });
        });
    }

    private VBox createModelBox(String modelName, String price, String material, String shape, String size) {
        VBox modelBox = new VBox(5);
        Label title = new Label("Model: " + modelName);
        Label info = new Label("Price: " + price + " | Material: " + material + ", Shape: " + shape + ", Size: " + size);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        modelBox.getChildren().addAll(title, info);
        return modelBox;
    }

    private StackPane createStepBox(Long stepId, String modelName, int order, String duration, String semiFinished, String machineName, HBox stepsHBox) {
        VBox stepBox = new VBox(5);
        stepBox.setPadding(new Insets(20));
        stepBox.setPrefWidth(304);
        stepBox.setAlignment(Pos.CENTER_LEFT);
        stepBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 20; -fx-background-radius: 20;");

        Label orderLabel = new Label("Step: " + order);
        Label durationLabel = new Label("Duration: " + formatDurationForDisplay(duration));
        Label semiLabel = new Label("Semifinished: " + semiFinished);
        Label machineLabel = new Label("Machinery: " + machineName);

        stepBox.getChildren().addAll(orderLabel, durationLabel, semiLabel, machineLabel);

        StackPane overlay = new StackPane(stepBox);
        overlay.setPadding(new Insets(10));
        overlay.setPrefSize(304, 150);
        overlay.setPickOnBounds(false);

        //solo il manager può vedere i tasti
        if(Session.getInstance().getCurrentUser().getRole().equals("MANAGER")) {
            // Modifica
            StackPane editCircle = createCircleButton("edit.png", "blue");
            StackPane.setMargin(editCircle, new Insets(0, -240, 0, 0));
            StackPane.setAlignment(editCircle, javafx.geometry.Pos.TOP_RIGHT);

            // Elimina
            StackPane deleteCircle = createCircleButton("delete.png", "red");
            StackPane.setMargin(deleteCircle, new Insets(0, -240, 150, 0));
            StackPane.setAlignment(deleteCircle, javafx.geometry.Pos.TOP_LEFT);

            // Aggiungi
            StackPane addCircle = createCircleButton("add.png", "green");
            StackPane.setMargin(addCircle, new Insets(0, -240, -150, 0));
            StackPane.setAlignment(addCircle, javafx.geometry.Pos.BOTTOM_RIGHT);

            overlay.getChildren().addAll(editCircle, deleteCircle, addCircle);


            // listener tasti
            deleteCircle.setOnMouseClicked(event -> handleDeleteStep(stepId, overlay, stepsHBox, modelName, order, editCircle, deleteCircle, addCircle));
            editCircle.setOnMouseClicked(event -> handleEditStep(stepId, overlay, modelName, editCircle, deleteCircle, addCircle));
            addCircle.setOnMouseClicked(event -> handleAddStep(overlay, stepsHBox, order, modelName, editCircle, deleteCircle, addCircle));
        }

        return overlay;
    }

    private StackPane createCircleButton(String imageName, String color) {
        Circle circle = new Circle(20);
        circle.setFill(Color.web(color));
        circle.setStroke(Color.BLACK);

        ImageView icon = new ImageView(getClass().getResource("/icons/" + imageName).toExternalForm());
        icon.setFitWidth(20);
        icon.setFitHeight(20);

        StackPane button = new StackPane(circle, icon);
        button.setStyle("-fx-cursor: hand;");

        //button.setOnMouseClicked(e -> System.out.println(imageName + " clicked")); //evento di prova
        button.setPickOnBounds(false); // permette il click solo sulla parte visibile
        button.setMouseTransparent(false); // assicura che riceva gli eventi mouse

        return button;
    }

    void handleDeleteStep(Long stepId, StackPane stepBox, HBox stepsHBox, String modelName, int order,
                          StackPane editCircle, StackPane deleteCircle, StackPane addCircle) {

        stepBox.getChildren().removeAll(editCircle, deleteCircle, addCircle);

        StackPane confirmBtn = createCircleButton("check.png", "green");
        StackPane.setMargin(confirmBtn, new Insets(0, -240, -150, 0));
        StackPane cancelBtn = createCircleButton("close.png", "red");
        StackPane.setMargin(cancelBtn, new Insets(0, -240, 150, 0));

        stepBox.getChildren().addAll(confirmBtn, cancelBtn);

        // annulla eliminazione
        cancelBtn.setOnMouseClicked(ev -> {
            stepBox.getChildren().removeAll(confirmBtn, cancelBtn);
            stepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);
        });

        // conferma eliminazione
        confirmBtn.setOnMouseClicked(ev -> {
            new Thread(() -> {
                try {
                    boolean deleted = processService.deleteById(stepId);  // ora usiamo solo l'id
                    if (deleted) {
                        Platform.runLater(() -> {
                            boolean removed = stepsHBox.getChildren().remove(stepBox);

                            if (stepsHBox.getChildren().isEmpty()) {
                                // ricompare il bottone per aggiungere il primo step
                                Button addFirstStepButton = new Button("+ Add first step");
                                addFirstStepButton.setStyle(
                                        "-fx-font-size: 14px;" +
                                                "-fx-background-color: #4CAF50;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 20 10 20;" +
                                                "-fx-background-radius: 10;"
                                );

                                // riuso il listener già fatto
                                addFirstStepButton.setOnAction(e -> {
                                    handleAddFirstStep(modelName, stepsHBox, addFirstStepButton);
                                });

                                stepsHBox.getChildren().add(addFirstStepButton);
                            } else {
                                // aggiorna label step order
                                for (int i = 0; i < stepsHBox.getChildren().size(); i++) {
                                    StackPane overlayPane = (StackPane) stepsHBox.getChildren().get(i);
                                    VBox innerBox = (VBox) overlayPane.getChildren().get(0);
                                    Label stepLabel = (Label) innerBox.getChildren().get(0);
                                    stepLabel.setText("Step: " + (i + 1));
                                }
                            }

                        });
                    } else {
                        System.err.println("Error while deleting step");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
    }

    void handleEditStep(Long stepId, StackPane stepBox, String modelName,
                        StackPane editCircle, StackPane deleteCircle, StackPane addCircle) {

        VBox innerBox = (VBox) stepBox.getChildren().get(0);

        // estrai i dati direttamente dalle Label esistenti nella UI
        String orderText = ((Label) innerBox.getChildren().get(0)).getText();

        String durationText = ((Label) innerBox.getChildren().get(1)).getText(); // per esempio "Duration: PT1H30M"
        String semiFinishedText = ((Label) innerBox.getChildren().get(2)).getText(); // pe esempio "Semifinished: semiName"
        String machineryText = ((Label) innerBox.getChildren().get(3)).getText(); // per esempèio "Machinery: machineName"

        // pulisci il prefisso per ottenere i valori "raw"
        String rawDuration = durationText.replace("Duration: ", "");

        final String durationISO;
        String durationISO1;
        try {
            java.time.Duration.parse(rawDuration);
            durationISO1 = rawDuration;
        } catch (Exception e) {
            durationISO1 = convertHMSStringToISO(rawDuration);
        }
        durationISO = durationISO1;
        String semiFinished = semiFinishedText.replace("Semifinished: ", "");
        String machinery = machineryText.replace("Machinery: ", "");

        // rimuovo i bottoni e mostro i campi modificabili
        stepBox.getChildren().removeAll(editCircle, deleteCircle, addCircle);
        innerBox.getChildren().clear();


        innerBox.getChildren().clear();

        Label orderLabel = new Label(orderText);

        orderLabel.setTextFill(Color.BLACK);

        Label durationLabel = new Label("Duration:");
        HBox durationField = buildDurationHBox(durationISO);


        Label semiLabel = new Label("Semifinished:");
        TextField semiField = new TextField(semiFinished);

        Label machineLabel = new Label("Machinery:");
        ComboBox<String> machineComboBox = new ComboBox<>();
        try {
            machineComboBox.getItems().addAll(machineryService.getAllMachineryNames());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        machineComboBox.setValue(machinery); // seleziona quello attuale


        innerBox.getChildren().addAll(orderLabel,
                new HBox(5, durationLabel, durationField),
                new HBox(5, semiLabel, semiField),
                new HBox(5, machineLabel, machineComboBox)
        );

        StackPane confirmBtn = createCircleButton("check.png", "green");
        StackPane.setMargin(confirmBtn, new Insets(0, -240, -150, 0));

        StackPane cancelBtn = createCircleButton("close.png", "red");
        StackPane.setMargin(cancelBtn, new Insets(0, -240, 150, 0));

        stepBox.getChildren().addAll(confirmBtn, cancelBtn);

        cancelBtn.setOnMouseClicked(e -> {
            // annulla: ripristina la visualizzazione originale
            stepBox.getChildren().removeAll(confirmBtn, cancelBtn);
            innerBox.getChildren().clear();

            innerBox.getChildren().addAll(
                    new Label(orderLabel.getText()),
                    new Label("Duration: " + formatDurationForDisplay(durationISO)),
                    new Label("Semifinished: " + semiFinished),
                    new Label("Machinery: " + machinery)
            );
            stepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);
        });

        confirmBtn.setOnMouseClicked(e -> {
            @SuppressWarnings("unchecked")
            List<TextField> fields = (List<TextField>) durationField.getUserData();

            int h = Integer.parseInt(fields.get(0).getText());
            int m = Integer.parseInt(fields.get(1).getText());
            int s = Integer.parseInt(fields.get(2).getText());

            String newDuration = java.time.Duration.ofHours(h)
                    .plusMinutes(m)
                    .plusSeconds(s)
                    .toString(); // sarà in formato ISO-8601 tipo "PT1H30M"

            String newSemi = semiField.getText();
            String newMachine = machineComboBox.getValue();

            new Thread(() -> {
                try {
                    Duration durationObj = Duration.ofHours(h).plusMinutes(m).plusSeconds(s);
                    boolean success = processService.updateStepById(stepId, durationObj, newSemi, newMachine);

                    if (success) {
                        Platform.runLater(() -> {
                            innerBox.getChildren().clear();
                            innerBox.getChildren().addAll(
                                    new Label(orderLabel.getText()),
                                    new Label("Duration: " + formatDurationForDisplay(newDuration)),
                                    new Label("Semifinished: " + newSemi),
                                    new Label("Machinery: " + newMachine)
                            );
                            stepBox.getChildren().removeAll(confirmBtn, cancelBtn);
                            stepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);
                        });
                    } else {
                        System.err.println("Error while updating");
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();

        });
    }

    private HBox createDurationInputFields(int hours, int minutes, int seconds) {
        TextField hourField = createNumericField(String.valueOf(hours));
        TextField minuteField = createNumericField(String.valueOf(minutes));
        TextField secondField = createNumericField(String.valueOf(seconds));

        Label colon1 = new Label(":");
        Label colon2 = new Label(":");

        HBox durationBox = new HBox(5, hourField, colon1, minuteField, colon2, secondField);
        durationBox.setAlignment(Pos.CENTER_LEFT);

        durationBox.setUserData(List.of(hourField, minuteField, secondField));
        return durationBox;
    }

    private TextField createNumericField(String initialValue) {
        TextField field = new TextField(initialValue);
        field.setPrefWidth(40);
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                field.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        return field;
    }

    private HBox buildDurationHBox(String durationStr) {
        try {
            java.time.Duration d = java.time.Duration.parse(durationStr);
            long hours = d.toHours();
            long minutes = d.toMinutesPart();
            long seconds = d.toSecondsPart();
            return createDurationInputFields((int) hours, (int) minutes, (int) seconds);
        } catch (Exception e) {
            // in caso di formato errato
            return createDurationInputFields(0, 0, 0);
        }
    }

    private String formatDurationForDisplay(String durationStr) {
        try {
            java.time.Duration d = java.time.Duration.parse(durationStr);
            long h = d.toHours();
            long m = d.toMinutesPart();
            long s = d.toSecondsPart();
            return String.format("%02d:%02d:%02d", h, m, s);
        } catch (Exception e) {
            return "00:00:00"; // fallback
        }
    }

    private String convertHMSStringToISO(String hms) {
        String[] parts = hms.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = Integer.parseInt(parts[2]);
        return "PT" + h + "H" + m + "M" + s + "S";
    }


    void handleAddStep(StackPane currentStepBox, HBox stepsHBox, int currentOrder, String modelName,
                       StackPane editCircle, StackPane deleteCircle, StackPane addCircle) {

        // disattiva temporaneamente i bottoni dello step corrente
        currentStepBox.getChildren().removeAll(editCircle, deleteCircle, addCircle);

        // disattiva tutti gli altri tasti per evitare casini
        setAllStepButtonsDisabled(true);

        VBox innerBox = (VBox) currentStepBox.getChildren().get(0);
        int insertIndex = stepsHBox.getChildren().indexOf(currentStepBox) + 1;

        // costruisci nuovo VBox per lo step vuoto
        VBox newStepBox = new VBox(5);
        newStepBox.setPadding(new Insets(20));
        newStepBox.setPrefWidth(300);
        newStepBox.setAlignment(Pos.CENTER_LEFT);
        newStepBox.setStyle("-fx-background-color: #eaf8e3; -fx-border-color: #a1d99b; -fx-border-radius: 20; -fx-background-radius: 20;");

        Label orderLabel = new Label("Step: " + (currentOrder + 1));
        Label durationLabel = new Label("Duration:");
        HBox durationField = buildDurationHBox("PT0H0M0S");

        Label semiLabel = new Label("Semifinished:");
        TextField semiField = new TextField();

        Label machineLabel = new Label("Machinery:");
        ComboBox<String> machineComboBox = new ComboBox<>();
        try {
            machineComboBox.getItems().addAll(machineryService.getAllMachineryNames());
        } catch (IOException e) {
            e.printStackTrace();
        }

        newStepBox.getChildren().addAll(
                orderLabel,
                new HBox(5, durationLabel, durationField),
                new HBox(5, semiLabel, semiField),
                new HBox(5, machineLabel, machineComboBox)
        );

        StackPane newStepOverlay = new StackPane(newStepBox);
        newStepOverlay.setPadding(new Insets(10));
        newStepOverlay.setPrefSize(300, 150);

        StackPane confirmBtn = createCircleButton("check.png", "green");
        StackPane.setMargin(confirmBtn, new Insets(0, -230, -150, 0));

        StackPane cancelBtn = createCircleButton("close.png", "red");
        StackPane.setMargin(cancelBtn, new Insets(0, -230, 150, 0));

        newStepOverlay.getChildren().addAll(confirmBtn, cancelBtn);

        // inserisci nella posizione giusta
        stepsHBox.getChildren().add(insertIndex, newStepOverlay);

        // evento annulla: rimuove lo step temporaneo e ripristina i pulsanti originali
        cancelBtn.setOnMouseClicked(e -> {
            stepsHBox.getChildren().remove(newStepOverlay);
            currentStepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);
            setAllStepButtonsDisabled(false);
        });

        // evento conferma: salva su backend
        confirmBtn.setOnMouseClicked(e -> {
            @SuppressWarnings("unchecked")
            List<TextField> fields = (List<TextField>) durationField.getUserData();

            int h = Integer.parseInt(fields.get(0).getText());
            int m = Integer.parseInt(fields.get(1).getText());
            int s = Integer.parseInt(fields.get(2).getText());

            String newDuration = Duration.ofHours(h).plusMinutes(m).plusSeconds(s).toString();
            String newSemi = semiField.getText();
            String newMachine = machineComboBox.getValue();

            ProcessStep newStep = null;
            try{
                newStep = processService.addNewStepToModel(modelName, Duration.parse(newDuration), newSemi, newMachine, currentOrder);
            }catch(Exception exception){
                exception.printStackTrace();
            }
            final Long newStepId = newStep!=null ?  newStep.getId() : null;

            // aggiorna i successivi stepOrder visualizzati
            for (int i = insertIndex + 1; i < stepsHBox.getChildren().size(); i++) {
                StackPane pane = (StackPane) stepsHBox.getChildren().get(i);
                VBox box = (VBox) pane.getChildren().get(0);
                Label label = (Label) box.getChildren().get(0);
                String currentText = label.getText(); // "Step: X"
                int newOrder = Integer.parseInt(currentText.replaceAll("\\D+", "")) + 1;
                label.setText("Step: " + newOrder);
            }

            // ricostruzione in versione "readonly"
            VBox finalizedBox = new VBox(5);
            finalizedBox.setPadding(new Insets(20));
            finalizedBox.setPrefWidth(300);
            finalizedBox.setAlignment(Pos.CENTER_LEFT);
            finalizedBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 20; -fx-background-radius: 20;");

            finalizedBox.getChildren().addAll(
                    new Label("Step: " + (currentOrder + 1)),
                    new Label("Duration: " + formatDurationForDisplay(newDuration)),
                    new Label("Semifinished: " + newSemi),
                    new Label("Machinery: " + newMachine)
            );

            StackPane finalizedOverlay = new StackPane(finalizedBox);
            finalizedOverlay.setPadding(new Insets(10));
            finalizedOverlay.setPrefSize(300, 150);

            // nuovi bottoni
            StackPane newEdit = createCircleButton("edit.png", "blue");
            StackPane.setMargin(newEdit, new Insets(0, -230, 0, 0));
            StackPane.setAlignment(newEdit, javafx.geometry.Pos.TOP_RIGHT);

            StackPane newDelete = createCircleButton("delete.png", "red");
            StackPane.setMargin(newDelete, new Insets(0, -230, 150, 0));
            StackPane.setAlignment(newDelete, javafx.geometry.Pos.TOP_LEFT);

            StackPane newAdd = createCircleButton("add.png", "green");
            StackPane.setMargin(newAdd, new Insets(0, -230, -150, 0));
            StackPane.setAlignment(newAdd, javafx.geometry.Pos.BOTTOM_RIGHT);

            finalizedOverlay.getChildren().addAll(newEdit, newDelete, newAdd);

            // aggiungi listeners anche al nuovo
            newEdit.setOnMouseClicked(ev -> handleEditStep(newStepId, finalizedOverlay, modelName, newEdit, newDelete, newAdd));
            newDelete.setOnMouseClicked(ev -> handleDeleteStep(newStepId, finalizedOverlay, stepsHBox, modelName, currentOrder + 1, newEdit, newDelete, newAdd));
            newAdd.setOnMouseClicked(ev -> handleAddStep(finalizedOverlay, stepsHBox, currentOrder + 1, modelName, newEdit, newDelete, newAdd));

            // sostituisci lo step temporaneo
            Platform.runLater(() -> {
                stepsHBox.getChildren().set(insertIndex, finalizedOverlay);
                currentStepBox.getChildren().addAll(editCircle, deleteCircle, addCircle);
            });

            // riabilita tutti gli altri tasti
            setAllStepButtonsDisabled(false);
        });
    }

    private void setAllStepButtonsDisabled(boolean disabled) {
        for (Node n : modelContainer.getChildren().filtered(n -> n instanceof VBox)) {
            VBox modelBox = (VBox) n;
            for (Node node : modelBox.getChildren())
                if (node instanceof ScrollPane scrollPane) {
                    HBox stepsHBox = (HBox) scrollPane.getContent();
                    for (Node stepNode : stepsHBox.getChildren())
                        if (stepNode instanceof StackPane stepOverlay)
                            for (Node btn : stepOverlay.getChildren())
                                if (btn instanceof StackPane circleBtn)
                                    circleBtn.setDisable(disabled);
                }
        }
    }


}
