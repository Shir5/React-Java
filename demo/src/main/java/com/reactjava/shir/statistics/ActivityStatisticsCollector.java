package com.reactjava.shir.statistics;

import com.reactjava.shir.activity.ActivitySession;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class ActivityStatisticsCollector
        implements Collector<ActivitySession, ActivityStatisticsCollector.Accumulator, ActivityStatistics> {

    public static final class Accumulator {
        private long sessionCount;
        private long totalMinutes;
        private long earnedCoins;

        private void add(ActivitySession session) {
            sessionCount++;
            totalMinutes += session.rewardedMinutes();
            earnedCoins += session.earnedCoins();
        }

        private Accumulator combine(Accumulator other) {
            sessionCount += other.sessionCount;
            totalMinutes += other.totalMinutes;
            earnedCoins += other.earnedCoins;
            return this;
        }

        private ActivityStatistics finish() {
            return new ActivityStatistics(sessionCount, totalMinutes, earnedCoins);
        }
    }

    @Override
    public Supplier<Accumulator> supplier() {
        return () -> new Accumulator();
    }

    @Override
    public BiConsumer<Accumulator, ActivitySession> accumulator() {
        return (accumulator, session) -> accumulator.add(session);
    }

    @Override
    public BinaryOperator<Accumulator> combiner() {
        return (firstAccumulator, secondAccumulator) -> firstAccumulator.combine(secondAccumulator);
    }

    @Override
    public Function<Accumulator, ActivityStatistics> finisher() {
        return accumulator -> accumulator.finish();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
