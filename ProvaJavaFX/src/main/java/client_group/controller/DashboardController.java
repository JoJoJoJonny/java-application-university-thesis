package client_group.controller;

import client_group.model.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    AnchorPane contentPane;

    @FXML
    public Button btnProfile;
    @FXML
    Button btnNoticeBoard;
    @FXML
    Button btnEmployees;
    @FXML
    Button btnGantt;
    @FXML
    Button btnClients;
    @FXML
    Button btnOrders;
    @FXML
    Button btnModels;
    @FXML
    Button btnProcesses;
    @FXML
    Button btnMachinery;
    @FXML
    Button btnInventory;

    @FXML
    public void initialize() {
        String role = Session.getInstance().getCurrentUser().getRole();

        switch (role) {
            case "MANAGER":
                // mostra tutto
                break;
            case "EMPLOYEE":
                btnEmployees.setManaged(false);
                btnMachinery.setManaged(false);
                btnInventory.setManaged(false);
                btnClients.setManaged(false);
                break;
            case "ACCOUNTANT":
                btnGantt.setManaged(false);
                btnProcesses.setManaged(false);
                btnMachinery.setManaged(false);
                break;
            default:
                // se per qualche motivo il ruolo è sconosciuto (anche se non dovrebbe), nasconde tutto tranne il profilo
                btnNoticeBoard.setManaged(false);
                btnEmployees.setManaged(false);
                btnGantt.setManaged(false);
                btnOrders.setManaged(false);
                btnModels.setManaged(false);
                btnProcesses.setManaged(false);
                btnMachinery.setManaged(false);
                btnInventory.setManaged(false);
                btnClients.setManaged(false);
                break;
        }
    }

    @FXML
    void handleNoticeBoard() {loadContent("/client_group/view/NoticeBoardView.fxml");}
    @FXML
    void handleProfile() {loadContent("/client_group/view/ProfileView.fxml");}
    @FXML
    void handleEmployees() {loadContent("/client_group/view/EmployeeListView.fxml");}
    @FXML
    void handleGantt() {loadContent("/client_group/view/GanttView.fxml");}
    @FXML
    void handleClients() {loadContent("/client_group/view/ClientsView.fxml");}
    @FXML
    void handleOrders() {loadContent("/client_group/view/OrdersView.fxml");}
    @FXML
    void handleModels() {loadContent("/client_group/view/ModelsView.fxml");}
    @FXML
    void handleProcesses() {loadContent("/client_group/view/ProcessesView.fxml");}
    @FXML
    void handleMachinery() {loadContent("/client_group/view/MachineryView.fxml");}
    @FXML
    void handleInventory() {loadContent("/client_group/view/InventoryView.fxml");}
    @FXML
    public void handleLogout() {
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
            Stage currentStage = (Stage) btnProfile.getScene().getWindow();
            currentStage.close();

            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load homepage.");
        }
    }



    public void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load content.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
