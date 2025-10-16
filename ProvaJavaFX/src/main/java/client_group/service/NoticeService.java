package client_group.service;

import client_group.dto.NoticeDTO;
import client_group.model.Session;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class NoticeService {

    private final Supplier<HttpURLConnection> connectionSupplier;

    // Costruttore normale
    public NoticeService() {
        this.connectionSupplier = null;
    }

    // Costruttore per factory injection
    public NoticeService(Supplier<HttpURLConnection> connectionSupplier) {
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

    public List<NoticeDTO> fetchNotices(String userEmail) {
        List<NoticeDTO> notices = new ArrayList<>();

        try {
            HttpURLConnection conn = openConnection("http://localhost:8080/api/notice/all" + "?userEmail=" + URLEncoder.encode(userEmail, "UTF-8"), "GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                notices = Arrays.asList(mapper.readValue(conn.getInputStream(), NoticeDTO[].class));
            } else {
                System.err.println("Errore nel recupero annunci: " + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return notices;
    }

    public Optional<NoticeDTO> saveNotice(NoticeDTO notice) {
        try {
            HttpURLConnection conn = openConnection("http://localhost:8080/api/notice/add", "POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonInputString = objectMapper.writeValueAsString(notice);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == 200 || code == 201) {
                try (InputStream is = conn.getInputStream()) {
                    NoticeDTO savedNotice = objectMapper.readValue(is, NoticeDTO.class);
                    return Optional.of(savedNotice);
                }
            } else {
                System.err.println("Failed to save notice: HTTP code " + code);
                return Optional.empty();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean deleteNotice(long id) {
        try {
            HttpURLConnection conn = openConnection("http://localhost:8080/api/notice/delete/" + id, "DELETE");

            int code = conn.getResponseCode();
            if (code == 200 || code == 204) {
                return true;
            } else {
                System.err.println("Failed to delete notice: HTTP code " + code);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
