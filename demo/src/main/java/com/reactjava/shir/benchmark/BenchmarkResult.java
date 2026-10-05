package com.reactjava.shir.benchmark;

import com.reactjava.shir.statistics.ActivityStatistics;

import java.util.Objects;

public record BenchmarkResult(String method, long startedAtNanos, long finishedAtNanos,
                              ActivityStatistics statistics, long cpuTimeNanos, long allocatedBytes) {
    public BenchmarkResult(String method, long startedAtNanos, long finishedAtNanos,
                           ActivityStatistics statistics) {
        this(method, startedAtNanos, finishedAtNanos, statistics, -1, -1);
    }

    public BenchmarkResult(String method, long startedAtNanos, long finishedAtNanos,
                           ActivityStatistics statistics, long allocatedBytes) {
        this(method, startedAtNanos, finishedAtNanos, statistics, -1, allocatedBytes);
    }

    public BenchmarkResult {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (finishedAtNanos - startedAtNanos < 0) {
            throw new IllegalArgumentException("finish time must not precede start time");
        }
        Objects.requireNonNull(statistics, "statistics");
        if (cpuTimeNanos < -1 || allocatedBytes < -1) {
            throw new IllegalArgumentException("CPU time and allocated bytes must be non-negative or -1 when unavailable");
        }
    }

    public long elapsedNanos() {
        return finishedAtNanos - startedAtNanos;
    }

    public double elapsedMillis() {
        return elapsedNanos() / 1_000_000.0;
    }
}
