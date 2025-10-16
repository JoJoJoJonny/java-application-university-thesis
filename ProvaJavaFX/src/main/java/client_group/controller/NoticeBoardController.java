package client_group.controller;

import client_group.dto.NoticeDTO;
import client_group.model.Session;
import client_group.service.NoticeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.util.List;

public class NoticeBoardController {

    @FXML
    GridPane personalGrid;
    @FXML private GridPane delayGrid;
    @FXML private GridPane employeeGrid;
    @FXML
    GridPane managerGrid;
    @FXML private GridPane accountantGrid;
    @FXML private GridPane otherGrid;

    @FXML
    Button deleteButton;
    @FXML private Label statusLabel;

    private final NoticeService noticeService = new NoticeService();

    NoticeDTO selectedNotice;
    VBox selectedBox;

    //per observer pattern
    private NoticeEventListener noticeEventListener;

    @FXML
    public void initialize() {
        String userEmail = Session.getInstance().getCurrentUser().getEmail();
        String userRole = Session.getInstance().getCurrentUser().getRole();

        // Manager può sempre cancellare
        deleteButton.setVisible("MANAGER".equalsIgnoreCase(userRole));
        deleteButton.setDisable(true);

        // Carica e popola i notice
        List<NoticeDTO> all = noticeService.fetchNotices(userEmail);
        for (NoticeDTO notice : all) {
            VBox box = createNoticeBox(notice, userRole, userEmail);
            switch (notice.getCategory()) {
                case "PersonalNotice" -> addBoxToGrid(personalGrid, box);
                case "DelayNotice" -> addBoxToGrid(delayGrid, box);
                case "EmployeeNotice" -> addBoxToGrid(employeeGrid, box);
                case "ManagerNotice" -> addBoxToGrid(managerGrid, box);
                case "AccountantNotice" -> addBoxToGrid(accountantGrid, box);
                default -> addBoxToGrid(otherGrid, box);
            }
        }

        //per observer pattern
        noticeEventListener = new NoticeEventListener(
                "http://localhost:8080/api/notice/stream",
                this::handleNoticeUpdate
        );
        noticeEventListener.start();
    }

    private void handleNoticeUpdate() {
        handleRefresh();
        statusLabel.setText("Updated from server");
        statusLabel.setStyle("-fx-text-fill: blue;");
    }

    VBox createNoticeBox(NoticeDTO notice, String userRole, String userEmail) {
        Label author = new Label(notice.getCreatorFullName() + "\n" + notice.getCreatorEmail());
        author.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        author.setWrapText(true);
        Label subject = new Label(notice.getSubject());
        subject.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        subject.setWrapText(true);
        Label desc = new Label(notice.getDescription());
        desc.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-border-radius: 4;");
        desc.setWrapText(true);

        VBox box = new VBox(5, author, subject, desc);
        box.setStyle("-fx-border-color: #ccc; -fx-padding: 8; -fx-background-color: white;");

        //solo il manager può cancellarli, quindi solo per lui mettiamo un listener
        if(Session.getInstance().getCurrentUser().getRole().equals("MANAGER")) {
            box.setOnMouseClicked(evt -> {
                if (evt.getButton() == MouseButton.PRIMARY) {
                    if (selectedBox != null)
                        selectedBox.setStyle("-fx-border-color: #ccc; -fx-padding: 8; -fx-background-color: white;");
                    selectedBox = box;
                    selectedNotice = notice;
                    box.setStyle("-fx-border-color: #0078D7; -fx-background-color: #E0F0FF; -fx-padding: 8;");
                    updateDeleteButton(userRole, userEmail, notice);
                }
            });
        }
        return box;
    }

    private void updateDeleteButton(String userRole, String userEmail, NoticeDTO notice) {
        boolean canDelete = "MANAGER".equalsIgnoreCase(userRole)
                || ("PersonalNotice".equals(notice.getCategory()) && userEmail.equalsIgnoreCase(notice.getCreatorEmail()));
        deleteButton.setDisable(!canDelete);
    }

    private void addBoxToGrid(GridPane grid, VBox box) {
        int count = grid.getChildren().size();
        int col = count % 4;
        int row = count / 4;
        grid.add(box, col, row);
        GridPane.setFillWidth(box, true);
    }

    @FXML
    private void handleRefresh() {
        Platform.runLater(() -> {
            clearAllGrids();
            initialize();
            statusLabel.setText("Refreshed");
            statusLabel.setStyle("-fx-text-fill: black;");
        });
    }

    @FXML
    private void handleAdd() {
        Dialog<NoticeDTO> dlg = new Dialog<>();
        dlg.setTitle("Add New Notice");
        ButtonType ok = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField subjectF = new TextField(); subjectF.setPromptText("Subject");
        TextArea descA = new TextArea(); descA.setPromptText("Description");
        VBox content = new VBox(10, new Label("Subject:"), subjectF, new Label("Description:"), descA);
        dlg.getDialogPane().setContent(content);

        Node addBtn = dlg.getDialogPane().lookupButton(ok);
        addBtn.setDisable(true);
        subjectF.textProperty().addListener((obs, o, n) -> addBtn.setDisable(n.trim().isEmpty()));

        dlg.setResultConverter(bt -> bt == ok ? new NoticeDTO(Session.getInstance().getCurrentUser().getEmail(), subjectF.getText(), descA.getText()) : null);
        dlg.showAndWait().ifPresent(dto -> {
            noticeService.saveNotice(dto);
            handleRefresh();
            statusLabel.setText("Added new notice");
            statusLabel.setStyle("-fx-text-fill: green;");
        });
    }

    @FXML
    private void handleDelete() {
        if (selectedNotice == null) {
            statusLabel.setText("No notice selected");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        new Thread(() -> {
            try {
                noticeService.deleteNotice(selectedNotice.getId());
                Pane parent = (Pane) selectedBox.getParent();
                Platform.runLater(() -> {
                    //parent.getChildren().remove(selectedBox);
                    selectedBox = null;
                    selectedNotice = null;
                    deleteButton.setDisable(true);
                    statusLabel.setText("Notice deleted");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error deleting notice");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    private void clearAllGrids() {
        for (GridPane gp : List.of(personalGrid, delayGrid, employeeGrid, managerGrid, accountantGrid, otherGrid)) {
            gp.getChildren().clear();
        }
    }
}

