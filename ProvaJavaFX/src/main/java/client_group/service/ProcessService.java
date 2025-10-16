package client_group.service;

import client_group.dto.ModelWithStepsDTO;
import client_group.model.Machinery;
import client_group.model.ProcessStep;
import client_group.model.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

public class ProcessService {

    private final MachineryService machineryService;
    private final Supplier<HttpURLConnection> connectionSupplier;

    // Costruttore
    public ProcessService() {
        this.machineryService = new MachineryService();
        this.connectionSupplier = null;
    }

    // Costruttore per factory injection
    public ProcessService(MachineryService machineryService, Supplier<HttpURLConnection> connectionSupplier) {
        this.machineryService = machineryService;
        this.connectionSupplier = connectionSupplier;
    }

    // Metodo che apre la connessione con factory injection
    private HttpURLConnection openConnection(String urlString, String method) throws Exception {
        HttpURLConnection con;
        if (connectionSupplier != null) {
            con = connectionSupplier.get();  // Usa il Supplier
        } else {
            URL url = new URL(urlString);
            con = (HttpURLConnection) url.openConnection();  // Connessione reale
        }
        con.setRequestMethod(method);
        con.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());
        return con;
    }

    public List<ModelWithStepsDTO> getAllModelWithSteps() throws Exception {
        HttpURLConnection conn = openConnection("http://localhost:8080/api/models/processes", "GET");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try (InputStream input = conn.getInputStream()) {
            return mapper.readValue(input, new TypeReference<List<ModelWithStepsDTO>>() {});
        }
    }

    public boolean deleteById(Long id) {
        try {
            HttpURLConnection conn = openConnection("http://localhost:8080/api/process/" + id, "DELETE");

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStepById(Long stepId, Duration duration, String semifinishedName, String machineryName) throws Exception {
        HttpURLConnection conn = openConnection("http://localhost:8080/api/process/" + stepId + "/updateStep", "PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Ricava l'oggetto Machinery corrispondente al nome
        Machinery machinery = machineryService.getMachineryByName(machineryName);
        if (machinery == null) {
            throw new IllegalArgumentException("Invalid machinery name: " + machineryName);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // crea JSON per aggiornare lo step
        ObjectNode json = mapper.createObjectNode();
        json.put("duration", duration.toString());
        json.put("semifinishedName", semifinishedName);
        ObjectNode machineryNode = mapper.createObjectNode();
        machineryNode.put("id", machinery.getId());
        json.set("machinery", machineryNode);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = mapper.writeValueAsBytes(json);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        conn.disconnect();

        return responseCode == 200;
    }

    public ProcessStep addNewStepToModel(String modelName, Duration duration, String semifinishedName, String machineryName, int insertAfterOrder) throws Exception {
        String endpoint = String.format("http://localhost:8080/api/process/add?modelName=%s&afterOrder=%d&duration=%s&semifinishedName=%s&machineryName=%s",
                URLEncoder.encode(modelName, StandardCharsets.UTF_8),
                insertAfterOrder,
                URLEncoder.encode(duration.toString(), StandardCharsets.UTF_8),
                URLEncoder.encode(semifinishedName, StandardCharsets.UTF_8),
                URLEncoder.encode(machineryName, StandardCharsets.UTF_8)
        );

        HttpURLConnection conn = openConnection(endpoint, "POST");

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            try (InputStream in = conn.getInputStream()) {
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                return mapper.readValue(body, ProcessStep.class);
            }
        } else {
            throw new IOException("Errore: " + responseCode);
        }
    }
}
