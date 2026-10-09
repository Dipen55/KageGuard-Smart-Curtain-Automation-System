package com.kageguard.backend.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorReadingTest {

    @Test
    void parsesNormalPacket() {
        SensorReading r = SensorReading.parse("S:340,L:4,P:3,R:0,T:27,F:320");
        assertEquals(340, r.analog());
        assertEquals(4, r.level());
        assertEquals(3, r.position());
        assertFalse(r.rain());
        assertEquals(27, r.temp());
        assertEquals(320, r.flame());
        assertFalse(r.fireDetected());
    }

    @Test
    void parsesRainPacket() {
        SensorReading r = SensorReading.parse("S:100,L:6,P:2,R:1,T:30,F:200");
        assertTrue(r.rain());
        assertEquals(6, r.level());
    }

    @Test
    void detectsFire() {
        SensorReading r = SensorReading.parse("S:500,L:0,P:4,R:0,T:60,F:800");
        assertTrue(r.fireDetected());
    }

    @Test
    void highTempAloneIsNotFire() {
        SensorReading r = SensorReading.parse("S:500,L:5,P:5,R:0,T:60,F:300");
        assertFalse(r.fireDetected());
    }

    @Test
    void handlesLineEnding() {
        SensorReading r = SensorReading.parse("S:340,L:4,P:3,R:0,T:27,F:320\r\n");
        assertEquals(340, r.analog());
    }

    @Test
    void calculatesClosedPercent() {
        SensorReading r = SensorReading.parse("S:340,L:4,P:3,R:0,T:27,F:320");
        assertEquals(50.0, r.closedPercent());
    }

    @Test
    void rejectsMissingField() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SensorReading.parse("S:340,L:4,P:3,R:0,T:27"));
        assertNotNull(ex);
    }

    @Test
    void rejectsOutOfRangeValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SensorReading.parse("S:2000,L:4,P:3,R:0,T:27,F:320"));
        assertNotNull(ex);
    }

    @Test
    void rejectsNonNumber() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SensorReading.parse("S:abc,L:4,P:3,R:0,T:27,F:320"));
        assertNotNull(ex);
    }

    @Test
    void rejectsEmptyInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> SensorReading.parse(""));
        assertNotNull(ex);
    }
}