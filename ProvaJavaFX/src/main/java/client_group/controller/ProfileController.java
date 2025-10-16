package client_group.controller;

import client_group.dto.AssignedTaskDTO;
import client_group.dto.ProfileDTO;
import client_group.model.Session;
import client_group.service.ProfileService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

public class ProfileController {
    @FXML
    Label emailLabel;
    @FXML
    Label nameLabel;
    @FXML
    Label surnameLabel;
    @FXML
    Label phoneLabel;
    @FXML
    Label statusLabel;
    @FXML
    Label welcomeLabel;
    @FXML
    public Label assignedTaskLabel;

    @FXML
    TableView<AssignedTaskDTO> tasksTable;
    @FXML
    TableColumn<AssignedTaskDTO, Long> orderIdCol;
    @FXML
    TableColumn<AssignedTaskDTO, String> machineryCol;
    @FXML
    TableColumn<AssignedTaskDTO, Integer> stepCol;
    @FXML
    TableColumn<AssignedTaskDTO, LocalDate> startCol;
    @FXML
    public TableColumn<AssignedTaskDTO, LocalDate> endCol;

    private ProfileService profileService;
    ProfileDTO currentProfile;

    // costruttore di default
    public ProfileController() {
        this(ProfileService::new);
    }

    // costruttore per test con factory injection
    public ProfileController(Supplier<ProfileService> factory) {
        this.profileService = factory.get();
    }

    @FXML
    public void initialize() {
        try {
            String loggedInEmail = Session.getInstance().getCurrentUser().getEmail();

            currentProfile = profileService.getProfileByEmail(loggedInEmail);

            emailLabel.setText(currentProfile.getEmail());
            nameLabel.setText(currentProfile.getName());
            surnameLabel.setText(currentProfile.getSurname());
            phoneLabel.setText(currentProfile.getPhone());
            welcomeLabel.setText("Here is your profile, " + currentProfile.getName());

            // setup colonne tabella
            tasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            machineryCol.setCellValueFactory(new PropertyValueFactory<>("machineryName"));
            stepCol.setCellValueFactory(new PropertyValueFactory<>("stepIndex"));
            startCol.setCellValueFactory(new PropertyValueFactory<>("scheduledStart"));
            endCol.setCellValueFactory(new PropertyValueFactory<>("scheduledEnd"));

            //solo l'employee vede cosa gli è stato assegnato
            if(Session.getInstance().getCurrentUser().getRole().equals("EMPLOYEE")) {
                loadAssignedTasks();
            }else{
                tasksTable.setManaged(false);
                assignedTaskLabel.setManaged(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            emailLabel.setText("Error while loading profile");
        }
    }

    @FXML
    void handleEdit() {
        if (currentProfile == null) {
            showStatus("Profile not uploaded", "red");
            return;
        }

        Dialog<ProfileDTO> dialog = new Dialog<>();
        dialog.setTitle("Edit profile");

        ButtonType updateButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(currentProfile.getName());
        TextField surnameField = new TextField(currentProfile.getSurname());
        TextField phoneField = new TextField(currentProfile.getPhone());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Surname:"), 0, 1);
        grid.add(surnameField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                ProfileDTO updated = new ProfileDTO("Test", "User", "test@email.com", "333333", "EMPLOYEE");
                updated.setEmail(currentProfile.getEmail()); // email non cambia
                updated.setName(nameField.getText());
                updated.setSurname(surnameField.getText());
                updated.setPhone(phoneField.getText());
                updated.setRole(Session.getInstance().getCurrentUser().getRole());
                return updated;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedProfile -> {
            new Thread(() -> {
                try {
                    profileService.updateProfile(updatedProfile);
                    Platform.runLater(() -> {
                        currentProfile = updatedProfile;
                        nameLabel.setText(updatedProfile.getName());
                        surnameLabel.setText(updatedProfile.getSurname());
                        phoneLabel.setText(updatedProfile.getPhone());
                        showStatus("Profile updated successfully", "green");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showStatus("Error while updating", "red"));
                }
            }).start();
        });
    }

    @FXML
    void handleDelete() {
        if (currentProfile == null) {
            showStatus("Profile not uploaded", "red");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm elimination");
        confirm.setHeaderText("Are you sure you want to delete this profile?");
        confirm.setContentText("This action is irreversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        profileService.deleteProfile(currentProfile.getEmail());
                        Platform.runLater(() -> {
                            showStatus("Deleted profile", "green");
                            logout();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> showStatus("Error while deleting", "red"));
                    }
                }).start();
            }
        });
    }

    private void logout(){
        Session.getInstance().clear();
        //torna alla homepage
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_group/view/HomeView.fxml"));
            Parent root = loader.load();

            //creo una nuova finestra per evitare problemi con resize
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setResizable(true);

            //chiudo la vecchia finestra
            Stage currentStage = (Stage) emailLabel.getScene().getWindow();
            currentStage.close();

            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showStatus(String message, String color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    private void loadAssignedTasks() {
        String email = Session.getInstance().getCurrentUser().getEmail();
        new Thread(() -> {
            try {
                //List<AssignedTaskDTO> tasks = profileService.getAssignedTasksToday(email);
                List<AssignedTaskDTO> tasks = profileService.getAssignedTasks(email);
                Platform.runLater(() -> {
                    tasksTable.getItems().setAll(tasks);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
