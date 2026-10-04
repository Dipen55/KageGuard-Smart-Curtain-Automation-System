package com.kageguard.backend.controller;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.service.ReadingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class ReadingController {

    private final ReadingService service;

    public ReadingController(ReadingService service) {
        this.service = service;
    }

    // Latest reading
    @GetMapping("/latest")
    public ResponseEntity<Reading> latest() {
        return service.latest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // Recent readings, newest first. Example: /api/readings?limit=10
    @GetMapping
    public List<Reading> recent(@RequestParam(defaultValue = "20") int limit) {
        return service.recent(limit);
    }

    // Send one packet as plain text
    @PostMapping(consumes = "text/plain")
    public ResponseEntity<?> ingest(@RequestBody String packet) {
        try {
            return ResponseEntity.ok(service.ingest(packet));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}