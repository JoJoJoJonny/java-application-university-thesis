package client_group.controller;

import client_group.model.Employee;
import client_group.service.EmployeeListService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeeListController {

    /*
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> emailColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> surnameColumn;
    @FXML private TableColumn<Employee, String> phoneColumn;

    private boolean initialized = false;

    public void loadEmployeeData() {
        if (!initialized) {
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
            phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
            initialized = true;
        }

        // esegui richiesta su nuovo thread per evitare blocchi UI
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/api/manager/employees");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    List<Employee> employeeList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Employee emp = new Employee();
                        emp.setEmail(obj.getString("email"));
                        emp.setName(obj.getString("name"));
                        emp.setSurname(obj.getString("surname"));
                        emp.setPhone(obj.getString("phone"));
                        employeeList.add(emp);
                    }

                    Platform.runLater(() -> employeeTable.getItems().setAll(employeeList));
                } else {
                    System.err.println("Errore HTTP: " + responseCode);
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    */


    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> emailColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> surnameColumn;
    @FXML private TableColumn<Employee, String> phoneColumn;

    private final EmployeeListService employeeService = new EmployeeListService();

    @FXML
    public void initialize() {
        setupTable();
        loadEmployeeData();
    }

    private void setupTable() {
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void loadEmployeeData() {
        List<Employee> employees = employeeService.fetchEmployees();
        Platform.runLater(() -> employeeTable.getItems().setAll(employees));
    }
}

