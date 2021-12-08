package io.github.janvinas.trensminecat.trainTracker;

import java.time.Duration;
import java.util.TreeSet;


public class TrackedLine {
    public String lineName;
    public Duration spawnFrequency;
    public TreeSet<TrackedStation> trackedStations = new TreeSet<>();
}
