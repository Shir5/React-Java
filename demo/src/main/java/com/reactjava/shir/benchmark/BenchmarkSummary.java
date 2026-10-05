package com.reactjava.shir.benchmark;

import com.reactjava.shir.statistics.ActivityStatistics;

import java.util.Objects;

public record BenchmarkSummary(String method, ActivityStatistics statistics, int measurementCount,
                               double medianElapsedNanos, long minElapsedNanos, long maxElapsedNanos,
                               double standardDeviationNanos, double medianCpuTimeNanos,
                               double medianAllocatedBytes) {
    public BenchmarkSummary {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        Objects.requireNonNull(statistics, "statistics");
        if (measurementCount <= 0 || medianElapsedNanos < 0 || minElapsedNanos < 0
                || maxElapsedNanos < minElapsedNanos || standardDeviationNanos < 0) {
            throw new IllegalArgumentException("Invalid benchmark summary");
        }
    }

    public double medianElapsedMillis() {
        return medianElapsedNanos / 1_000_000.0;
    }

    public double standardDeviationMillis() {
        return standardDeviationNanos / 1_000_000.0;
    }

    public double medianCpuTimeMillis() {
        return medianCpuTimeNanos < 0 ? -1 : medianCpuTimeNanos / 1_000_000.0;
    }
}
