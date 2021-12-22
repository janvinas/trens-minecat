package io.github.janvinas.trensminecat;

import io.github.janvinas.trensminecat.trainTracker.TrackedStation;
import io.github.janvinas.trensminecat.trainTracker.TrackedTrain;
import io.github.janvinas.trensminecat.trainTracker.TrainTracker;
import net.intelie.omnicron.Cron;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class BoardUtils {
    public static TreeMap<LocalDateTime, Departure> fillDepartureBoard(LocalDateTime from, List<TrainLine> trainLines, Integer boardLength, String stationCode){
        TrainTracker tracker = TrensMinecat.getPlugin(TrensMinecat.class).trainTracker;
        TreeMap<LocalDateTime, Departure> departures = new TreeMap<>();
        //TODO tracked trains that are about to enter this station should also be added.
        List<TrackedTrain> trackedTrains = tracker.getTrainsWithNextStation(stationCode);
        trackedTrains.forEach(trackedTrain -> {
            Departure departure = new Departure();
            departure.name = trackedTrain.trainName;
            departure.destination = trackedTrain.destination;
            departure.information = trackedTrain.trainName; //also add train name as destination TODO for debugging purposes
            departure.delay = trackedTrain.delay;

            trackedTrain.nextStations.forEach(trackedStation -> {
                if(trackedStation.stationCode.equals(stationCode)){
                    departures.put(trackedTrain.departureTime.plus(trackedStation.timeFromSpawn), departure);
                }
            });
        });

        for(TrainLine trainLine : trainLines){
            Departure departure = new Departure();

            departure.name = trainLine.name;
            departure.destination = trainLine.destination;
            departure.platform = trainLine.platform;
            departure.information = trainLine.information;

            Cron cron = new Cron(trainLine.cron);

            LocalDateTime input = from;
            //put enough trains of every train line so the board will never be empty
            for (int i = 0; i < boardLength; i++) {
                input = cron.next(input);
                departures.put(input, departure);
            }
        }
        return departures;
    }
    public static TreeMap<LocalDateTime, Departure> fillDepartureBoard(LocalDateTime from, List<TrainLine> trainLines, Integer boardLength, String stationCode, boolean cutAtLength){
        TreeMap<LocalDateTime, Departure> departures = fillDepartureBoard(from, trainLines, boardLength, stationCode);
        if(cutAtLength){
            while(departures.size() > boardLength){
                departures.remove(departures.lastKey());
            }
        }
        return departures;
    }

    public static String getTrainLine(String trainName){
        if(trainName.matches(".*[_].*")) return trainName.substring(0, trainName.indexOf('_'));
        return "unknownLine";
    }
}