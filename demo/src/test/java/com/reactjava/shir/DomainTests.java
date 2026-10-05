package com.reactjava.shir;

import com.reactjava.shir.activity.ActivityPeriod;
import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.activity.RewardPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.reactjava.shir.ActivityFixtures.START;
import static com.reactjava.shir.ActivityFixtures.session;
import static org.junit.jupiter.api.Assertions.*;

class DomainTests {
    @ParameterizedTest
    @CsvSource({"0,1,1", "59,999999999,1", "60,0,1", "60,1,2", "120,0,2"})
    void rewardedMinutesRoundUpAtBoundaries(long seconds, int nanos, long expectedMinutes) {
        var period = new ActivityPeriod(START, START.plusSeconds(seconds).plusNanos(nanos));
        var policy = new RewardPolicy(1, ActivityType.READING, 3);
        assertEquals(expectedMinutes, policy.rewardedMinutes(period));
        assertEquals(expectedMinutes * 3, policy.calculateReward(period));
    }

    @Test
    void sessionCopiesTagsAndRejectsModification() {
        ActivitySession original = session(1, ActivityType.READING, Duration.ofMinutes(10), 2);
        var tags = new ArrayList<>(List.of("java"));
        var copy = new ActivitySession(original.id(), original.title(), original.completedAt(), original.type(),
                original.period(), tags, original.details(), original.rewardPolicy());
        tags.clear();

        assertEquals(List.of("java"), copy.tags());
        assertThrows(UnsupportedOperationException.class, () -> copy.tags().add("book"));
    }

    @Test
    void inconsistentSessionAttributesAreRejected() {
        ActivitySession valid = session(1, ActivityType.READING, Duration.ofMinutes(10), 2);
        assertThrows(IllegalArgumentException.class, () -> new ActivitySession(valid.id(), valid.title(),
                valid.completedAt().plusSeconds(1), valid.type(), valid.period(), valid.tags(),
                valid.details(), valid.rewardPolicy()));
        assertThrows(IllegalArgumentException.class, () -> new ActivitySession(valid.id(), valid.title(),
                valid.completedAt(), ActivityType.SPORT, valid.period(), valid.tags(),
                valid.details(), valid.rewardPolicy()));
        assertThrows(IllegalArgumentException.class, () -> new ActivitySession(valid.id(), valid.title(),
                valid.completedAt(), valid.type(), valid.period(), valid.tags(), valid.details(),
                new RewardPolicy(2, ActivityType.SPORT, 3)));
    }

    @Test
    void rewardMultiplicationChecksOverflow() {
        var period = new ActivityPeriod(START, START.plusMinutes(Long.MAX_VALUE / Integer.MAX_VALUE + 1));
        var policy = new RewardPolicy(1, ActivityType.READING, Integer.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> policy.calculateReward(period));
    }
}
