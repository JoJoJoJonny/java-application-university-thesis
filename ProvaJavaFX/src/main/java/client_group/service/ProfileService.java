package client_group.service;

import client_group.dto.AssignedTaskDTO;
import client_group.dto.ProfileDTO;
import client_group.model.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ProfileService {

    private static final String BASE_URL = "http://localhost:8080/api/profile";
    private final ObjectMapper mapper = new ObjectMapper();

    //Factory Injection
    @FunctionalInterface
    public interface ConnectionFactory {
        HttpURLConnection create(URL url) throws IOException;
    }

    private final ConnectionFactory connectionFactory;

    // Costruttore
    public ProfileService() {
        this.connectionFactory = null; // default produce connessioni reali
    }

    // Costruttore per i test
    public ProfileService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private HttpURLConnection openConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (connectionFactory != null) ? connectionFactory.create(url) : (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());
        return conn;
    }

    // GET
    public ProfileDTO getProfileByEmail(String email) throws Exception {
        HttpURLConnection conn = openConnection(BASE_URL + "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8), "GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed: HTTP error code " + conn.getResponseCode());
        }

        try (InputStream is = conn.getInputStream()) {
            ProfileDTO profile = mapper.readValue(is, ProfileDTO.class);
            return profile;
        } finally {
            conn.disconnect();
        }
    }

    // PUT - update profile
    public ProfileDTO updateProfile(ProfileDTO profile) throws Exception {
        String emailEncoded = URLEncoder.encode(profile.getEmail(), StandardCharsets.UTF_8);
        HttpURLConnection conn = openConnection(BASE_URL + "/" + emailEncoded, "PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("email", profile.getEmail());
        json.put("name", profile.getName());
        json.put("surname", profile.getSurname());
        json.put("phone", profile.getPhone());
        json.put("role", profile.getRole());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.toString().getBytes());
            os.flush();
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to update profile: HTTP error code " + conn.getResponseCode());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return mapper.readValue(response.toString(), ProfileDTO.class);
        } finally {
            conn.disconnect();
        }
    }

    // DELETE
    public void deleteProfile(String email) throws Exception {
        String emailEncoded = URLEncoder.encode(email, StandardCharsets.UTF_8);
        HttpURLConnection conn = openConnection(BASE_URL + "/" + emailEncoded, "DELETE");

        int responseCode = conn.getResponseCode();
        conn.disconnect();

        if (responseCode != 200) {
            throw new RuntimeException("Failed to delete profile: HTTP error code " + responseCode);
        }
    }

    // Lavorazione quotidiana
    public List<AssignedTaskDTO> getAssignedTasksToday(String email) throws IOException {
        HttpURLConnection conn = openConnection(BASE_URL + "/assigned-today?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8), "GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() == 200) {
            try (InputStream is = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                AssignedTaskDTO[] arr = mapper.readValue(is, AssignedTaskDTO[].class);
                return Arrays.asList(arr);
            } finally {
                conn.disconnect();
            }
        } else {
            conn.disconnect();
            throw new RuntimeException("Failed to fetch tasks: " + conn.getResponseCode());
        }
    }

    // Tutte le lavorazioni assegnate
    public List<AssignedTaskDTO> getAssignedTasks(String email) throws IOException {
        HttpURLConnection conn = openConnection(BASE_URL + "/assigned?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8), "GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() == 200) {
            try (InputStream is = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                AssignedTaskDTO[] arr = mapper.readValue(is, AssignedTaskDTO[].class);
                return Arrays.asList(arr);
            } finally {
                conn.disconnect();
            }
        } else {
            conn.disconnect();
            throw new RuntimeException("Failed to fetch tasks: " + conn.getResponseCode());
        }
    }
}
