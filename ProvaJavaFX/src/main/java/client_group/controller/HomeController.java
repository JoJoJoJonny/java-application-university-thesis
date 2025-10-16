package client_group.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    void navigate(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            //creo una nuova finestra per evitare problemi con resize
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setResizable(true);

            //chiudo la vecchia finestra
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.close();

            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onCustomUserLogin(ActionEvent event) {
        navigate(event, "/client_group/view/CustomUserLoginView.fxml");
    }

    @FXML
    void onCustomUserRegister(ActionEvent event) {
        navigate(event, "/client_group/view/CustomUserRegisterView.fxml");
    }

}
