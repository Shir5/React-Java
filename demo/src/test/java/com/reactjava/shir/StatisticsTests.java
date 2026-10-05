package com.reactjava.shir;

import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.generator.ActivityDetailsGenerator;
import com.reactjava.shir.generator.ActivitySessionGenerator;
import com.reactjava.shir.generator.RewardPolicyGenerator;
import com.reactjava.shir.statistics.ActivityStatistics;
import com.reactjava.shir.statistics.ActivityStatisticsCalculator;
import com.reactjava.shir.statistics.ActivityStatisticsCollector;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static com.reactjava.shir.ActivityFixtures.session;
import static org.junit.jupiter.api.Assertions.*;

class StatisticsTests {
    @Test
    void mixedActivitiesHaveKnownTotalsIncludingPartialMinutes() {
        var sessions = List.of(
                session(1, ActivityType.READING, Duration.ofMinutes(40), 3),
                session(2, ActivityType.READING, Duration.ofMinutes(5).plusSeconds(59), 2),
                session(3, ActivityType.SPORT, Duration.ofMinutes(60), 4),
                session(4, ActivityType.WALKING, Duration.ofMinutes(10), 1));
        var expected = new ActivityStatistics(4, 116, 382);

        assertAllMethods(expected, sessions);
    }

    @Test
    void generatedDataMatchesAcrossAllMethodsAndParallelCollector() {
        var details = new ActivityDetailsGenerator(new Random(10)).generate(50);
        var policies = new RewardPolicyGenerator(new Random(20)).generate(50);
        var sessions = new ActivitySessionGenerator(new Random(30), details, policies).generate(5_000);
        ActivityStatistics expected = ActivityStatisticsCalculator.withLoop(sessions);

        assertAllMethods(expected, sessions);
        assertEquals(expected, sessions.parallelStream().collect(new ActivityStatisticsCollector()));
        assertEquals(ActivityStatistics.empty(), List.<ActivitySession>of().parallelStream()
                .collect(new ActivityStatisticsCollector()));
    }

    private void assertAllMethods(ActivityStatistics expected, List<ActivitySession> sessions) {
        assertEquals(expected, ActivityStatisticsCalculator.withLoop(sessions));
        assertEquals(expected, ActivityStatisticsCalculator.withStandardCollectors(sessions));
        assertEquals(expected, ActivityStatisticsCalculator.withCustomCollector(sessions));
    }
}
