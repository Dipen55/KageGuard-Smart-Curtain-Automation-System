package com.kageguard.backend.controller;

import com.kageguard.backend.alert.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlertControllerTest {

    private AlertService alertService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        alertService = Mockito.mock(AlertService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AlertController(alertService)).build();
    }

    @Test
    void testEndpointReportsTheResultOfEachChannel() throws Exception {
        when(alertService.sendTest()).thenReturn(List.of("telegram: sent", "email: sent"));

        mockMvc.perform(post("/api/alerts/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("telegram: sent"))
                .andExpect(jsonPath("$[1]").value("email: sent"));
    }

    @Test
    void testEndpointReportsFailuresToo() throws Exception {
        when(alertService.sendTest()).thenReturn(List.of("telegram: failed - chat not found"));

        mockMvc.perform(post("/api/alerts/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("telegram: failed - chat not found"));
    }

    @Test
    void testEndpointDoesNotAcceptGet() throws Exception {
        mockMvc.perform(get("/api/alerts/test"))
                .andExpect(status().isMethodNotAllowed());
    }
}