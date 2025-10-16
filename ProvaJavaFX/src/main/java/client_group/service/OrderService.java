package client_group.service;

import client_group.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OrderService {

    private static final String BASE_URL = "http://localhost:8080/api/orders";

    private final Supplier<HttpURLConnection> connectionSupplier;

    // costruttore normale
    public OrderService() {
        this.connectionSupplier = null;
    }

    // costruttore per factory injection
    public OrderService(Supplier<HttpURLConnection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    // apertura connessione
    private HttpURLConnection openConnection(String urlString, String method) throws IOException {
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
    public List<Order> fetchAll() throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL, "GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to fetch orders: HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONArray jsonArray = new JSONArray(response.toString());
        List<Order> orderList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Order order = parseOrder(obj);
            orderList.add(order);
        }

        return orderList;
    }

    // Create new order
    public Order createOrder(Order order) throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL, "POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        JSONObject json = orderToJson(order);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        if (conn.getResponseCode() != 201 && conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to create order: HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONObject obj = new JSONObject(response.toString());
        return parseOrder(obj);
    }

    // Update existing order
    public Order updateOrder(Order order) throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL + "/" + order.getId(), "PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        JSONObject json = orderToJson(order);
        json.put("id", order.getId());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to update order: HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONObject obj = new JSONObject(response.toString());
        return parseOrder(obj);
    }

    //Update state
    public void updateOrderState(Long orderId, String action) throws IOException {
        HttpURLConnection conn = openConnection(BASE_URL + "/" + orderId + "/" + action, "PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Errore dal server: " + responseCode);
        }
    }

    // Utility: convert Order to JSON for POST/PUT
    private JSONObject orderToJson(Order order) {
        JSONObject json = new JSONObject();

        json.put("quantity", order.getQuantity());
        json.put("status", order.getStatus().name());

        // Dates
        if (order.getCreateDate() != null) {
            json.put("createDate", order.getCreateDate().toString());
        }
        if (order.getDeadline() != null) {
            json.put("deadline", order.getDeadline().toString());
        }
        if (order.getEndDate() != null) {
            json.put("endDate", order.getEndDate().toString());
        }
        if (order.getStartDate() != null) {
            json.put("startDate", order.getStartDate().toString());
        }

        // Model
        JSONObject modelJson = new JSONObject();
        modelJson.put("id", order.getModel().getRaw().getId());
        modelJson.put("name", order.getModel().getName());
        json.put("model", modelJson);

        // Client
        JSONObject clientJson = new JSONObject();
        clientJson.put("piva", order.getClient().getPiva());
        clientJson.put("companyName", order.getClient().getCompanyName());
        json.put("client", clientJson);

        return json;
    }

    // Utility: parse JSONObject to Order
    private Order parseOrder(JSONObject obj) {
        Order order = new Order();

        order.setId(obj.optLong("id", 0));
        order.setQuantity(obj.optInt("quantity", 0));
        order.setStatus(OrderStatus.valueOf(obj.getString("status")));

        // Parse dates
        if (obj.has("createDate") && !obj.isNull("createDate")) {
            order.setCreateDate(LocalDate.parse(obj.getString("createDate")));
        }
        if (obj.has("deadline") && !obj.isNull("deadline")) {
            order.setDeadline(LocalDate.parse(obj.getString("deadline")));
        }
        if (obj.has("endDate") && !obj.isNull("endDate")) {
            order.setEndDate(LocalDate.parse(obj.getString("endDate")));
        }
        if (obj.has("startDate") && !obj.isNull("startDate")) {
            order.setStartDate(LocalDate.parse(obj.getString("startDate")));
        }

        // Parse Model
        if (obj.has("model") && !obj.isNull("model")) {
            JSONObject modelObj = obj.getJSONObject("model");
            Model model = new Model();
            Raw raw = new Raw();
            raw.setId(modelObj.optLong("id", 0));
            model.setName(modelObj.optString("name", null));

            if (modelObj.has("raw") && !modelObj.isNull("raw")) {
                JSONObject rawObj = modelObj.getJSONObject("raw");
                raw.setId(rawObj.optLong("id", 0));
                model.setRaw(raw);
            }

            order.setModel(model);
        }

        // Parse Client
        if (obj.has("client") && !obj.isNull("client")) {
            JSONObject clientObj = obj.getJSONObject("client");
            Client client = new Client();
            client.setPiva(clientObj.optString("piva", null));
            client.setCompanyName(clientObj.optString("companyName", null));
            client.setEmail(clientObj.optString("email", null));
            client.setPhone(clientObj.optString("phone", null));
            order.setClient(client);
        }

        return order;
    }
}
