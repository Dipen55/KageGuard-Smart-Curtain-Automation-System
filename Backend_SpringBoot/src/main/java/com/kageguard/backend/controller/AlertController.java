package com.kageguard.backend.controller;

import com.kageguard.backend.alert.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Telegram and Gmail alerts for fire and rain")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @Operation(summary = "Send a test alert",
            description = "Sends a real test message to every enabled channel (Telegram and Gmail) "
                    + "and reports the result for each one.")
    @PostMapping("/test")
    public List<String> test() {
        return alertService.sendTest();
    }
}