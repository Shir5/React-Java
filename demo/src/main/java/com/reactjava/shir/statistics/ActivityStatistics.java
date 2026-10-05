package com.reactjava.shir.statistics;

public record ActivityStatistics(long sessionCount, long totalMinutes, long earnedCoins) {
    public ActivityStatistics {
        if (sessionCount < 0 || totalMinutes < 0 || earnedCoins < 0) {
            throw new IllegalArgumentException("Statistics must not be negative");
        }
    }

    public static ActivityStatistics empty() {
        return new ActivityStatistics(0, 0, 0);
    }
}
