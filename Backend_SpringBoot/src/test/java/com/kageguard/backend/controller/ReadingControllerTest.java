package com.kageguard.backend.controller;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.model.SensorReading;
import com.kageguard.backend.service.ReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReadingControllerTest {

    private static final String NORMAL = "S:340,L:4,P:3,R:0,T:27,F:320";
    private static final String FIRE = "S:340,L:0,P:0,R:0,T:60,F:800";

    private ReadingService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(ReadingService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReadingController(service)).build();
    }

    private Reading reading(String packet) {
        return Reading.from(SensorReading.parse(packet));
    }

    @Test
    void latestReturnsTheNewestReading() throws Exception {
        when(service.latest()).thenReturn(Optional.of(reading(NORMAL)));

        mockMvc.perform(get("/api/readings/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lightValue").value(340))
                .andExpect(jsonPath("$.targetLevel").value(4))
                .andExpect(jsonPath("$.curtainPosition").value(3))
                .andExpect(jsonPath("$.tempC").value(27))
                .andExpect(jsonPath("$.fireDetected").value(false));
    }

    @Test
    void latestReturns204WhenThereIsNoData() throws Exception {
        when(service.latest()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/readings/latest"))
                .andExpect(status().isNoContent());
    }

    @Test
    void recentReturnsAListAndPassesTheLimit() throws Exception {
        when(service.recent(2)).thenReturn(List.of(reading(FIRE), reading(NORMAL)));

        mockMvc.perform(get("/api/readings").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fireDetected").value(true))
                .andExpect(jsonPath("$[1].fireDetected").value(false));
    }

    @Test
    void recentUsesALimitOf20WhenNoneIsGiven() throws Exception {
        when(service.recent(20)).thenReturn(List.of(reading(NORMAL)));

        mockMvc.perform(get("/api/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void postingAValidPacketReturns200() throws Exception {
        when(service.ingest(NORMAL)).thenReturn(reading(NORMAL));

        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(NORMAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lightValue").value(340));
    }

    @Test
    void postingABadPacketReturns400WithTheReason() throws Exception {
        when(service.ingest("S:2000,L:4,P:3,R:0,T:27,F:320"))
                .thenThrow(new IllegalArgumentException("S out of range: 2000"));

        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("S:2000,L:4,P:3,R:0,T:27,F:320"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("S out of range: 2000"));
    }
}