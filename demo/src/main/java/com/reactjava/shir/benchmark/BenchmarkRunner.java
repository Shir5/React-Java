package com.reactjava.shir.benchmark;

import com.sun.management.ThreadMXBean;
import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.statistics.ActivityStatistics;
import com.reactjava.shir.statistics.ActivityStatisticsCalculator;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public final class BenchmarkRunner {
    private static final int WARMUP_ROUNDS = 5;
    private static final int MEASUREMENT_ROUNDS = 30;

    private final ThreadMXBean threadBean = threadBean();
    private final boolean cpuTimeEnabled = enableCpuTime(threadBean);
    private final boolean allocatedMemoryEnabled = enableAllocatedMemory(threadBean);

    public List<BenchmarkSummary> run(List<ActivitySession> sessions) {
        Objects.requireNonNull(sessions, "sessions");
        List<BenchmarkMethod> methods = methods();
        Random orderRandom = new Random(42);

        warmUp(sessions, methods, orderRandom);

        Map<String, List<BenchmarkResult>> measurements = new LinkedHashMap<>();
        for (BenchmarkMethod method : methods) {
            measurements.put(method.name(), new ArrayList<>(MEASUREMENT_ROUNDS));
        }

        for (int round = 0; round < MEASUREMENT_ROUNDS; round++) {
            List<BenchmarkMethod> order = shuffled(methods, orderRandom);
            for (BenchmarkMethod method : order) {
                measurements.get(method.name()).add(measure(method, sessions));
            }
        }

        ActivityStatistics expected = measurements.values().iterator().next().get(0).statistics();
        if (measurements.values().stream().flatMap(List::stream)
                .anyMatch(result -> !result.statistics().equals(expected))) {
            throw new IllegalStateException("Calculation methods produced different statistics");
        }

        return methods.stream()
                .map(method -> summarize(method.name(), measurements.get(method.name())))
                .toList();
    }

    private void warmUp(List<ActivitySession> sessions, List<BenchmarkMethod> methods, Random random) {
        for (int round = 0; round < WARMUP_ROUNDS; round++) {
            for (BenchmarkMethod method : shuffled(methods, random)) {
                method.calculate().apply(sessions);
            }
        }
    }

    private BenchmarkResult measure(BenchmarkMethod method, List<ActivitySession> sessions) {
        long allocatedBefore = allocatedBytes();
        long cpuBefore = cpuTimeNanos();
        long startedAt = System.nanoTime();
        ActivityStatistics statistics = method.calculate().apply(sessions);
        long finishedAt = System.nanoTime();
        long cpuAfter = cpuTimeNanos();
        long allocatedAfter = allocatedBytes();

        return new BenchmarkResult(method.name(), startedAt, finishedAt, statistics,
                difference(cpuBefore, cpuAfter), difference(allocatedBefore, allocatedAfter));
    }

    private BenchmarkSummary summarize(String method, List<BenchmarkResult> results) {
        List<Long> elapsed = results.stream().map(BenchmarkResult::elapsedNanos).sorted().toList();
        double mean = elapsed.stream().mapToDouble(Long::doubleValue).average().orElseThrow();
        double variance = elapsed.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0);
        double standardDeviation = Math.sqrt(variance);
        double medianElapsed = median(elapsed);
        double medianCpu = medianAvailable(results.stream().map(BenchmarkResult::cpuTimeNanos).toList());
        double medianAllocated = medianAvailable(results.stream().map(BenchmarkResult::allocatedBytes).toList());

        return new BenchmarkSummary(method, results.get(0).statistics(), results.size(), medianElapsed,
                elapsed.get(0), elapsed.get(elapsed.size() - 1), standardDeviation,
                medianCpu, medianAllocated);
    }

    private long allocatedBytes() {
        return allocatedMemoryEnabled
                ? threadBean.getThreadAllocatedBytes(Thread.currentThread().getId())
                : -1;
    }

    private long cpuTimeNanos() {
        return cpuTimeEnabled ? threadBean.getCurrentThreadCpuTime() : -1;
    }

    private static long difference(long before, long after) {
        return before < 0 || after < 0 ? -1 : after - before;
    }

    private static double median(List<Long> sortedValues) {
        int middle = sortedValues.size() / 2;
        if (sortedValues.size() % 2 == 1) {
            return sortedValues.get(middle);
        }
        return sortedValues.get(middle - 1) / 2.0 + sortedValues.get(middle) / 2.0;
    }

    private static double medianAvailable(List<Long> values) {
        List<Long> available = values.stream().filter(value -> value >= 0).sorted().toList();
        return available.isEmpty() ? -1 : median(available);
    }

    private static List<BenchmarkMethod> shuffled(List<BenchmarkMethod> methods, Random random) {
        List<BenchmarkMethod> order = new ArrayList<>(methods);
        Collections.shuffle(order, random);
        return order;
    }

    private static List<BenchmarkMethod> methods() {
        return List.of(
                new BenchmarkMethod("for loop", ActivityStatisticsCalculator::withLoop),
                new BenchmarkMethod("Stream API, standard collectors",
                        ActivityStatisticsCalculator::withStandardCollectors),
                new BenchmarkMethod("Stream API, custom collector",
                        ActivityStatisticsCalculator::withCustomCollector),
                new BenchmarkMethod("Stream API, teeing collector",
                        ActivityStatisticsCalculator::withTeeingCollector));
    }

    private static ThreadMXBean threadBean() {
        return ManagementFactory.getThreadMXBean() instanceof ThreadMXBean bean ? bean : null;
    }

    private static boolean enableCpuTime(ThreadMXBean bean) {
        if (bean == null || !bean.isCurrentThreadCpuTimeSupported()) {
            return false;
        }
        try {
            if (!bean.isThreadCpuTimeEnabled()) {
                bean.setThreadCpuTimeEnabled(true);
            }
            return bean.isThreadCpuTimeEnabled();
        } catch (SecurityException | UnsupportedOperationException exception) {
            return false;
        }
    }

    private static boolean enableAllocatedMemory(ThreadMXBean bean) {
        if (bean == null || !bean.isThreadAllocatedMemorySupported()) {
            return false;
        }
        try {
            if (!bean.isThreadAllocatedMemoryEnabled()) {
                bean.setThreadAllocatedMemoryEnabled(true);
            }
            return bean.isThreadAllocatedMemoryEnabled();
        } catch (SecurityException | UnsupportedOperationException exception) {
            return false;
        }
    }

    private record BenchmarkMethod(String name,
                                   Function<List<ActivitySession>, ActivityStatistics> calculate) {
    }
}
