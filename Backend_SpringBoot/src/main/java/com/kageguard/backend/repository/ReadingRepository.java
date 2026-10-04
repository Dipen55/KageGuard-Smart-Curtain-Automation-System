package com.kageguard.backend.repository;

import com.kageguard.backend.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    Optional<Reading> findFirstByOrderByIdDesc();
}