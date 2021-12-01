package io.github.janvinas.trensminecat;

import net.intelie.omnicron.Cron;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

public class BoardUtils {
    public static TreeMap<LocalDateTime, Departure> fillDepartureBoard(LocalDateTime from, List<TrainLine> trainLines, Integer boardLength){
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