package com.kageguard.backend.event;

import com.kageguard.backend.entity.Reading;

public record ReadingSavedEvent(Reading reading) {
}