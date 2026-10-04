package com.kageguard.backend.simulator;

import com.kageguard.backend.service.ReadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kageguard.simulator.enabled", havingValue = "true")
public class SimulatorRunner {

    private static final Logger log = LoggerFactory.getLogger(SimulatorRunner.class);

    private final CurtainSimulator simulator = new CurtainSimulator();
    private final ReadingService service;

    public SimulatorRunner(ReadingService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 1000)
    public void tick() {
        String packet = simulator.nextPacket();
        service.ingest(packet);
        log.info("Simulated packet: {}", packet);
    }
}