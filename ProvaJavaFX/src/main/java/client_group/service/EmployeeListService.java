package client_group.service;

import client_group.model.Employee;
import client_group.model.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EmployeeListService {

    private final Supplier<HttpURLConnection> connectionSupplier;

    // Costruttore normale
    public EmployeeListService() {
        this.connectionSupplier = null;
    }

    // Costruttore per factory injection
    EmployeeListService(Supplier<HttpURLConnection> supplier) {
        this.connectionSupplier = supplier;
    }

    private HttpURLConnection openConnection(String urlString, String method) throws Exception {
        HttpURLConnection conn;
        if (connectionSupplier != null) {
            conn = connectionSupplier.get();
        } else {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
        }
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());
        return conn;
    }

    public List<Employee> fetchEmployees() {
        List<Employee> employeeList = new ArrayList<>();

        try {
            HttpURLConnection conn = openConnection("http://localhost:8080/api/employee/list", "GET");

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
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Employee emp = new Employee();
                    emp.setEmail(obj.getString("email"));
                    emp.setName(obj.getString("name"));
                    emp.setSurname(obj.getString("surname"));
                    emp.setPhone(obj.getString("phone"));
                    employeeList.add(emp);
                }
            } else {
                System.err.println("Errore HTTP: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return employeeList;
    }
}
