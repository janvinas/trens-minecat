package io.github.janvinas.trensminecat;

import io.github.janvinas.trensminecat.trainTracker.TrackedTrain;
import io.github.janvinas.trensminecat.trainTracker.TrainTracker;
import net.intelie.omnicron.Cron;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

public class BoardUtils {
    public static TreeMap<LocalDateTime, Departure> fillDepartureBoard(LocalDateTime from, List<TrainLine> trainLines, Integer boardLength, String stationCode){

        TrainTracker tracker = TrensMinecat.getPlugin(TrensMinecat.class).trainTracker;
        TreeMap<LocalDateTime, Departure> departures = new TreeMap<>();
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

        List<TrackedTrain> trackedTrains = tracker.getTrainsWithNextStation(stationCode);
        trackedTrains.forEach(trackedTrain -> {
            Departure departure = new Departure();
            departure.name = trackedTrain.linedest.substring(0, trackedTrain.linedest.indexOf(" "));
            departure.destination = trackedTrain.destination;
            departure.information = "_";
            departure.delay = trackedTrain.delay;

            trackedTrain.nextStations.forEach(trackedStation -> {
                if(trackedStation.stationCode.equals(stationCode)){
                    LocalDateTime arrivalTime = trackedTrain.departureTime.plus(trackedStation.timeFromSpawn);
                    departure.platform = trackedStation.platform;
                    //if a departure exists with the same arrival time (without nanos) and line, remove nanos from departure (will override the scheduled departure)
                    if(departures.containsKey(arrivalTime.withNano(0))) {
                        if(departures.get(arrivalTime.withNano(0)).name.equals(departure.name)){
                            arrivalTime = arrivalTime.withNano(0);
                        }
                    }
                    departures.put(arrivalTime, departure);
                }
            });
        });
        return departures;
    }

    public static TreeMap<LocalDateTime, Departure> fillDepartureBoard(LocalDateTime from, List<TrainLine> trainLines, Integer boardLength){
        TrainTracker tracker = TrensMinecat.getPlugin(TrensMinecat.class).trainTracker;
        TreeMap<LocalDateTime, Departure> departures = new TreeMap<>();

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

    public static TreeMap<LocalDateTime, Departure> fillDepartureBoard(LocalDateTime from, List<TrainLine> trainLines, Integer boardLength, boolean cutAtLength){
        TreeMap<LocalDateTime, Departure> departures = fillDepartureBoard(from, trainLines, boardLength);
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