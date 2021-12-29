package io.github.janvinas.trensminecat.trainTracker;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class TrackedStation implements Comparable<TrackedStation> {
    public String stationCode;
    public Duration timeFromSpawn;
    public String platform;

    public TrackedStation(String stationCode, Duration timeFromSpawn, String platform){
        this.stationCode = stationCode;
        this.timeFromSpawn = timeFromSpawn;
        this.platform = platform;

    }

    @Override
    public int compareTo(@NotNull TrackedStation o) {
        return timeFromSpawn.compareTo(o.timeFromSpawn);
    }
}
