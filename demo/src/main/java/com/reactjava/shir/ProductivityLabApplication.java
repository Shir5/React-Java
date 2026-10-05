package com.reactjava.shir;

import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.benchmark.BenchmarkResult;
import com.reactjava.shir.benchmark.BenchmarkRunner;
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

    private static void printResults(int size, List<BenchmarkResult> results) {
        ActivityStatistics statistics = results.get(0).statistics();
        System.out.printf(Locale.ROOT, "%nCollection size: %,d%n", size);
        System.out.printf(Locale.ROOT, "Sessions: %,d; minutes: %,d; earned coins: %,d%n",
                statistics.sessionCount(), statistics.totalMinutes(), statistics.earnedCoins());
        for (BenchmarkResult result : results) {
            String allocated = result.allocatedBytes() < 0
                    ? "n/a"
                    : String.format(Locale.ROOT, "%,d", result.allocatedBytes());
            System.out.printf(Locale.ROOT,
                    "%-34s start=%d ns, finish=%d ns, elapsed=%.3f ms, allocated=%s bytes%n",
                    result.method(), result.startedAtNanos(), result.finishedAtNanos(),
                    result.elapsedMillis(), allocated);
        }
    }
}
