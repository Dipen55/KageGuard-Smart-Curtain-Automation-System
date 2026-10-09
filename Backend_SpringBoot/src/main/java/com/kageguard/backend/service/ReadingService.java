package com.kageguard.backend.service;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.event.ReadingSavedEvent;
import com.kageguard.backend.model.SensorReading;
import com.kageguard.backend.repository.ReadingRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReadingService {

    private final ReadingRepository repository;
    private final ApplicationEventPublisher publisher;

    public ReadingService(ReadingRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    // Checks the packet text, saves it, then announces it. Throws IllegalArgumentException if the packet is bad.
    public Reading ingest(String packet) {
        SensorReading parsed = SensorReading.parse(packet);
        Reading saved = repository.save(Reading.from(parsed));
        publisher.publishEvent(new ReadingSavedEvent(saved));
        return saved;
    }

    public Optional<Reading> latest() {
        return repository.findFirstByOrderByIdDesc();
    }

    public List<Reading> recent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return repository
                .findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "id")))
                .getContent();
    }
}