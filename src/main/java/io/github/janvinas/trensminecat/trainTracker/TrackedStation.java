package io.github.janvinas.trensminecat.trainTracker;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class TrackedStation implements Comparable<TrackedStation> {
    String stationCode;
    Duration timeFromSpawn;

    public TrackedStation(String stationCode, Duration timeFromSpawn){
        this.stationCode = stationCode;
        this.timeFromSpawn = timeFromSpawn;
    }

    @Override
    public int compareTo(@NotNull TrackedStation o) {
        return timeFromSpawn.compareTo(o.timeFromSpawn);
    }
}
