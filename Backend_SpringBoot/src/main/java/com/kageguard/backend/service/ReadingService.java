package com.kageguard.backend.service;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.model.SensorReading;
import com.kageguard.backend.repository.ReadingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReadingService {

    private final ReadingRepository repository;

    public ReadingService(ReadingRepository repository) {
        this.repository = repository;
    }

    // Checks the packet text, then saves it. Throws IllegalArgumentException if the packet is bad.
    public Reading ingest(String packet) {
        SensorReading parsed = SensorReading.parse(packet);
        return repository.save(Reading.from(parsed));
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