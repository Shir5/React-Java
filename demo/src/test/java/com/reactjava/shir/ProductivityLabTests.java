package com.reactjava.shir;

import com.reactjava.shir.activity.ActivityDetails;
import com.reactjava.shir.activity.ActivityPeriod;
import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.activity.RewardPolicy;
import com.reactjava.shir.activity.TrackingMethod;
import com.reactjava.shir.generator.ActivityDetailsGenerator;
import com.reactjava.shir.generator.ActivitySessionGenerator;
import com.reactjava.shir.generator.RewardPolicyGenerator;
import com.reactjava.shir.statistics.ActivityStatistics;
import com.reactjava.shir.statistics.ActivityStatisticsCalculator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductivityLabTests {
    @Test
    void generatorsCreateRequestedNumberOfUniqueValidObjects() {
        List<ActivityDetails> details = new ActivityDetailsGenerator(new Random(1)).generate(25);
        List<RewardPolicy> policies = new RewardPolicyGenerator(new Random(2)).generate(25);
        List<ActivitySession> sessions =
                new ActivitySessionGenerator(new Random(3), details, policies).generate(5_000);

        assertEquals(25, new HashSet<>(details.stream().map(detail -> detail.id()).toList()).size());
        assertEquals(25, new HashSet<>(policies.stream().map(policy -> policy.id()).toList()).size());
        assertEquals(5_000, new HashSet<>(sessions.stream().map(session -> session.id()).toList()).size());
        assertTrue(sessions.stream().allMatch(session ->
                session.period().end().isAfter(session.period().start())
                        && session.completedAt().equals(session.period().end())
                        && session.type() == session.details().type()
                        && session.type() == session.rewardPolicy().type()
                        && session.rewardPolicy().coinsPerMinute() > 0
                        && !session.tags().isEmpty()));
    }

    @Test
    void allCalculationMethodsProduceExpectedResult() {
        ActivityDetails details = new ActivityDetails(1, ActivityType.READING,
                "Read a Java book", TrackingMethod.TIMER);
        RewardPolicy policy = new RewardPolicy(1, ActivityType.READING, 3);
        LocalDateTime start = LocalDateTime.of(2026, 9, 26, 10, 0);
        ActivityPeriod period = new ActivityPeriod(start, start.plusMinutes(40));
        ActivitySession session = new ActivitySession(1, "Java reading", period.end(),
                ActivityType.READING, period, List.of("java", "book"), details, policy);
        List<ActivitySession> sessions = List.of(session);

        ActivityStatistics expected = new ActivityStatistics(1, 40, 120);

        assertEquals(expected, ActivityStatisticsCalculator.withLoop(sessions));
        assertEquals(expected, ActivityStatisticsCalculator.withStandardCollectors(sessions));
        assertEquals(expected, ActivityStatisticsCalculator.withCustomCollector(sessions));
        assertEquals(expected, ActivityStatisticsCalculator.withTeeingCollector(sessions));
    }

    @Test
    void allCalculationMethodsSupportEmptyCollection() {
        ActivityStatistics expected = ActivityStatistics.empty();

        assertEquals(expected, ActivityStatisticsCalculator.withLoop(List.of()));
        assertEquals(expected, ActivityStatisticsCalculator.withStandardCollectors(List.of()));
        assertEquals(expected, ActivityStatisticsCalculator.withCustomCollector(List.of()));
        assertEquals(expected, ActivityStatisticsCalculator.withTeeingCollector(List.of()));
    }

    @Test
    void invalidDomainValuesAreRejected() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 26, 10, 0);

        assertThrows(IllegalArgumentException.class, () -> new ActivityPeriod(start, start));
        assertThrows(IllegalArgumentException.class,
                () -> new RewardPolicy(1, ActivityType.SPORT, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ActivityDetails(1, ActivityType.READING, "Book", TrackingMethod.CAMERA));
        assertThrows(IllegalArgumentException.class,
                () -> new ActivityDetailsGenerator(new Random()).generate(-1));
    }

    @Test
    void partialMinutesAreAcceptedAndRoundedUp() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 26, 10, 0);
        ActivityPeriod period = new ActivityPeriod(start, start.plusMinutes(5).plusSeconds(59));
        RewardPolicy policy = new RewardPolicy(1, ActivityType.READING, 3);

        assertEquals(java.time.Duration.ofMinutes(5).plusSeconds(59), period.duration());
        assertEquals(6, policy.rewardedMinutes(period));
        assertEquals(18, policy.calculateReward(period));
        assertEquals(1, policy.rewardedMinutes(new ActivityPeriod(start, start.plusNanos(1))));
    }
}
