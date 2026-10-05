package com.reactjava.shir;

import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.benchmark.BenchmarkRunner;
import com.reactjava.shir.benchmark.BenchmarkSummary;
import com.reactjava.shir.generator.ActivityDetailsGenerator;
import com.reactjava.shir.generator.ActivitySessionGenerator;
import com.reactjava.shir.generator.RewardPolicyGenerator;
import com.reactjava.shir.statistics.ActivityStatistics;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class ProductivityLabApplication {
    private static final int[] COLLECTION_SIZES = {5_000, 50_000, 250_000};

    private ProductivityLabApplication() {
    }

    public static void main(String[] args) {
        var details = new ActivityDetailsGenerator(new Random(10)).generate(50);
        var policies = new RewardPolicyGenerator(new Random(20)).generate(50);
        var generator = new ActivitySessionGenerator(new Random(30), details, policies);
        var runner = new BenchmarkRunner();

        System.out.println("Productivity activity statistics benchmark");
        for (int size : COLLECTION_SIZES) {
            List<ActivitySession> sessions = generator.generate(size);
            printResults(size, runner.run(sessions));
        }
    }

    private static void printResults(int size, List<BenchmarkSummary> results) {
        ActivityStatistics statistics = results.get(0).statistics();
        System.out.printf(Locale.ROOT, "%nCollection size: %,d%n", size);
        System.out.printf(Locale.ROOT, "Sessions: %,d; minutes: %,d; earned coins: %,d%n",
                statistics.sessionCount(), statistics.totalMinutes(), statistics.earnedCoins());
        for (BenchmarkSummary result : results) {
            String cpuTime = result.medianCpuTimeMillis() < 0
                    ? "n/a"
                    : String.format(Locale.ROOT, "%.3f", result.medianCpuTimeMillis());
            String allocated = result.medianAllocatedBytes() < 0
                    ? "n/a"
                    : String.format(Locale.ROOT, "%.0f", result.medianAllocatedBytes());
            System.out.printf(Locale.ROOT,
                    "%-34s runs=%d, median=%.3f ms, min=%.3f ms, max=%.3f ms, sd=%.3f ms, CPU=%s ms, allocated=%s bytes%n",
                    result.method(), result.measurementCount(), result.medianElapsedMillis(),
                    result.minElapsedNanos() / 1_000_000.0, result.maxElapsedNanos() / 1_000_000.0,
                    result.standardDeviationMillis(), cpuTime, allocated);
        }
    }
}
