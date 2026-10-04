package com.kageguard.backend.websocket;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.model.SensorReading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadingWebSocketHandlerTest {

    @Test
    void jsonContainsAllFields() {
        Reading r = Reading.from(SensorReading.parse("S:340,L:4,P:3,R:0,T:27,F:320"));
        String json = ReadingWebSocketHandler.toJson(r);

        assertTrue(json.contains("\"lightValue\":340"));
        assertTrue(json.contains("\"targetLevel\":4"));
        assertTrue(json.contains("\"curtainPosition\":3"));
        assertTrue(json.contains("\"rainActive\":false"));
        assertTrue(json.contains("\"tempC\":27"));
        assertTrue(json.contains("\"flameValue\":320"));
        assertTrue(json.contains("\"fireDetected\":false"));
    }

    @Test
    void jsonShowsFire() {
        Reading r = Reading.from(SensorReading.parse("S:340,L:0,P:0,R:0,T:60,F:800"));
        assertTrue(ReadingWebSocketHandler.toJson(r).contains("\"fireDetected\":true"));
    }
}