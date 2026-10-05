package com.reactjava.shir;

import com.reactjava.shir.activity.ActivityDetails;
import com.reactjava.shir.activity.ActivityPeriod;
import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.activity.RewardPolicy;
import com.reactjava.shir.activity.TrackingMethod;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

final class ActivityFixtures {
    static final LocalDateTime START = LocalDateTime.of(2026, 9, 26, 10, 0);

    private ActivityFixtures() {
    }

    static ActivitySession session(long id, ActivityType type, Duration duration, int rate) {
        var period = new ActivityPeriod(START, START.plus(duration));
        TrackingMethod method = type == ActivityType.SLEEP ? TrackingMethod.MANUAL : TrackingMethod.TIMER;
        return new ActivitySession(id, "Session #" + id, period.end(), type, period,
                List.of("test"), new ActivityDetails(id, type, "Test activity", method),
                new RewardPolicy(id, type, rate));
    }
}
