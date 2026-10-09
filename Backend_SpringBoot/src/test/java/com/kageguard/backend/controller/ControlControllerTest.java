package com.kageguard.backend.controller;

import com.kageguard.backend.simulator.CurtainSimulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControlControllerTest {

    private CurtainSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new CurtainSimulator();
    }

    private MockMvc mockMvc(boolean simulatorEnabled) {
        return MockMvcBuilders.standaloneSetup(new ControlController(simulator, simulatorEnabled)).build();
    }

    @Test
    void stateStartsAsAuto() throws Exception {
        mockMvc(true).perform(get("/api/control"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("AUTO"));
    }

    @Test
    void openSetsLevelZero() throws Exception {
        mockMvc(true).perform(post("/api/control/manual").param("level", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("MANUAL"))
                .andExpect(jsonPath("$.manualLevel").value(0));
    }

    @Test
    void closeSetsLevelSix() throws Exception {
        mockMvc(true).perform(post("/api/control/manual").param("level", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manualLevel").value(6));
        assertEquals(6, simulator.state().manualLevel());
    }

    @Test
    void autoReturnsToAutomaticMode() throws Exception {
        MockMvc mvc = mockMvc(true);
        mvc.perform(post("/api/control/manual").param("level", "3")).andExpect(status().isOk());

        mvc.perform(post("/api/control/auto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("AUTO"));
        assertNull(simulator.state().manualLevel());
    }

    @Test
    void levelAboveSixIsRejected() throws Exception {
        mockMvc(true).perform(post("/api/control/manual").param("level", "9"))
                .andExpect(status().isBadRequest());
        assertEquals("AUTO", simulator.state().mode());
    }

    @Test
    void negativeLevelIsRejected() throws Exception {
        mockMvc(true).perform(post("/api/control/manual").param("level", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingLevelIsRejected() throws Exception {
        mockMvc(true).perform(post("/api/control/manual"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void commandsAreRefusedWhenTheSimulatorIsOff() throws Exception {
        MockMvc mvc = mockMvc(false);

        mvc.perform(post("/api/control/manual").param("level", "2"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/control/auto"))
                .andExpect(status().isConflict());
        assertEquals("AUTO", simulator.state().mode());
    }
}