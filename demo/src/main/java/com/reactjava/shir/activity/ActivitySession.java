package com.reactjava.shir.activity;

import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Value
@Accessors(fluent = true)
public class ActivitySession {
    long id;
    String title;
    LocalDateTime completedAt;
    ActivityType type;
    ActivityPeriod period;
    List<String> tags;
    ActivityDetails details;
    RewardPolicy rewardPolicy;

    public ActivitySession(long id, String title, LocalDateTime completedAt, ActivityType type,
                           ActivityPeriod period, List<String> tags, ActivityDetails details,
                           RewardPolicy rewardPolicy) {
        if (id <= 0 || title == null || title.isBlank()) {
            throw new IllegalArgumentException("id must be positive and title must not be blank");
        }
        this.type = Objects.requireNonNull(type, "type");
        this.period = Objects.requireNonNull(period, "period");
        this.details = Objects.requireNonNull(details, "details");
        this.rewardPolicy = Objects.requireNonNull(rewardPolicy, "rewardPolicy");
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt");
        this.tags = List.copyOf(Objects.requireNonNull(tags, "tags"));

        if (!completedAt.equals(period.end()) || details.type() != type || rewardPolicy.type() != type) {
            throw new IllegalArgumentException("Session attributes must match its type and completion time");
        }
        this.id = id;
        this.title = title;
    }

    public Duration duration() {
        return period.duration();
    }

    public long rewardedMinutes() {
        return rewardPolicy.rewardedMinutes(period);
    }

    public long earnedCoins() {
        return rewardPolicy.calculateReward(period);
    }
}
