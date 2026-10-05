package com.reactjava.shir.activity;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

@Value
@Accessors(fluent = true)
public class ActivityDetails {
    long id;
    ActivityType type;
    String description;
    TrackingMethod trackingMethod;

    public ActivityDetails(long id, ActivityType type, String description, TrackingMethod trackingMethod) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }

        this.type = Objects.requireNonNull(type, "type");
        this.trackingMethod = Objects.requireNonNull(trackingMethod, "trackingMethod");

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }

        if (!trackingMethod.supports(type)) {
            throw new IllegalArgumentException("Tracking method does not support activity type");
        }

        this.id = id;
        this.description = description;
    }
}
