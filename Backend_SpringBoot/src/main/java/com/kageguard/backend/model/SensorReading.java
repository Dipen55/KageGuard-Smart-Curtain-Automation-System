package com.kageguard.backend.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SensorReading(int analog, int level, int position,
                            boolean rain, int temp, int flame) {

    // Turns "S:340,L:4,P:3,R:0,T:27,F:320" into a SensorReading
    public static SensorReading parse(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Packet is empty");
        }

        Map<String, Integer> values = new HashMap<>();
        for (String part : line.trim().split(",")) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) {
                throw new IllegalArgumentException("Bad field: " + part);
            }

            String key = kv[0].trim();
            String rawValue = kv[1].trim();
            try {
                values.put(key, Integer.valueOf(rawValue));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Not a number: " + part);
            }
        }

        for (String key : List.of("S", "L", "P", "R", "T", "F")) {
            if (!values.containsKey(key)) {
                throw new IllegalArgumentException("Missing field: " + key);
            }
        }

        SensorReading reading = new SensorReading(
                values.get("S"), values.get("L"), values.get("P"),
                values.get("R") == 1, values.get("T"), values.get("F"));
        reading.validate();
        return reading;
    }

    // Same rule as the Arduino code: T > 45 and F > 650
    public boolean fireDetected() {
        return temp > 45 && flame > 650;
    }

    // 0 = fully open, 100 = fully closed
    public double closedPercent() {
        return position * 100.0 / 6;
    }

    private void validate() {
        if (analog < 0 || analog > 1023) throw new IllegalArgumentException("S out of range: " + analog);
        if (flame < 0 || flame > 1023)   throw new IllegalArgumentException("F out of range: " + flame);
        if (level < 0 || level > 6)      throw new IllegalArgumentException("L out of range: " + level);
        if (position < 0 || position > 6) throw new IllegalArgumentException("P out of range: " + position);
        if (temp < -50 || temp > 150)    throw new IllegalArgumentException("T out of range: " + temp);
    }
}