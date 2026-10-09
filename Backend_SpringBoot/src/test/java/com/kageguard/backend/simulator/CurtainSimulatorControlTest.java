package com.kageguard.backend.simulator;

import com.kageguard.backend.model.SensorReading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurtainSimulatorControlTest {

    @Test
    void startsInAutoMode() {
        ControlState state = new CurtainSimulator(7).state();
        assertEquals("AUTO", state.mode());
        assertNull(state.manualLevel());
    }

    @Test
    void manualModeIsReported() {
        CurtainSimulator sim = new CurtainSimulator(7);
        sim.setManualLevel(4);
        assertEquals("MANUAL", sim.state().mode());
        assertEquals(4, sim.state().manualLevel());
    }

    @Test
    void returningToAutoClearsTheLevel() {
        CurtainSimulator sim = new CurtainSimulator(7);
        sim.setManualLevel(4);
        sim.setAuto();
        assertEquals("AUTO", sim.state().mode());
        assertNull(sim.state().manualLevel());
    }

    @Test
    void rejectsLevelsOutsideZeroToSix() {
        CurtainSimulator sim = new CurtainSimulator(7);
        assertThrows(IllegalArgumentException.class, () -> sim.setManualLevel(-1));
        assertThrows(IllegalArgumentException.class, () -> sim.setManualLevel(7));
    }

    @Test
    void manualLevelIsFollowedUnlessThereIsFire() {
        CurtainSimulator sim = new CurtainSimulator(7);
        sim.setManualLevel(4);

        for (int i = 0; i < 600; i++) {
            SensorReading r = SensorReading.parse(sim.nextPacket());
            if (r.fireDetected()) {
                assertEquals(0, r.level());   // fire always wins
            } else {
                assertEquals(4, r.level());   // otherwise the manual level, even in rain
            }
        }
    }

    @Test
    void curtainReachesTheManualLevel() {
        CurtainSimulator sim = new CurtainSimulator(7);
        sim.setManualLevel(6);

        boolean reached = false;
        for (int i = 0; i < 200; i++) {
            SensorReading r = SensorReading.parse(sim.nextPacket());
            if (r.position() == 6) {
                reached = true;
            }
        }
        assertTrue(reached);
    }

    @Test
    void curtainStillMovesOneStepAtATimeInManualMode() {
        CurtainSimulator sim = new CurtainSimulator(7);
        sim.setManualLevel(6);

        int previous = 0;
        for (int i = 0; i < 300; i++) {
            SensorReading r = SensorReading.parse(sim.nextPacket());
            assertTrue(Math.abs(r.position() - previous) <= 1);
            previous = r.position();
        }
    }
}