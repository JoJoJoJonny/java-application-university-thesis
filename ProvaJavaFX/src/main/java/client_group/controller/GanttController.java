package client_group.controller;

import client_group.dto.GanttBlockDTO;
import client_group.dto.NoticeDTO;
import client_group.model.*;
import client_group.service.EmployeeListService;
import client_group.service.GanttService;
import client_group.service.NoticeService;
import com.flexganttfx.model.Activity;
import com.flexganttfx.model.ActivityRef;
import com.flexganttfx.model.Layer;
import com.flexganttfx.model.Row;
import com.flexganttfx.model.layout.GanttLayout;
import com.flexganttfx.view.GanttChart;
import com.flexganttfx.view.graphics.ActivityEvent;
import com.flexganttfx.view.graphics.GraphicsBase;
import com.flexganttfx.view.graphics.renderer.ActivityBarRenderer;
import com.flexganttfx.view.timeline.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GanttController {
    @FXML
    AnchorPane contentPane;
    @FXML
    private Button filterButton;
    @FXML
    ScrollPane ganttScrollPane;

    private Button confirmButton;
    private Button cancelButton;

    private final List<BlockActivity> modifiedActivities = new ArrayList<>();

    Set<Long> selectedOrderIds = new HashSet<>();

    GanttService ganttService = new GanttService();

    final EmployeeListService employeeListService = new EmployeeListService();

    private final NoticeService noticeService = new NoticeService();

    @FXML
    public void initialize() {
        //loadGanttForAllOrders();
        loadAndShowAllOrders();
    }

    /* //versione prima del tasto coi filtri ordine
    private void loadGanttForAllOrders() {
        try {
            List<GanttBlockDTO> data = ganttService.getGanttForAllOrders();
            GanttChart<MachineRow> ganttChart = buildChartFromData(data);
            showGantt(ganttChart);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    void loadAndShowAllOrders() {
        try {
            List<GanttBlockDTO> data = ganttService.getGanttForAllOrders();
            if (!selectedOrderIds.isEmpty()) {
                data = data.stream()
                        .filter(dto -> selectedOrderIds.contains(dto.getOrderId()))
                        .collect(Collectors.toList());
            }
            GanttChart<MachineRow> ganttChart = buildChartFromData(data);
            showGanttWithFilterButton(ganttChart);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    GanttChart<MachineRow> buildChartFromData(List<GanttBlockDTO> data) {
        GanttChart<MachineRow> ganttChart = new GanttChart<>(new MachineRow("ROOT"));

        GraphicsBase<?> graphics = ganttChart.getGraphics();

        //tutti i ruoli posso fare drag o resize
        //solo i manager possono però confermare le modifiche, e quindi effettivamente applicarle
        //non ho trovato il modo di impedire il drag/resize in quanto il BlockActivity è definito come Mutable e aggiunge in automatico i listener
        if(Session.getInstance().getCurrentUser().getRole().equals("MANAGER")) {
            graphics.setOnActivityChangeFinished(evt -> handleActivityUpdate(evt));
        }


        Layer layer = new Layer("Process Steps");
        ganttChart.getLayers().add(layer);

        Map<String, MachineRow> rows = new LinkedHashMap<>();

        for (GanttBlockDTO block : data) {
            MachineRow row = rows.computeIfAbsent(block.getMachineryName(), MachineRow::new);
            row.addActivity(layer, new BlockActivity(block));
        }

        ganttChart.getRoot().getChildren().setAll(rows.values());

        Timeline timeline = ganttChart.getTimeline();
        timeline.showTemporalUnit(ChronoUnit.DAYS, 10);

        ganttChart.getGraphics().setActivityRenderer(BlockActivity.class, GanttLayout.class,
                new ColoredActivityRenderer(ganttChart.getGraphics()));

        // per aggiungere assign employee e notify delay
        graphics.setContextMenuCallback(param -> {
            ContextMenu menu = new ContextMenu();

            // evitiamo menu vuoti se non c'è nessuna activity
            if (param.getActivities().isEmpty()) {
                return menu;
            }

            // prendiamo il primo blocco
            ActivityRef<?> ref = param.getActivities().get(0);
            Activity activity = ref.getActivity();

            if (activity instanceof BlockActivity blockActivity) {
                // menu "Assign Employee" (solo se manager)
                if (Session.getInstance().getCurrentUser().getRole().equals("MANAGER")) {
                    Menu assignMenu = new Menu("Assign Employee");

                    String assignedEmail = blockActivity.getDTO().getAssignedEmployeeEmail();

                    for (Employee employee : employeeListService.fetchEmployees()) {
                        String fullName = employee.getName() + " " + employee.getSurname();
                        String email = employee.getEmail();

                        Label nameLabel = new Label(fullName);
                        Label emailLabel = new Label(email);
                        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
                        emailLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");

                        VBox content = new VBox(nameLabel, emailLabel);
                        content.setSpacing(2);
                        content.setPadding(new Insets(5));

                        if (email.equals(assignedEmail)) {
                            content.setStyle("-fx-background-color: lightgreen;");
                        }

                        CustomMenuItem item = new CustomMenuItem(content);
                        item.setHideOnClick(true);

                        item.setOnAction(e -> {
                            if (email.equals(blockActivity.getDTO().getAssignedEmployeeEmail())) {
                                // se clicco sull'employee già assegnato: de-assegna
                                blockActivity.getDTO().setAssignedEmployeeEmail(null);
                                blockActivity.getDTO().setAssignedEmployeeFullName(null);
                            } else {
                                // altrimenti assegna l'employee selezionato
                                blockActivity.getDTO().setAssignedEmployeeEmail(email);
                                blockActivity.getDTO().setAssignedEmployeeFullName(fullName);
                            }

                            if (!modifiedActivities.contains(blockActivity)) {
                                modifiedActivities.add(blockActivity);
                            }

                            showConfirmationButtons();
                        });

                        assignMenu.getItems().add(item);
                    }

                    menu.getItems().add(assignMenu);
                }

                // menu "Notice Delay" (solo se employee)
                if (Session.getInstance().getCurrentUser().getRole().equals("EMPLOYEE")) {
                    Menu delayMenu = new Menu("Notice Delay");

                    // durate predefinite in minuti (usa LinkedHashMap per mantenere l'ordine)
                    Map<String, Long> delays = new LinkedHashMap<>();
                    delays.put("30 minutes", 30L);
                    delays.put("1 hour", 60L);
                    delays.put("2 hours", 120L);
                    delays.put("4 hours", 240L);
                    delays.put("8 hours", 480L);

                    delays.forEach((label, totalMin) -> {
                        long days = totalMin / (8 * 60);
                        long rem = totalMin % (8 * 60);
                        long hours = rem / 60;
                        long minutes = rem % 60;

                        MenuItem item = new MenuItem(label);
                        item.setOnAction(e -> createDelayNotice(
                                blockActivity,
                                Duration.ofMinutes(totalMin),
                                days, hours, minutes
                        ));
                        delayMenu.getItems().add(item);
                    });

                    MenuItem customDelay = new MenuItem("Custom...");
                    customDelay.setOnAction(e -> {
                        Dialog<ButtonType> dialog = new Dialog<>();
                        dialog.setTitle("Custom Delay");
                        dialog.setHeaderText("Imposta ritardo (Days : Hours : Minutes)\n(1 day = 8 working hours)");

                        ButtonType apply = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
                        dialog.getDialogPane().getButtonTypes().addAll(apply, ButtonType.CANCEL);

                        // HBox con 3 campi: giorni : ore : minuti
                        HBox inputBox = createDelayInputFields(0, 0, 0);
                        dialog.getDialogPane().setContent(inputBox);

                        // validazione base: almeno uno > 0
                        Node applyBtn = dialog.getDialogPane().lookupButton(apply);
                        applyBtn.addEventFilter(ActionEvent.ACTION, ev -> {
                            @SuppressWarnings("unchecked")
                            List<TextField> fields = (List<TextField>) inputBox.getUserData();
                            long d = parseLongSafe(fields.get(0).getText());
                            long h = parseLongSafe(fields.get(1).getText());
                            long m = parseLongSafe(fields.get(2).getText());
                            long totalMinutes = d * 8 * 60 + h * 60 + m;
                            if (totalMinutes <= 0) {
                                ev.consume();
                                new Alert(Alert.AlertType.ERROR, "Il ritardo deve essere maggiore di zero.").showAndWait();
                            }
                        });

                        dialog.showAndWait().ifPresent(bt -> {
                            if (bt == apply) {
                                @SuppressWarnings("unchecked")
                                List<TextField> fields = (List<TextField>) inputBox.getUserData();
                                long days = parseLongSafe(fields.get(0).getText());
                                long hours = parseLongSafe(fields.get(1).getText());
                                long minutes = parseLongSafe(fields.get(2).getText());

                                long totalMinutes = days * 8 * 60 + hours * 60 + minutes;
                                createDelayNotice(blockActivity, Duration.ofMinutes(totalMinutes), days, hours, minutes);
                            }
                        });
                    });
                    delayMenu.getItems().add(customDelay);

                    menu.getItems().add(delayMenu);
                }

            }

            return menu;
        });



        ganttChart.getGraphics().showEarliestActivities();

        return ganttChart;
    }

    private void createDelayNotice(BlockActivity blockActivity, Duration delay, long days, long hours, long minutes) {
        NoticeDTO notice = new NoticeDTO();
        notice.setCreatorEmail(Session.getInstance().getCurrentUser().getEmail());
        notice.setSubject("Delay");

        String human = formatDelayHuman(days, hours, minutes); // per esempio "1d 2h 30m"
        String description = String.format(
                "Order ID: %d\nMachinery: %s\nStep order: %d\nDelay: %s (total %d minutes)",
                blockActivity.getDTO().getOrderId(),
                blockActivity.getDTO().getMachineryName(),
                blockActivity.getDTO().getStepOrder(),
                human,
                delay.toMinutes()
        );

        notice.setDescription(description);
        noticeService.saveNotice(notice);
    }

    private String formatDelayHuman(long days, long hours, long minutes) {
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        String s = sb.toString().trim();
        return s.isEmpty() ? "0m" : s;
    }


    private HBox createDelayInputFields(int days, int hours, int minutes) {
        TextField dayField = createNumericField(String.valueOf(days));
        TextField hourField = createNumericField(String.valueOf(hours));
        TextField minuteField = createNumericField(String.valueOf(minutes));

        // separatori tipo quello che usi nel process
        Label colon1 = new Label(":");
        Label colon2 = new Label(":");

        // suggerimento chiaro sull’ordine
        dayField.setPromptText("days");
        hourField.setPromptText("hours");
        minuteField.setPromptText("minutes");

        HBox box = new HBox(5, dayField, colon1, hourField, colon2, minuteField);
        box.setAlignment(Pos.CENTER_LEFT);

        // per recuperare facilmente i campi
        box.setUserData(List.of(dayField, hourField, minuteField));
        return box;
    }

    // parser sicuro: vuoto -> 0
    private long parseLongSafe(String s) {
        if (s == null || s.isBlank()) return 0L;
        try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return 0L; }
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


    /*
    private void showGantt(GanttChart<MachineRow> chart) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(chart);
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);
    }*/

    @FXML
    private void onFilterButtonClick() throws Exception {
        List<GanttBlockDTO> allData = ganttService.getGanttForAllOrders();
        Set<Long> allOrderIds = allData.stream()
                .map(GanttBlockDTO::getOrderId)
                .collect(Collectors.toCollection(TreeSet::new));

        // finestra di dialogo con selezione multipla
        Dialog<Set<Long>> dialog = new Dialog<>();
        dialog.setTitle("Seleziona Ordini");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        ListView<Long> listView = new ListView<>(FXCollections.observableArrayList(allOrderIds));
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedOrderIds.forEach(id -> listView.getSelectionModel().select(id)); // pre-seleziona

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.APPLY) {
                return new HashSet<>(listView.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        Optional<Set<Long>> result = dialog.showAndWait();
        result.ifPresent(selection -> {
            selectedOrderIds = selection;
            loadAndShowAllOrders(); // ricarica la Gantt con il filtro
        });
    }

    private void showGanttWithFilterButton(GanttChart<MachineRow> chart) {
        // crea il bottone filtro dinamicamente
        Button filterButton = new Button("Orders Filter");
        filterButton.setOnAction(evt -> {
            try {
                openFilterDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // arra superiore con bottoni
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button confirmButton = new Button("Conferma Modifiche");
        confirmButton.setOnAction(evt -> onConfirmChanges());
        confirmButton.setVisible(false);

        Button cancelButton = new Button("Annulla Modifiche");
        cancelButton.setOnAction(evt -> onCancelChanges());
        cancelButton.setVisible(false);

        topBar.getChildren().addAll(filterButton, spacer, cancelButton, confirmButton);

        // ScrollPane con il Gantt
        ScrollPane scrollPane = new ScrollPane(chart);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // VBox layout
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(topBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // salva riferimenti per toggle visibilità
        this.confirmButton = confirmButton;
        this.cancelButton = cancelButton;
        // pulisci contentPane e metti il VBox
        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);

        AnchorPane.setTopAnchor(vbox, 0.0);
        AnchorPane.setBottomAnchor(vbox, 0.0);
        AnchorPane.setLeftAnchor(vbox, 0.0);
        AnchorPane.setRightAnchor(vbox, 0.0);
    }

    private void openFilterDialog() throws Exception {
        List<GanttBlockDTO> allData = ganttService.getGanttForAllOrders();
        Set<Long> allOrderIds = allData.stream()
                .map(GanttBlockDTO::getOrderId)
                .collect(Collectors.toCollection(TreeSet::new));

        Dialog<Set<Long>> dialog = new Dialog<>();
        dialog.setTitle("Select Orders");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        // VBox per contenere le checkbox
        VBox checkBoxContainer = new VBox(5);
        checkBoxContainer.setPadding(new Insets(10));

        // mappa per tenere traccia delle checkbox per ogni orderId
        Map<Long, CheckBox> checkBoxMap = new HashMap<>();

        for (Long orderId : allOrderIds) {
            CheckBox cb = new CheckBox("Order #" + orderId);
            if (selectedOrderIds.contains(orderId)) {
                cb.setSelected(true);
            }
            checkBoxMap.put(orderId, cb);
            checkBoxContainer.getChildren().add(cb);
        }

        dialog.getDialogPane().setContent(checkBoxContainer);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                // raccogli solo gli orderId con checkbox selezionata
                Set<Long> selected = checkBoxMap.entrySet().stream()
                        .filter(e -> e.getValue().isSelected())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
                return selected;
            }
            return null;
        });

        Optional<Set<Long>> result = dialog.showAndWait();
        result.ifPresent(selection -> {
            selectedOrderIds = selection;
            loadAndShowAllOrders();
        });
    }

    private void handleActivityUpdate(ActivityEvent evt) {
        Activity activity = evt.getActivityRef().getActivity();
        if (activity instanceof BlockActivity blockActivity) {
            GanttBlockDTO dto = blockActivity.getDTO();

            dto.setActualStart(LocalDate.ofInstant(blockActivity.getStartTime(), ZoneId.systemDefault()));
            dto.setActualEnd(LocalDate.ofInstant(blockActivity.getEndTime(), ZoneId.systemDefault()));

            if (!modifiedActivities.contains(blockActivity)) {
                modifiedActivities.add(blockActivity);
            }
            showConfirmationButtons();
        }
    }


    private void showConfirmationButtons() {
        Platform.runLater(() -> {
            if (confirmButton != null && cancelButton != null) {
                confirmButton.setVisible(true);
                cancelButton.setVisible(true);
            }
        });
    }

    private void hideConfirmationButtons() {
        Platform.runLater(() -> {
            if (confirmButton != null && cancelButton != null) {
                confirmButton.setVisible(false);
                cancelButton.setVisible(false);
            }
        });
    }

    private void onConfirmChanges() {
        try {
            List<GanttBlockDTO> modifiedBlocks = modifiedActivities.stream()
                    .map(BlockActivity::getDTO)
                    .collect(Collectors.toList());

            ganttService.saveModifiedBlocks(modifiedBlocks);

            modifiedActivities.clear();
            hideConfirmationButtons();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onCancelChanges() {
        modifiedActivities.clear();
        hideConfirmationButtons();

        loadAndShowAllOrders();
    }

}
