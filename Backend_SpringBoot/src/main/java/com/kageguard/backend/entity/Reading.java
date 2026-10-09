package com.kageguard.backend.entity;

import com.kageguard.backend.model.SensorReading;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "readings")
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant recordedAt;
    private int lightValue;
    private int targetLevel;
    private int curtainPosition;
    private boolean rainActive;
    private int tempC;
    private int flameValue;
    private boolean fireDetected;

    protected Reading() {
        // needed by JPA
    }

    public static Reading from(SensorReading r) {
        Reading e = new Reading();
        e.recordedAt = Instant.now();
        e.lightValue = r.analog();
        e.targetLevel = r.level();
        e.curtainPosition = r.position();
        e.rainActive = r.rain();
        e.tempC = r.temp();
        e.flameValue = r.flame();
        e.fireDetected = r.fireDetected();
        return e;
    }

    public Long getId() { return id; }
    public Instant getRecordedAt() { return recordedAt; }
    public int getLightValue() { return lightValue; }
    public int getTargetLevel() { return targetLevel; }
    public int getCurtainPosition() { return curtainPosition; }
    public boolean isRainActive() { return rainActive; }
    public int getTempC() { return tempC; }
    public int getFlameValue() { return flameValue; }
    public boolean isFireDetected() { return fireDetected; }
}