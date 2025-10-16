package client_group.controller;

import client_group.model.Session;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;

public class NoticeEventListener {
    private final String serverUrl;
    private final NoticeUpdateHandler handler;
    private Thread listenerThread;

    // Permette di fornire uno stream "finto" nei test
    private final Supplier<InputStream> inputStreamSupplier;

    // in modo che il controller stesso possa decidere il proprio handler
    public interface NoticeUpdateHandler {
        void onNoticesUpdated();
    }

    public NoticeEventListener(String serverUrl, NoticeUpdateHandler handler) {
        this(serverUrl, handler, null);
    }

    // Costruttore usato solo nei test
    NoticeEventListener(String serverUrl, NoticeUpdateHandler handler, Supplier<InputStream> supplier) {
        this.serverUrl = serverUrl;
        this.handler = handler;
        this.inputStreamSupplier = supplier;
    }

    public void start() {
        listenerThread = new Thread(() -> {
            try {
                InputStream in;
                if (inputStreamSupplier != null) {
                    // nei test usiamo lo stream finto
                    in = inputStreamSupplier.get();
                } else {
                    // in produzione apriamo la connessione vera
                    URL url = new URL(serverUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "text/event-stream");
                    conn.setRequestProperty("Authorization", "Bearer " + Session.getInstance().getToken());
                    in = conn.getInputStream();
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            Platform.runLater(handler::onNoticesUpdated);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenerThread.setDaemon(true); // daemon = funziona in background
        listenerThread.start();
    }

    public void stop() {
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }
}
