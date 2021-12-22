package io.github.janvinas.trensminecat.trainTracker;
import io.github.janvinas.trensminecat.TrensMinecat;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeSet;
import java.util.logging.Level;

public class TrackedTrain implements Serializable {
    private static final long serialVersionUID = 5550978909888616753L;
    public String destination;
    public String trainName;
    public String linedest; //format "R2 Sant Celoni"
    public TreeSet<TrackedStation> nextStations = new TreeSet<>();
    public LocalDateTime departureTime;
    public Duration delay;

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

    public boolean enterStation(String stationCode){
        //check if station exists in station list. To prevent this method from deleting all stations, it will do nothing
        //if the station queried is not in the nex stations list.
        for(TrackedStation s : nextStations){
            if(s.stationCode.equals(stationCode)){
                LocalDateTime now = LocalDateTime.now();
                delay = Duration.between(departureTime.plus(s.timeFromSpawn), now);
                nextStations = (TreeSet<TrackedStation>) nextStations.tailSet(s, false);
                return true;
            }
        }
        return false;
    }

    public boolean hasStation(String station){
        for (TrackedStation trackedStation : nextStations) {
            if(trackedStation.stationCode.equals(station)) return true;
        }
        return false;
    }
}
