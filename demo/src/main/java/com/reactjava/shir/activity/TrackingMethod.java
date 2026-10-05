package com.reactjava.shir.activity;

public enum TrackingMethod {
    MANUAL,
    TIMER,
    CAMERA,
    PHONE_SENSOR;

    public boolean supports(ActivityType type) {
        return switch (type) {
            case READING, HOBBY -> this == MANUAL || this == TIMER;
            case SPORT -> this == TIMER || this == CAMERA;
            case WALKING -> this == TIMER || this == PHONE_SENSOR;
            case SLEEP -> this == MANUAL || this == PHONE_SENSOR;
        };
    }
}
