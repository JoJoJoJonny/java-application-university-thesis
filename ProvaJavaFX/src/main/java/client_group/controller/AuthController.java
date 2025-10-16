package client_group.controller;

import client_group.dto.ProfileDTO;
import client_group.model.Session;
import client_group.service.ProfileService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AuthController {
    @FXML TextField emailField;
    @FXML PasswordField passwordField;
    @FXML TextField nameField;
    @FXML TextField surnameField;
    @FXML TextField phoneField;
    @FXML ComboBox<String> roleComboBox;
    @FXML Label statusLabel;

    ProfileService profileService;
    Function<String, HttpURLConnection> connectionFactory;
    //Costruttore per test
    public AuthController(ProfileService profileService,
                          Function<String, HttpURLConnection> connectionFactory) {
        this.profileService = profileService;
        this.connectionFactory = connectionFactory;
    }

    //Costruttore di default per JavaFX
    public AuthController() {
        this(new ProfileService(), (urlString) -> {
            try {
                URL url = new URL(urlString);
                return (HttpURLConnection) url.openConnection();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        try {
            HttpURLConnection conn = connectionFactory.apply("http://localhost:8080/api/auth/login");
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String response = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            if (responseCode == 200) {
                JSONObject res = new JSONObject(response);
                String token = res.getString("token").trim();


                Session.getInstance().setToken(token);
                ProfileDTO profile = profileService.getProfileByEmail(email);
                Session.getInstance().setCurrentUser(profile);

                loadDashboard("/client_group/view/DashboardView.fxml");
            } else {
                statusLabel.setText(response);
                statusLabel.setStyle("-fx-text-fill: red;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Server error");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }


    void loadDashboard(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            DashboardController controller = loader.getController();

            // crea una nuova finestra per la dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.setScene(new Scene(root));
            dashboardStage.setMaximized(true);
            dashboardStage.setResizable(true);

            // chiudi la finestra di login
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();

            dashboardStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading dashboard");
        }
    }

    @FXML
    void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_group/view/HomeView.fxml"));
            Parent root = loader.load();
            HomeController controller = loader.getController();

            // creo una nuova finestra e chiudo quella precedente per evitare problemi di ridimensionamento
            Stage backStage = new Stage();
            backStage.setScene(new Scene(root));
            backStage.setResizable(true);

            // chiudo quella vecchia
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();

            backStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String name = nameField.getText();
        String surname = surnameField.getText();
        String phone = phoneField.getText();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty() || phone.isEmpty() || role == null) {
            statusLabel.setText("All fields are required!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            HttpURLConnection conn = connectionFactory.apply("http://localhost:8080/api/auth/register");
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"%s\",\"surname\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"}",
                    email, password, name, surname, phone, role
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseMessage = "Generic Error!";

            if (is != null) {
                responseMessage = new BufferedReader(new InputStreamReader(is))
                        .lines().collect(Collectors.joining("\n"));
            }

            if (responseCode == 200) {
                statusLabel.setText("Registration successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText(responseMessage);
                statusLabel.setStyle("-fx-text-fill: red;");
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Internal error during registration");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
