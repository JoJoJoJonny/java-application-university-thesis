package server_group.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NoticeEventPublisher {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerClient() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // connessione lunga
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    public void publishEvent(Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("noticeUpdate")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
