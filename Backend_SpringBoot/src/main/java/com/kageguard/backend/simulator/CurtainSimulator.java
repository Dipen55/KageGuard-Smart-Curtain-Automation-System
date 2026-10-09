package com.kageguard.backend.simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CurtainSimulator {

    private final Random random;

    private int tick = 0;
    private int lastLevel = 0;
    private int stableLevel = 0;
    private int stabilityCounter = 0;
    private int position = 0;
    private boolean rain = false;
    private int fireTicksLeft = 0;

    // null means automatic mode. A number from 0 to 6 means "hold the curtain here".
    private Integer manualLevel = null;

    @Autowired
    public CurtainSimulator() {
        this.random = new Random();
    }

    // Used by tests, so the random numbers are repeatable
    public CurtainSimulator(long seed) {
        this.random = new Random(seed);
    }

    // Returns one packet like "S:340,L:4,P:3,R:0,T:27,F:320"
    public synchronized String nextPacket() {
        tick++;

        // Light follows a slow day/night wave with a little noise
        int light = (int) (500 + 450 * Math.sin(tick / 40.0)) + random.nextInt(31) - 15;
        light = clamp(light, 0, 1023);

        // Rain button is pressed rarely
        if (random.nextInt(60) == 0) {
            rain = !rain;
        }

        // A fire event starts rarely and lasts 10 packets
        if (fireTicksLeft == 0 && random.nextInt(150) == 0) {
            fireTicksLeft = 10;
        }
        int temp;
        int flame;
        if (fireTicksLeft > 0) {
            fireTicksLeft--;
            temp = 55 + random.nextInt(10);
            flame = 700 + random.nextInt(200);
        } else {
            temp = 24 + random.nextInt(8);
            flame = 200 + random.nextInt(200);
        }

        // Same logic as the Arduino code. This keeps running in manual mode too,
        // so automatic mode resumes smoothly.
        int level = calculateLevel(light);
        if (level == lastLevel) {
            stabilityCounter++;
        } else {
            stabilityCounter = 0;
        }
        if (stabilityCounter >= 4) {
            stableLevel = level;
        }
        lastLevel = level;

        // Priority: fire, then manual command, then rain, then light
        boolean fire = temp > 45 && flame > 650;
        int target;
        if (fire) {
            target = 0;
        } else if (manualLevel != null) {
            target = manualLevel;
        } else if (rain) {
            target = 6;
        } else {
            target = stableLevel;
        }

        // Curtain moves one step per packet
        if (position < target) {
            position++;
        } else if (position > target) {
            position--;
        }

        return String.format("S:%d,L:%d,P:%d,R:%d,T:%d,F:%d",
                light, target, position, rain ? 1 : 0, temp, flame);
    }

    // Hold the curtain at a level from 0 (fully open) to 6 (fully closed)
    public synchronized void setManualLevel(int level) {
        if (level < 0 || level > 6) {
            throw new IllegalArgumentException("Level must be between 0 and 6");
        }
        this.manualLevel = level;
    }

    // Go back to automatic control
    public synchronized void setAuto() {
        this.manualLevel = null;
    }

    public synchronized ControlState state() {
        return new ControlState(manualLevel == null ? "AUTO" : "MANUAL", manualLevel);
    }

    static int calculateLevel(int value) {
        if (value <= 78) return 0;
        if (value <= 156) return 1;
        if (value <= 234) return 2;
        if (value <= 312) return 3;
        if (value <= 390) return 4;
        if (value <= 468) return 5;
        return 6;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}