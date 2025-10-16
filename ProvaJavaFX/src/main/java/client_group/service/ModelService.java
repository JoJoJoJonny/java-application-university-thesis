package client_group.service;

import client_group.model.Model;
import client_group.model.Raw;
import client_group.model.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class ModelService {

    private final String BASE_URL = "http://localhost:8080/api/models";
    private final String RAW_URL = "http://localhost:8080/api/raw";

    private final ObjectMapper mapper = new ObjectMapper();

    //Factory Injection
    @FunctionalInterface
    public interface ConnectionFactory {
        HttpURLConnection create(URL url) throws IOException;
    }

    private final ConnectionFactory connectionFactory;

    // Costruttore reale
    public ModelService() {
        this.connectionFactory = url -> (HttpURLConnection) url.openConnection();
    }

    // Costruttore per i test
    public ModelService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public List<Model> loadAllModels() throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection con = connectionFactory.create(url);
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        try (InputStream input = con.getInputStream()) {
            return mapper.readValue(input, new TypeReference<List<Model>>() {});
        }
    }

    public List<Model> fetchAllModels() {
        try {
            return loadAllModels();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of(); // ritorna lista vuota in caso di errore
        }
    }

    public Optional<Model> saveModel(Model model) throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection con = connectionFactory.create(url);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        try (OutputStream os = con.getOutputStream()) {
            mapper.writeValue(os, model);
        }

        if (con.getResponseCode() == 201 || con.getResponseCode() == 200) {
            try (InputStream input = con.getInputStream()) {
                Model savedModel = mapper.readValue(input, Model.class);
                return Optional.of(savedModel);
            }
        }
        return Optional.empty();
    }

    public void updateModel(Model model) throws IOException {
        if (model.getName() == null || model.getName().isBlank()) {
            throw new IllegalArgumentException("Model name is null or empty, cannot update.");
        }

        URL url = new URL(BASE_URL + "/" + model.getName());
        System.out.println("PUT " + url);

        HttpURLConnection con = connectionFactory.create(url);
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        try (OutputStream os = con.getOutputStream()) {
            mapper.writeValue(os, model);
        }

        if (con.getResponseCode() != 200) {
            throw new IOException("Errore durante l'update. Codice: " + con.getResponseCode());
        }
    }

    public void deleteModel(String id) throws IOException {
        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection con = connectionFactory.create(url);
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        if (con.getResponseCode() != 204 && con.getResponseCode() != 200) {
            throw new IOException("Errore durante la cancellazione. Codice: " + con.getResponseCode());
        }
    }

    public List<Raw> loadAllRaws() throws IOException {
        URL url = new URL(RAW_URL);
        HttpURLConnection con = connectionFactory.create(url);
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());

        try (InputStream input = con.getInputStream()) {
            return mapper.readValue(input, new TypeReference<List<Raw>>() {});
        }
    }
}
