package com.kageguard.backend.controller;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.service.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Readings", description = "Sensor readings from the simulated KageGuard curtain")
public class ReadingController {

    private final ReadingService service;

    public ReadingController(ReadingService service) {
        this.service = service;
    }

    @Operation(summary = "Get the latest reading",
            description = "Returns the newest saved reading, or 204 No Content if nothing has been saved yet.")
    @GetMapping("/latest")
    public ResponseEntity<Reading> latest() {
        return service.latest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get recent readings",
            description = "Returns the newest readings first. The limit is kept between 1 and 500. Default is 20.")
    @GetMapping
    public List<Reading> recent(@RequestParam(defaultValue = "20") int limit) {
        return service.recent(limit);
    }

    @Operation(summary = "Send one sensor packet",
            description = "Send one packet as plain text, for example S:340,L:4,P:3,R:0,T:27,F:320. "
                    + "The packet is checked and saved. An invalid packet returns 400 with a message.")
    @PostMapping(consumes = "text/plain")
    public ResponseEntity<?> ingest(@RequestBody String packet) {
        try {
            return ResponseEntity.ok(service.ingest(packet));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}