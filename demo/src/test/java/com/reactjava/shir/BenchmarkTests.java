package com.reactjava.shir;

import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.benchmark.BenchmarkResult;
import com.reactjava.shir.benchmark.BenchmarkRunner;
import com.reactjava.shir.benchmark.BenchmarkSummary;
import com.reactjava.shir.statistics.ActivityStatistics;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static com.reactjava.shir.ActivityFixtures.session;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkTests {
    @Test
    void runnerReturnsFourMatchingResultsWithMeasuredIntervals() {
        var results = new BenchmarkRunner().run(List.of(
                session(1, ActivityType.READING, Duration.ofMinutes(10), 2)));

        assertEquals(List.of("for loop", "Stream API, standard collectors", "Stream API, custom collector",
                        "Stream API, teeing collector"),
                results.stream().map(result -> result.method()).toList());
        for (BenchmarkSummary result : results) {
            assertEquals(results.get(0).statistics(), result.statistics());
            assertEquals(1, result.statistics().sessionCount());
            assertEquals(10, result.statistics().totalMinutes());
            assertEquals(20, result.statistics().earnedCoins());
            assertEquals(30, result.measurementCount());
            assertTrue(result.medianElapsedNanos() >= 0);
            assertTrue(result.medianCpuTimeNanos() >= -1);
            assertTrue(result.medianAllocatedBytes() >= -1);
        }
        assertThrows(UnsupportedOperationException.class, () -> results.clear());
    }

    @Test
    void runnerSupportsEmptyData() {
        assertTrue(new BenchmarkRunner().run(List.of()).stream()
                .allMatch(result -> result.statistics().equals(ActivityStatistics.empty())));
    }

    @Test
    void benchmarkResultConvertsNanosAndHandlesSignedCounterWrap() {
        var result = new BenchmarkResult("test", -2_000_000, 500_000, ActivityStatistics.empty());
        assertEquals(2_500_000, result.elapsedNanos());
        assertEquals(2.5, result.elapsedMillis());
        var wrapped = new BenchmarkResult("test", Long.MAX_VALUE - 5, Long.MIN_VALUE + 4,
                ActivityStatistics.empty());
        assertEquals(10, wrapped.elapsedNanos());
        assertThrows(IllegalArgumentException.class,
                () -> new BenchmarkResult("test", 10, 9, ActivityStatistics.empty()));
        assertThrows(NullPointerException.class, () -> new BenchmarkResult("test", 0, 1, null));
    }
}
