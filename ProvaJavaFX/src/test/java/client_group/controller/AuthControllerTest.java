package client_group.controller;

import client_group.dto.ProfileDTO;
import client_group.service.ProfileService;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest extends ApplicationTest {

    private AuthController controller;
    private HttpURLConnection mockConnection;
    private ProfileService mockProfileService;

    @Override
    public void start(Stage stage) {
        controller = new AuthController();
        controller.emailField = new TextField();
        controller.passwordField = new PasswordField();
        controller.nameField = new TextField();
        controller.surnameField = new TextField();
        controller.phoneField = new TextField();
        controller.roleComboBox = new ComboBox<>();
        controller.roleComboBox.getItems().addAll("User", "Admin");
        controller.statusLabel = new Label();

        stage.setScene(new Scene(controller.emailField)); // scena dummy
        stage.show();
    }

    @BeforeEach
    void setup() {
        mockConnection = mock(HttpURLConnection.class);
        mockProfileService = mock(ProfileService.class);

        // factory che restituisce connessione mockata
        Function<String, HttpURLConnection> factory = (url) -> mockConnection;

        controller = new AuthController(mockProfileService, factory);
        controller.emailField = new TextField();
        controller.passwordField = new PasswordField();
        controller.nameField = new TextField();
        controller.surnameField = new TextField();
        controller.phoneField = new TextField();
        controller.roleComboBox = new ComboBox<>();
        controller.roleComboBox.getItems().addAll("User", "Admin");
        controller.statusLabel = new Label();
    }






    @Test
    void testHandleLogin_emptyFields() {
        controller.emailField.setText("");
        controller.passwordField.setText("");

        controller.handleLogin();

        Assertions.assertEquals("All fields are required.", controller.statusLabel.getText());
        verifyNoInteractions(mockConnection);
    }

    @Test
    void testHandleLogin_failureResponse() throws Exception {
        controller.emailField.setText("user@test.com");
        controller.passwordField.setText("password");

        when(mockConnection.getResponseCode()).thenReturn(401);
        when(mockConnection.getErrorStream()).thenReturn(new ByteArrayInputStream("Invalid credentials".getBytes()));
        when(mockConnection.getOutputStream()).thenReturn(mock(OutputStream.class));

        controller.handleLogin();

        Assertions.assertEquals("Invalid credentials", controller.statusLabel.getText());
        Assertions.assertTrue(controller.statusLabel.getStyle().contains("red"));
    }

    @Test
    void testHandleLogin_exception() throws Exception {
        controller.emailField.setText("user@test.com");
        controller.passwordField.setText("password");

        when(mockConnection.getOutputStream()).thenThrow(new RuntimeException("boom"));

        controller.handleLogin();

        Assertions.assertEquals("Server error", controller.statusLabel.getText());
        Assertions.assertTrue(controller.statusLabel.getStyle().contains("red"));
    }

    @Test
    void testHandleRegister_success() throws Exception {
        controller.emailField.setText("new@test.com");
        controller.passwordField.setText("pass");
        controller.nameField.setText("Name");
        controller.surnameField.setText("Surname");
        controller.phoneField.setText("123456");
        controller.roleComboBox.setValue("User");

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("ok".getBytes()));
        when(mockConnection.getOutputStream()).thenReturn(mock(OutputStream.class));

        controller.handleRegister();

        Assertions.assertEquals("Registration successful!", controller.statusLabel.getText());
        Assertions.assertTrue(controller.statusLabel.getStyle().contains("green"));
    }

    @Test
    void testHandleRegister_missingFields() {
        controller.emailField.setText("");
        controller.handleRegister();

        Assertions.assertEquals("All fields are required!", controller.statusLabel.getText());
    }

    @Test
    void testHandleRegister_failureResponse() throws Exception {
        controller.emailField.setText("user@test.com");
        controller.passwordField.setText("pass");
        controller.nameField.setText("Name");
        controller.surnameField.setText("Surname");
        controller.phoneField.setText("123456");
        controller.roleComboBox.setValue("User");

        when(mockConnection.getResponseCode()).thenReturn(400);
        when(mockConnection.getErrorStream()).thenReturn(new ByteArrayInputStream("Bad request".getBytes()));
        when(mockConnection.getOutputStream()).thenReturn(mock(OutputStream.class));

        controller.handleRegister();

        Assertions.assertEquals("Bad request", controller.statusLabel.getText());
        Assertions.assertTrue(controller.statusLabel.getStyle().contains("red"));
    }

    @Test
    void testHandleRegister_exception() throws Exception {
        controller.emailField.setText("user@test.com");
        controller.passwordField.setText("pass");
        controller.nameField.setText("Name");
        controller.surnameField.setText("Surname");
        controller.phoneField.setText("123456");
        controller.roleComboBox.setValue("User");

        when(mockConnection.getOutputStream()).thenThrow(new RuntimeException("boom"));

        controller.handleRegister();

        Assertions.assertEquals("Internal error during registration", controller.statusLabel.getText());
        Assertions.assertTrue(controller.statusLabel.getStyle().contains("red"));
    }

    @Test
    void testHandleBack_safe() {

        Assertions.assertDoesNotThrow(controller::handleBack);
    }

    @Test
    void testLoadDashboard_safe() {
        Assertions.assertDoesNotThrow(() -> controller.loadDashboard("/client_group/view/DashboardView.fxml"));
    }
    @Test
    void testHandleLogin_missingTokenInResponse() throws Exception {
        controller.emailField.setText("user@test.com");
        controller.passwordField.setText("password");

        String response = "{\"role\":\"User\"}";
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(response.getBytes()));
        when(mockConnection.getOutputStream()).thenReturn(mock(OutputStream.class));

        controller.handleLogin();

        Assertions.assertEquals("Server error", controller.statusLabel.getText());
        Assertions.assertTrue(controller.statusLabel.getStyle().contains("red"));
    }
    @Test
    void testHandleRegister_nullStream() throws Exception {
        controller.emailField.setText("user@test.com");
        controller.passwordField.setText("pass");
        controller.nameField.setText("Name");
        controller.surnameField.setText("Surname");
        controller.phoneField.setText("123456");
        controller.roleComboBox.setValue("User");

        when(mockConnection.getResponseCode()).thenReturn(500);
        when(mockConnection.getErrorStream()).thenReturn(null);
        when(mockConnection.getOutputStream()).thenReturn(mock(OutputStream.class));

        controller.handleRegister();

        Assertions.assertEquals("Generic Error!", controller.statusLabel.getText());
        Assertions.assertTrue(controller.statusLabel.getStyle().contains("red"));
    }
    @Test
    void testLoadDashboard_invalidFxml() {
        controller.loadDashboard("/client_group/view/NonExistent.fxml");
        Assertions.assertEquals("Error loading dashboard", controller.statusLabel.getText());
    }
    @Test
    void testHandleBack_invalidFxml() {
        Assertions.assertDoesNotThrow(() -> controller.loadDashboard("/wrong/path.fxml"));
    }
}
