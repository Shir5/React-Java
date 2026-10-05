package com.reactjava.shir.generator;

import com.reactjava.shir.activity.ActivityDetails;
import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.activity.TrackingMethod;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public final class ActivityDetailsGenerator implements ObjectGenerator<ActivityDetails> {
    private static final ActivityType[] TYPES = ActivityType.values();
    private final Random random;
    private long nextId = 1;

    public ActivityDetailsGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public ActivityDetails next() {
        long id = nextId;

        nextId = Math.incrementExact(nextId);

        ActivityType type = TYPES[(int) ((id - 1) % TYPES.length)];
        
        TrackingMethod[] allowed = Arrays.stream(TrackingMethod.values())
                .filter(method -> method.supports(type))
                .toArray(size -> new TrackingMethod[size]);
        TrackingMethod method = allowed[random.nextInt(allowed.length)];
        return new ActivityDetails(id, type, description(type) + " #" + id, method);
    }

    private static String description(ActivityType type) {
        return switch (type) {
            case READING -> "Reading a book";
            case SPORT -> "Training";
            case WALKING -> "Walking";
            case SLEEP -> "Sleeping";
            case HOBBY -> "Practicing a hobby";
        };
    }
}
