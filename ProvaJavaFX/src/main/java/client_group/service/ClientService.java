package client_group.service;

import client_group.model.Client;
import client_group.model.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

public class ClientService {

    private final String BASE_URL = "http://localhost:8080/api/clients";
    private final ObjectMapper mapper = new ObjectMapper();

    // factory injection
    private final Supplier<HttpURLConnection> connectionSupplier;

    // costruttore normale
    public ClientService() {
        this.connectionSupplier = null;
    }

    // costruttore per test con injection
    ClientService(Supplier<HttpURLConnection> supplier) {
        this.connectionSupplier = supplier;
    }

    private HttpURLConnection openConnection(String urlString, String method) throws Exception {
        HttpURLConnection con;
        if (connectionSupplier != null) {
            con = connectionSupplier.get();
        } else {
            URL url = new URL(urlString);
            con = (HttpURLConnection) url.openConnection();
        }
        con.setRequestMethod(method);
        con.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());
        return con;
    }

    public List<Client> fetchAllClients() {
        try {
            HttpURLConnection con = openConnection(BASE_URL, "GET");
            con.setRequestProperty("Accept", "application/json");

            if (con.getResponseCode() != 200) {
                throw new RuntimeException("Errore nel recupero clienti: HTTP code " + con.getResponseCode());
            }

            try (InputStream input = con.getInputStream()) {
                return mapper.readValue(input, new TypeReference<List<Client>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // lista vuota in caso di errore
        }
    }

    public void deleteClient(String piva) throws Exception {
        HttpURLConnection con = openConnection(BASE_URL + "/" + piva, "DELETE");

        int responseCode = con.getResponseCode();
        con.disconnect();

        if (responseCode != 200 && responseCode != 204) {
            throw new RuntimeException("Errore nell'eliminazione del client con PIVA " + piva);
        }
    }

    public Client createClient(Client client) throws Exception {
        HttpURLConnection con = openConnection(BASE_URL + "/create_client", "POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");

        // serializza il client in JSON e lo scrive nel body
        try (OutputStream os = con.getOutputStream()) {
            mapper.writeValue(os, client);
        }

        int responseCode = con.getResponseCode();

        if (responseCode < 200 || responseCode > 300) {
            throw new RuntimeException("Errore nella creazione del client: HTTP code " + responseCode);
        }

        try (InputStream input = con.getInputStream()) {
            return mapper.readValue(input, Client.class);
        }
    }

    public Client updateClient(Client client) throws Exception {
        HttpURLConnection con = openConnection(BASE_URL + "/" + client.getPiva(), "PUT");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = con.getOutputStream()) {
            mapper.writeValue(os, client);
        }

        int responseCode = con.getResponseCode();

        if (responseCode != 200) {
            throw new RuntimeException("Errore nell'aggiornamento del client: HTTP code " + responseCode);
        }

        try (InputStream input = con.getInputStream()) {
            return mapper.readValue(input, Client.class);
        }
    }
}
