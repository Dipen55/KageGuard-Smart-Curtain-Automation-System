package com.kageguard.backend.controller;

import com.kageguard.backend.simulator.ControlState;
import com.kageguard.backend.simulator.CurtainSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control")
public class ControlController {

    private static final Logger log = LoggerFactory.getLogger(ControlController.class);

    private final CurtainSimulator simulator;
    private final boolean simulatorEnabled;

    public ControlController(CurtainSimulator simulator,
                             @Value("${kageguard.simulator.enabled:true}") boolean simulatorEnabled) {
        this.simulator = simulator;
        this.simulatorEnabled = simulatorEnabled;
    }

    // Current mode: AUTO or MANUAL, and the held level
    @GetMapping
    public ControlState state() {
        return simulator.state();
    }

    // Hold the curtain at a level: 0 = fully open, 6 = fully closed
    @PostMapping("/manual")
    public ResponseEntity<?> manual(@RequestParam int level) {
        if (!simulatorEnabled) {
            return simulatorOff();
        }
        try {
            simulator.setManualLevel(level);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        log.info("Manual control: curtain held at level {}", level);
        return ResponseEntity.ok(simulator.state());
    }

    // Return to automatic control
    @PostMapping("/auto")
    public ResponseEntity<?> auto() {
        if (!simulatorEnabled) {
            return simulatorOff();
        }
        simulator.setAuto();
        log.info("Control returned to automatic");
        return ResponseEntity.ok(simulator.state());
    }

    private ResponseEntity<String> simulatorOff() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("The simulator is turned off, so there is no curtain to control.");
    }
}