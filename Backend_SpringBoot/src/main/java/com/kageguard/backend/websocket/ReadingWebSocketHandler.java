package com.kageguard.backend.websocket;

import java.io.IOException;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.event.ReadingSavedEvent;
import com.kageguard.backend.repository.ReadingRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ReadingWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ReadingWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ReadingRepository repository;

    public ReadingWebSocketHandler(ReadingRepository repository) {
        this.repository = repository;
    }

    // A dashboard connected: remember it and send the latest reading right away
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Dashboard connected ({} open)", sessions.size());
        repository.findFirstByOrderByIdDesc().ifPresent(r -> send(session, toJson(r)));
    }

    // A dashboard left: forget it
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("Dashboard disconnected ({} open)", sessions.size());
    }

    // Runs every time ReadingService saves a reading
    @EventListener
    public void onReadingSaved(ReadingSavedEvent event) {
        if (sessions.isEmpty()) {
            return;
        }
        String json = toJson(event.reading());
        for (WebSocketSession session : sessions) {
            send(session, json);
        }
    }

    private void send(WebSocketSession session, String json) {
        try {
            synchronized (session) {   // one message at a time per connection
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            sessions.remove(session);
            log.warn("Removed a broken connection: {}", e.getMessage());
        }
    }

    // Builds the JSON by hand so it matches the /api/readings format
    static String toJson(Reading r) {
        return String.format(Locale.ROOT,
                "{\"id\":%d,\"recordedAt\":\"%s\",\"lightValue\":%d,\"targetLevel\":%d,"
                        + "\"curtainPosition\":%d,\"rainActive\":%b,\"tempC\":%d,"
                        + "\"flameValue\":%d,\"fireDetected\":%b}",
                r.getId(), r.getRecordedAt(), r.getLightValue(), r.getTargetLevel(),
                r.getCurtainPosition(), r.isRainActive(), r.getTempC(),
                r.getFlameValue(), r.isFireDetected());
    }
}