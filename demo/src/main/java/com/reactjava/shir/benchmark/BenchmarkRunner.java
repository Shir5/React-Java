package com.reactjava.shir.benchmark;

import com.sun.management.ThreadMXBean;
import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.statistics.ActivityStatistics;
import com.reactjava.shir.statistics.ActivityStatisticsCalculator;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class BenchmarkRunner {
    private static final int WARMUP_ROUNDS = 3;
    private final ThreadMXBean allocationBean = allocationBean();

    public List<BenchmarkResult> run(List<ActivitySession> sessions) {
        Objects.requireNonNull(sessions, "sessions");
        warmUp(sessions);
        List<BenchmarkResult> results = List.of(
                measure("for loop", sessions,
                        items -> ActivityStatisticsCalculator.withLoop(items)),
                measure("Stream API, standard collectors", sessions,
                        items -> ActivityStatisticsCalculator.withStandardCollectors(items)),
                measure("Stream API, custom collector", sessions,
                        items -> ActivityStatisticsCalculator.withCustomCollector(items)),
                measure("Stream API, teeing collector", sessions,
                        items -> ActivityStatisticsCalculator.withTeeingCollector(items)));
        ActivityStatistics expected = results.get(0).statistics();
        if (results.stream().anyMatch(result -> !result.statistics().equals(expected))) {
            throw new IllegalStateException("Calculation methods produced different statistics");
        }
        return results;
    }

    private void warmUp(List<ActivitySession> sessions) {
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            ActivityStatisticsCalculator.withLoop(sessions);
            ActivityStatisticsCalculator.withStandardCollectors(sessions);
            ActivityStatisticsCalculator.withCustomCollector(sessions);
            ActivityStatisticsCalculator.withTeeingCollector(sessions);
        }
    }

    private BenchmarkResult measure(String method, List<ActivitySession> sessions,
                                    Function<List<ActivitySession>, ActivityStatistics> calculate) {
        long allocatedBefore = allocatedBytes();
        long startedAt = System.nanoTime();
        ActivityStatistics statistics = calculate.apply(sessions);
        long finishedAt = System.nanoTime();
        long allocatedAfter = allocatedBytes();
        long allocatedBytes = allocatedBefore < 0 || allocatedAfter < 0
                ? -1
                : allocatedAfter - allocatedBefore;
        return new BenchmarkResult(method, startedAt, finishedAt, statistics, allocatedBytes);
    }

    private long allocatedBytes() {
        return allocationBean == null
                ? -1
                : allocationBean.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    private static ThreadMXBean allocationBean() {
        if (!(ManagementFactory.getThreadMXBean() instanceof ThreadMXBean bean)
                || !bean.isThreadAllocatedMemorySupported()) {
            return null;
        }
        try {
            if (!bean.isThreadAllocatedMemoryEnabled()) {
                bean.setThreadAllocatedMemoryEnabled(true);
            }
            return bean.isThreadAllocatedMemoryEnabled() ? bean : null;
        } catch (SecurityException | UnsupportedOperationException exception) {
            return null;
        }
    }
}
