package com.reactjava.shir.generator;

import com.reactjava.shir.activity.ActivityDetails;
import com.reactjava.shir.activity.ActivityPeriod;
import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.activity.RewardPolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class ActivitySessionGenerator implements ObjectGenerator<ActivitySession> {
    private static final LocalDateTime FIRST_START = LocalDateTime.of(2025, 1, 1, 0, 0);

    private static final int MINUTES_IN_YEAR = 365 * 24 * 60;

    private final Random random;

    private final Map<ActivityType, List<ActivityDetails>> detailsByType;

    private final Map<ActivityType, List<RewardPolicy>> policiesByType;

    private final List<ActivityType> availableTypes = new ArrayList<>();

    private long nextId = 1;

    public ActivitySessionGenerator(Random random, List<ActivityDetails> details, List<RewardPolicy> policies) {
        this.random = Objects.requireNonNull(random, "random");

        Objects.requireNonNull(details, "details");

        Objects.requireNonNull(policies, "policies");

        detailsByType = groupDetailsByType(details);

        policiesByType = groupPoliciesByType(policies);

        for (ActivityType type : ActivityType.values()) {
            if (detailsByType.containsKey(type) && policiesByType.containsKey(type)) {
                availableTypes.add(type);
            }
        }
    }

    @Override
    public ActivitySession next() {
        if (availableTypes.isEmpty()) {
            throw new IllegalStateException("At least one activity type needs details and a reward policy");
        }

        ActivityType type = pickRandom(availableTypes);

        ActivityDetails activityDetails = pickRandom(detailsByType.get(type));

        RewardPolicy policy = pickRandom(policiesByType.get(type));

        int durationMinutes = randomDuration(type);

        int startMinute = random.nextInt(MINUTES_IN_YEAR);

        LocalDateTime start = FIRST_START.plusMinutes(startMinute);

        LocalDateTime end = start.plusMinutes(durationMinutes);

        ActivityPeriod period = new ActivityPeriod(start, end);

        long id = nextId;

        nextId = Math.incrementExact(nextId);

        String title = type + " session #" + id;

        List<String> tags = List.of(
                type.name().toLowerCase(Locale.ROOT),
                activityDetails.trackingMethod().name().toLowerCase(Locale.ROOT)
        );

        return new ActivitySession(
                id,
                title,
                end,
                type,
                period,
                tags,
                activityDetails,
                policy
        );
    }

    private Map<ActivityType, List<ActivityDetails>> groupDetailsByType(List<ActivityDetails> details) {
        Map<ActivityType, List<ActivityDetails>> groups = new EnumMap<>(ActivityType.class);

        for (ActivityDetails item : details) {
            ActivityType type = item.type();

            List<ActivityDetails> group = groups.get(type);

            if (group == null) {
                group = new ArrayList<>();

                groups.put(type, group);
            }

            group.add(item);
        }

        return groups;
    }

    private Map<ActivityType, List<RewardPolicy>> groupPoliciesByType(List<RewardPolicy> policies) {
        Map<ActivityType, List<RewardPolicy>> groups = new EnumMap<>(ActivityType.class);

        for (RewardPolicy item : policies) {
            ActivityType type = item.type();

            List<RewardPolicy> group = groups.get(type);

            if (group == null) {
                group = new ArrayList<>();
                groups.put(type, group);
            }

            group.add(item);
        }

        return groups;
    }

    private <T> T pickRandom(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }

    private int randomDuration(ActivityType type) {
        return switch (type) {
            case READING -> randomBetween(10, 120);

            case SPORT, HOBBY -> randomBetween(15, 180);

            case WALKING -> randomBetween(10, 240);

            case SLEEP -> randomBetween(240, 600);
        };
    }

    private int randomBetween(int minimum, int maximum) {
        return minimum + random.nextInt(maximum - minimum + 1);
    }
}
