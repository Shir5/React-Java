package com.reactjava.shir.statistics;

import com.reactjava.shir.activity.ActivitySession;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ActivityStatisticsCalculator {
    private ActivityStatisticsCalculator() {
    }

    public static ActivityStatistics withLoop(List<ActivitySession> sessions) {
        Objects.requireNonNull(sessions, "sessions");

        long sessionCount = 0;
        long totalMinutes = 0;
        long earnedCoins = 0;

        for (ActivitySession session : sessions) {
            sessionCount++;
            totalMinutes += session.rewardedMinutes();
            earnedCoins += session.earnedCoins();
        }

        return new ActivityStatistics(sessionCount, totalMinutes, earnedCoins);
    }

    public static ActivityStatistics withStandardCollectors(List<ActivitySession> sessions) {
        Objects.requireNonNull(sessions, "sessions");

        long sessionCount = sessions.stream().collect(Collectors.counting());

        long totalMinutes = sessions.stream()
                .collect(Collectors.summingLong(session -> session.rewardedMinutes()));

        long earnedCoins = sessions.stream()
                .collect(Collectors.summingLong(session -> session.earnedCoins()));

        return new ActivityStatistics(sessionCount, totalMinutes, earnedCoins);
    }

    public static ActivityStatistics withCustomCollector(List<ActivitySession> sessions) {
        Objects.requireNonNull(sessions, "sessions");

        return sessions.stream().collect(new ActivityStatisticsCollector());
    }

    public static ActivityStatistics withTeeingCollector(List<ActivitySession> sessions) {
        Objects.requireNonNull(sessions, "sessions");

        return sessions.stream().collect(Collectors.teeing(
                Collectors.counting(),
                Collectors.teeing(
                        Collectors.summingLong(ActivitySession::rewardedMinutes),
                        Collectors.summingLong(ActivitySession::earnedCoins),
                        MinuteAndCoinTotals::new),
                (sessionCount, totals) -> new ActivityStatistics(
                        sessionCount, totals.totalMinutes(), totals.earnedCoins())));
    }

    private record MinuteAndCoinTotals(long totalMinutes, long earnedCoins) {
    }
}
