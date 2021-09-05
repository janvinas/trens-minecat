package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.MapTexture;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ManualDisplays {

    public static class ManualDisplay1 extends MapDisplay {
        static String imgDir = "img/";
        boolean updateTime = true;

        public boolean updateInformation(String displayID, String displayName, String destination){
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            String trainLine;
            String dest;
            if(destination.equals("nopara")){
                trainLine = "info";
                dest = "sense parada";
            }else{
                trainLine = destination.substring(0, destination.indexOf(' '));
                dest = destination;
                if(destination.contains(" → "))
                    dest = destination.substring(destination.indexOf('→') + 2);
                dest = dest.toUpperCase();
            }

            getLayer(5).clear();
            getLayer(4).clear();
            MapTexture lineIcon = loadTexture(imgDir + "28px/" + trainLine + ".png");
            if(!(lineIcon.getHeight() > 1)){
                dest = displayName;
                lineIcon = loadTexture(imgDir + "28px/what.png");
            }
            getLayer(4).draw(lineIcon, 5, 14);
            getLayer(4).setAlignment(MapFont.Alignment.MIDDLE);
            getLayer(4).draw(MapFont.MINECRAFT, 74, 23,
                    MapColorPalette.getColor(255, 255, 255),
                    dest);

            updateTime = false;
            return true;
        }

        public boolean clearInformation(String displayID){
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(4).clear();
            getLayer(4).draw(loadTexture(imgDir + "28px/rodalies.png"), 5, 14);

            getLayer(5).clear();
            getLayer(5).setAlignment(MapFont.Alignment.RIGHT);
            LocalDateTime now = LocalDateTime.now();
            getLayer(5).draw(MapFont.MINECRAFT, 119, 24, MapColorPalette.COLOR_WHITE,
                    now.format(DateTimeFormatter.ofPattern("HH:mm")));
            updateTime = true;
            return true;
        }

        @Override
        public void onAttached() {
            super.onAttached();
            getLayer(1).clear();
            getLayer(1).fillRectangle(5, 14, 118, 28, MapColorPalette.getColor(40, 40, 40));
            getLayer(3).clear();
            getLayer(3).draw(loadTexture(imgDir + "DepartureBoard3.png"), 0, 0);
            getLayer(4).clear();
            getLayer(4).draw(loadTexture(imgDir + "28px/rodalies.png"), 5, 14);

        }

        @Override
        public void onTick() {
            super.onTick();
            if(updateTime){
                getLayer(5).clear();
                getLayer(5).setAlignment(MapFont.Alignment.RIGHT);
                LocalDateTime now = LocalDateTime.now();
                getLayer(5).draw(MapFont.MINECRAFT, 119, 24, MapColorPalette.COLOR_WHITE,
                        now.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
    }
    /*
    public static class ManualDisplay2 extends MapDisplay{
        TreeMap<String, LocalDateTime> lastTrains = new TreeMap<>();
        HashMap<String, Duration> trainIntervals = properties.get("trainIntervals", HashMap.class);
        int tickCount = 0;

        public boolean updateInformation(String displayID, String displayName, String destination){
            if(! properties.get("ID", String.class).equals(displayID)) return false;
            LocalDateTime now = LocalDateTime.now();
            lastTrains.put(destination, now);
            return true;
        }

        public boolean clearInformation(String displayID){
            return true;
        }

        @Override
        public void onAttached() {
            getLayer(0).draw(loadTexture(imgDir + "DepartureBoard4.png"), 0, 0);
            super.onAttached();
        }

        @Override
        public void onTick() {

            LocalDateTime now = LocalDateTime.now();
            getLayer(2).clear();
            getLayer(2).setAlignment(MapFont.Alignment.MIDDLE);
            getLayer(2).draw(MapFont.MINECRAFT, 227, 7, MapColorPalette.COLOR_BLACK,
                    now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            if( (tickCount % updateTime) == 0){
                int secondsToDisplayOnBoard = TrensMinecat.secondsToDisplayOnBoard;
                TreeMap<LocalDateTime, String> departureBoardTrains = new TreeMap<>();

                for(String train : lastTrains.keySet()){


                    departureBoardTrains.put(lastTrains.get(train).plus(interval), train);  //add first expected train
                    departureBoardTrains.put(lastTrains.get(train).plus(interval.multipliedBy(2)), train); //add the next expected train twice the duration of the first
                }

                //print train lines on screen
                getLayer(1).clear();
                getLayer(1).setAlignment(MapFont.Alignment.LEFT);
                int i = 0;
                for(LocalDateTime departureTime : departureBoardTrains.keySet()){


                    Duration untilDeparture = Duration.between(now, departureTime);
                    if(untilDeparture.minusSeconds(secondsToDisplayOnBoard).isNegative()) {
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i * 14,
                                MapColorPalette.getColor(255, 0, 0), "imminent");
                    }else if(untilDeparture.minusMinutes(5).isNegative()){
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i * 14,
                                MapColorPalette.getColor(0, 128, 0),
                                untilDeparture.getSeconds()/60 + "min");
                    }else{
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i*14,
                                MapColorPalette.getColor(0, 0, 0),
                                departureTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                    //getLayer(1).draw(loadTexture(imgDir + "11px/" +
                            //departureBoardTrains.get(departureTime).name + ".png"), 1, 33 + i*14);

                    String destination = departureBoardTrains.get(departureTime).destination;
                    if(!destination.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 21, 34 + i*14,
                            MapColorPalette.getColor(0, 0, 0),
                            departureBoardTrains.get(departureTime).destination);
                    String platform = departureBoardTrains.get(departureTime).platform;
                    if(!platform.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 99, 34 + i*14,
                            MapColorPalette.getColor(0, 0, 0),
                            departureBoardTrains.get(departureTime).platform);
                    String information = departureBoardTrains.get(departureTime).information;
                    if(!information.equals("_")){
                        getLayer(1).draw(MapFont.MINECRAFT, 162, 34 + i*14,
                                MapColorPalette.getColor(0, 0, 0),
                                departureBoardTrains.get(departureTime).information);
                    }

                    i++;
                }
            }

            tickCount++;
            super.onTick();
        }
    }
    */

    public static class ManualDisplay3 extends MapDisplay{
        static String imgDir = MapDisplays.imgDir;
        static int updateTime = MapDisplays.updateTime;

        static Font helvetica;

        static {
            try {
                InputStream helveticaStream = TrensMinecat.class.getResourceAsStream("/fonts/Helvetica.ttf");
                helvetica = Font.createFont(Font.TRUETYPE_FONT, helveticaStream);
            } catch (FontFormatException | IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAttached() {
            super.onAttached();
            getLayer(0).clear();
            getLayer(0).draw(loadTexture(imgDir + "ManualDisplay3.png"), 0, 0);
        }

        @Override
        public void onTick() {
            super.onTick();

            getLayer(1).clear();
            LocalDateTime now = LocalDateTime.now();
            getLayer(1).draw(MapFont.fromJavaFont(helvetica.deriveFont(12F)), 24, 11, MapColorPalette.COLOR_WHITE,
                    now.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }
}
