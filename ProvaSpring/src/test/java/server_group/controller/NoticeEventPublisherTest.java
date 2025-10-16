package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NoticeEventPublisherTest {

    @InjectMocks
    private NoticeEventPublisher noticeEventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- TEST REGISTER CLIENT ---
    @Test
    void registerClient_shouldAddEmitter() {
        // Act
        SseEmitter emitter = noticeEventPublisher.registerClient();

        // Assert
        assertNotNull(emitter);
    }

    // --- TEST PUBLISH EVENT WITH ACTIVE EMITTER ---
    @Test
    void publishEvent_withActiveEmitter_shouldSendEvent() {
        // Arrange
        SseEmitter emitter = noticeEventPublisher.registerClient();

        // Act & Assert (no eccezione = successo)
        assertDoesNotThrow(() -> noticeEventPublisher.publishEvent("Test notice"));
    }
}
