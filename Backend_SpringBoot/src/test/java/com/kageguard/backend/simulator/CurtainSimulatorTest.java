package com.kageguard.backend.simulator;

import com.kageguard.backend.model.SensorReading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CurtainSimulatorTest {

    @Test
    void everyPacketCanBeParsed() {
        CurtainSimulator sim = new CurtainSimulator(42);
        for (int i = 0; i < 1000; i++) {
            SensorReading r = SensorReading.parse(sim.nextPacket());
            assertTrue(r.position() >= 0 && r.position() <= 6);
        }
    }

    @Test
    void curtainMovesAtMostOneStepPerPacket() {
        CurtainSimulator sim = new CurtainSimulator(42);
        int previous = 0;
        for (int i = 0; i < 1000; i++) {
            SensorReading r = SensorReading.parse(sim.nextPacket());
            assertTrue(Math.abs(r.position() - previous) <= 1);
            previous = r.position();
        }
    }
}