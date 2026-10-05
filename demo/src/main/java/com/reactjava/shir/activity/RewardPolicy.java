package com.reactjava.shir.activity;

import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.Objects;

@Value
@Accessors(fluent = true)
public class RewardPolicy {
    long id;
    ActivityType type;
    int coinsPerMinute;

    public RewardPolicy(long id, ActivityType type, int coinsPerMinute) {
        if (id <= 0 || coinsPerMinute <= 0) {
            throw new IllegalArgumentException("id and coinsPerMinute must be positive");
        }
        this.id = id;
        this.type = Objects.requireNonNull(type, "type");
        this.coinsPerMinute = coinsPerMinute;
    }

    public long rewardedMinutes(ActivityPeriod period) {
        Duration duration = Objects.requireNonNull(period, "period").duration();
        long seconds = duration.getSeconds();
        long fullMinutes = seconds / 60;
        boolean hasStartedMinute = seconds % 60 != 0 || duration.getNano() != 0;
        return hasStartedMinute ? fullMinutes + 1 : fullMinutes;
    }

    public long calculateReward(ActivityPeriod period) {
        return Math.multiplyExact(rewardedMinutes(period), coinsPerMinute);
    }
}
