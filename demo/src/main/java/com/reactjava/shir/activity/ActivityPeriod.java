package com.reactjava.shir.activity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public record ActivityPeriod(LocalDateTime start, LocalDateTime end) {
    public ActivityPeriod {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Period end must be after its start");
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }
}
