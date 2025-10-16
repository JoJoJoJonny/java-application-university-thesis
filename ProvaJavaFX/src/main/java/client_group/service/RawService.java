package client_group.service;

import client_group.model.Raw;
import client_group.model.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RawService {

    private static final String BASE_URL = "http://localhost:8080/api/raw";

    private final ObjectMapper mapper = new ObjectMapper();

    private final Supplier<HttpURLConnection> connectionSupplier;

    // Costruttore
    public RawService() {
        this.connectionSupplier = null;
    }

    // Costruttore per factory injection
    public RawService(Supplier<HttpURLConnection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
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

    // GET all
    public List<Raw> fetchAllRaw() throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL, "GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to fetch raw materials: HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONArray jsonArray = new JSONArray(response.toString());
        List<Raw> rawList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Raw raw = new Raw();
            raw.setId(obj.getLong("id"));
            raw.setShape(obj.getString("shape"));
            raw.setMaterial(obj.getString("material"));
            raw.setSize(obj.getString("size"));
            raw.setCastingNumber(obj.optString("castingNumber", ""));
            raw.setThickness(obj.optString("thickness", ""));
            rawList.add(raw);
        }

        return rawList;
    }

    // DELETE by ID
    public void deleteRaw(Long id) throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL + "/" + id, "DELETE");

        int responseCode = conn.getResponseCode();
        conn.disconnect();

        if (responseCode != 200) {
            throw new RuntimeException("Failed to delete raw with id " + id);
        }
    }

    // POST create
    public Raw createRaw(Raw raw) throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL, "POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("shape", raw.getShape());
        json.put("material", raw.getMaterial());
        json.put("size", raw.getSize());
        json.put("castingNumber", raw.getCastingNumber());
        json.put("thickness", raw.getThickness());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.toString().getBytes());
            os.flush();
        }

        if (conn.getResponseCode() != 201) {
            throw new RuntimeException("Failed to create raw: HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONObject obj = new JSONObject(response.toString());
        Raw created = new Raw();
        created.setId(obj.getLong("id"));
        created.setShape(obj.getString("shape"));
        created.setMaterial(obj.getString("material"));
        created.setSize(obj.getString("size"));
        created.setCastingNumber(obj.getString("castingNumber"));
        created.setThickness(obj.getString("thickness"));
        return created;
    }

    // PUT update
    public Raw updateRaw(Raw raw) throws Exception {
        Long id = raw.getId();
        HttpURLConnection conn = openConnection(BASE_URL + "/" + id, "PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("shape", raw.getShape());
        json.put("material", raw.getMaterial());
        json.put("size", raw.getSize());
        json.put("castingNumber", raw.getCastingNumber());
        json.put("thickness", raw.getThickness());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.toString().getBytes());
            os.flush();
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to update raw: HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONObject obj = new JSONObject(response.toString());
        Raw updated = new Raw();
        updated.setId(obj.getLong("id"));
        updated.setShape(obj.getString("shape"));
        updated.setMaterial(obj.getString("material"));
        updated.setSize(obj.getString("size"));
        updated.setCastingNumber(obj.getString("castingNumber"));
        updated.setThickness(obj.getString("thickness"));
        return updated;
    }

    public List<Raw> loadAllRaw() throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL, "GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to fetch raw data: HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        JSONArray jsonArray = new JSONArray(response.toString());
        List<Raw> rawList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Raw raw = new Raw();
            raw.setId(obj.getLong("id"));
            raw.setShape(obj.getString("shape"));
            raw.setMaterial(obj.getString("material"));
            raw.setSize(obj.getString("size"));
            raw.setCastingNumber(obj.getString("castingNumber"));
            raw.setThickness(obj.getString("thickness"));
            rawList.add(raw);
        }

        return rawList;
    }

    public List<Raw> getAllRaws() throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL, "GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to fetch raw data: HTTP error code : " + conn.getResponseCode());
        }

        try (InputStream input = conn.getInputStream()) {
            return mapper.readValue(input, new TypeReference<List<Raw>>() {});
        }
    }
}
