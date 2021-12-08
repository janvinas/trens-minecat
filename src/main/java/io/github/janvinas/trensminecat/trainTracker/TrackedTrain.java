package io.github.janvinas.trensminecat.trainTracker;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import io.github.janvinas.trensminecat.TrensMinecat;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeSet;
import java.util.logging.Level;

public class TrackedTrain implements Serializable {
    private static final long serialVersionUID = 5550978909888616753L;
    String destination;
    String trainName;
    String linedest; //format "R2 Sant Celoni"
    TreeSet<TrackedStation> nextStations = new TreeSet<>();
    LocalDateTime departureTime;
    Duration delay;

    public TrackedTrain(TrainTracker trainTracker, String trainName, String destination){
        this.trainName = trainName;
        this.destination = destination;
        String line = trainName.substring(0, trainName.indexOf("_"));
        linedest = line + " " + destination;
        departureTime = LocalDateTime.now();
        this.addAllStations(trainTracker, linedest);
        delay = Duration.ZERO;
    }

    public void addAllStations(TrainTracker trainTracker, String linedest){
        TrackedLine trackedLine = trainTracker.getLineMatching(linedest);
        if(trackedLine == null){
            TrensMinecat.getPlugin(TrensMinecat.class).getLogger().log(Level.WARNING, "Train of line \"" + linedest + "\" was registered but no station list could be found in stationList.yml");
        }else{
            nextStations = trackedLine.trackedStations;
        }

    }
}
