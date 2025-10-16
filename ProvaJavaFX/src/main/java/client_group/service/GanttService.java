package client_group.service;

import client_group.dto.GanttBlockDTO;
import client_group.model.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

public class GanttService {

    private final Supplier<HttpURLConnection> connectionSupplier;

    // Costruttore normale
    public GanttService() {
        this.connectionSupplier = null;
    }

    // Costruttore per factory injection
    GanttService(Supplier<HttpURLConnection> supplier) {
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

    public List<GanttBlockDTO> getGanttForAllOrders() throws Exception {
        HttpURLConnection conn = openConnection("http://localhost:8080/api/gantt/orders/all", "GET");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try (InputStream input = conn.getInputStream()) {
            return mapper.readValue(input, new TypeReference<List<GanttBlockDTO>>() {});
        }
    }

    public void saveModifiedBlocks(List<GanttBlockDTO> modifiedBlocks) throws IOException {
        try {
            HttpURLConnection conn = openConnection("http://localhost:8080/api/gantt/update", "POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            try (OutputStream os = conn.getOutputStream()) {
                mapper.writeValue(os, modifiedBlocks);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Errore nella conferma delle modifiche: " + responseCode);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
