package client_group.controller;

import javafx.application.Platform;
import org.junit.jupiter.api.*;
import java.io.ByteArrayInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class NoticeEventListenerTest {

    private NoticeEventListener.NoticeUpdateHandler handler;

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {

        }
    }

    @BeforeEach
    void setUp() {
        handler = mock(NoticeEventListener.NoticeUpdateHandler.class);
    }

    @Test
    void testStartAndHandlerCalledWithValidEvent() throws Exception {

        ByteArrayInputStream fakeStream = new ByteArrayInputStream("data: test\n\n".getBytes());

        NoticeEventListener listener =
                new NoticeEventListener("http://localhost/fake", handler, () -> fakeStream);

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(handler).onNoticesUpdated();

        listener.start();

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(handler, atLeastOnce()).onNoticesUpdated();
    }

    @Test
    void testStopInterruptsThread() {
        NoticeEventListener listener =
                new NoticeEventListener("http://localhost/fake", handler,
                        () -> new ByteArrayInputStream(new byte[0]));
        listener.start();
        listener.stop();

        Assertions.assertDoesNotThrow(listener::stop);
    }

    @Test
    void testHandlesExceptionGracefully() {
        NoticeEventListener listener =
                new NoticeEventListener("http://localhost/fake", handler,
                        () -> { throw new RuntimeException("boom"); });
        Assertions.assertDoesNotThrow(listener::start);
        listener.stop();
    }

    @Test
    void testMultipleLinesSomeValidSomeInvalid() throws Exception {
        String fakeData = "notaevent: ignore\n" +
                "data: first\n" +
                "garbage line\n" +
                "data: second\n";
        ByteArrayInputStream fakeStream = new ByteArrayInputStream(fakeData.getBytes());

        NoticeEventListener listener =
                new NoticeEventListener("http://localhost/fake", handler, () -> fakeStream);

        CountDownLatch latch = new CountDownLatch(2);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(handler).onNoticesUpdated();

        listener.start();

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(handler, atLeast(2)).onNoticesUpdated();
    }

    @Test
    void testRealConnectionFallback() {
        NoticeEventListener listener = new NoticeEventListener("http://localhost/fake", handler);

        Assertions.assertDoesNotThrow(listener::start);
        listener.stop();
    }
}
