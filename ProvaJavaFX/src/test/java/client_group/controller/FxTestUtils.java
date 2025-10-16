package client_group.controller;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FxTestUtils {

    public static void runAndWait(Runnable action) throws InterruptedException {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            CountDownLatch doneLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    action.run();
                } finally {
                    doneLatch.countDown();
                }
            });
            if (!doneLatch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting for FX runLater");
            }
        }
    }

    public static void waitForRunLater() throws InterruptedException {
        if (Platform.isFxApplicationThread()) {

            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for FX runLater");
        }
    }

}
