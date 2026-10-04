package com.kageguard.backend.controller;

import com.kageguard.backend.alert.AlertService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // Sends a test message to every enabled channel
    @PostMapping("/test")
    public List<String> test() {
        return alertService.sendTest();
    }
}