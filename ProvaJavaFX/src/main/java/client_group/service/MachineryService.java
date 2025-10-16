package client_group.service;

import client_group.model.Machinery;
import client_group.model.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.net.URLEncoder;

public class MachineryService {

    private static final String BASE_URL = "http://localhost:8080/api/machinery";

    //Factory Injection
    @FunctionalInterface
    public interface ConnectionFactory {
        HttpURLConnection create(URL url) throws IOException;
    }

    private final ConnectionFactory connectionFactory;

    public MachineryService() {
        this.connectionFactory=url -> (HttpURLConnection) url.openConnection(); // default → produce connessioni reali
    }

    public MachineryService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // GET all
    public List<Machinery> loadAllMachinery() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = connectionFactory.create(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to fetch machinery: HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONArray jsonArray = new JSONArray(response.toString());
        List<Machinery> machineryList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Machinery m = new Machinery();
            m.setId(obj.getInt("id"));
            m.setName(obj.getString("name"));
            m.setBuyDate(LocalDate.parse(obj.getString("buyDate")));
            m.setYearManufacture(obj.optString("yearManufacture", ""));
            m.setCapacity(obj.optString("capacity", ""));
            machineryList.add(m);
        }

        return machineryList;
    }

    // DELETE by ID
    public void deleteMachinery(int id) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection conn = connectionFactory.create(url);
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        int responseCode = conn.getResponseCode();
        conn.disconnect();

        if (responseCode != 200) {
            throw new RuntimeException("Failed to delete machinery with id " + id);
        }
    }

    // POST create
    public Optional<Machinery> saveMachinery(Machinery machinery) throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = connectionFactory.create(url);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        JSONObject json = new JSONObject();
        json.put("name", machinery.getName());
        json.put("buyDate", machinery.getBuyDate().toString());
        json.put("yearManufacture", machinery.getYearManufacture());
        json.put("capacity", machinery.getCapacity());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.toString().getBytes());
            os.flush();
        }

        if (conn.getResponseCode() != 201) {
            conn.disconnect();
            return Optional.empty();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONObject obj = new JSONObject(response.toString());
        Machinery created = new Machinery();
        created.setId(obj.getInt("id"));
        created.setName(obj.getString("name"));
        created.setBuyDate(LocalDate.parse(obj.getString("buyDate")));
        created.setYearManufacture(obj.getString("yearManufacture"));
        created.setCapacity(obj.getString("capacity"));

        return Optional.of(created);
    }

    // PUT update
    public Optional<Machinery> updateMachinery(Machinery machinery) throws Exception {
        int id = machinery.getId();
        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection conn = connectionFactory.create(url);
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", machinery.getName());
        json.put("buyDate", machinery.getBuyDate().toString());
        json.put("yearManufacture", machinery.getYearManufacture());
        json.put("capacity", machinery.getCapacity());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.toString().getBytes());
            os.flush();
        }

        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            return Optional.empty();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONObject obj = new JSONObject(response.toString());
        Machinery updated = new Machinery();
        updated.setId(obj.getInt("id"));
        updated.setName(obj.getString("name"));
        updated.setBuyDate(LocalDate.parse(obj.getString("buyDate")));
        updated.setYearManufacture(obj.getString("yearManufacture"));
        updated.setCapacity(obj.getString("capacity"));

        return Optional.of(updated);
    }

    // GET all
    public List<Machinery> getAllMachinery() throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = connectionFactory.create(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try (InputStream input = conn.getInputStream()) {
            return mapper.readValue(input, new TypeReference<List<Machinery>>() {});
        }
    }

    public List<String> getAllMachineryNames() throws IOException {
        return getAllMachinery().stream().map(Machinery::getName).toList();
    }

    public Machinery getMachineryByName(String name) throws IOException {
        List<Machinery> all = getAllMachinery();
        return all.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
