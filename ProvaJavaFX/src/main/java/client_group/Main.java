package client_group;

import com.flexganttfx.core.FlexGanttFX;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;



public class Main extends Application {

    //per flexgantt
    final String myLicenseKey = "LIC=MattiaBonetti;VEN=DLSC;VER=12;PRO=STANDARD;RUN=yes;CTR=1;SignCode=3F;Signature=302D0214528141DA25728ED61BA8B1105B467E12D9C0D64602150092280939F6AF8DE38000F8781E7A22E34E0AE5E5";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/client_group/view/HomeView.fxml"));
        primaryStage.setTitle("Homepage");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.show();

        //per licenza flexgantt
        FlexGanttFX.setLicenseKey(myLicenseKey);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
