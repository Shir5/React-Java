package com.reactjava.shir.generator;

import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.activity.RewardPolicy;

import java.util.Objects;
import java.util.Random;

public final class RewardPolicyGenerator implements ObjectGenerator<RewardPolicy> {
    private static final ActivityType[] TYPES = ActivityType.values();
    private final Random random;
    private long nextId = 1;

    public RewardPolicyGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public RewardPolicy next() {
        long id = nextId;
        nextId = Math.incrementExact(nextId);
        ActivityType type = TYPES[(int) ((id - 1) % TYPES.length)];
        return new RewardPolicy(id, type, 1 + random.nextInt(5));
    }
}
