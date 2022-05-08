package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.MapTexture;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class MapDisplays{
    static String imgDir = "img/";
    static int updateTime = 100; //time in ticks between sign updates
    //TODO afegir cartell rodalies antic (leds vermells)
    public static class DepartureBoard1 extends MapDisplay{
        int tickCount = 0;

        @Override
        public void onAttached() {
            getLayer().fillRectangle(0, 0, 256, 10, MapColorPalette.getColor(72, 129, 183));
            getLayer().fillRectangle(0, 10, 256, 1, MapColorPalette.getColor(0, 0, 0));
            getLayer().draw(MapFont.MINECRAFT, 1, 1, MapColorPalette.getColor(255, 255, 255), "Departures:");
            getLayer().setAlignment(MapFont.Alignment.RIGHT);
            getLayer().draw(MapFont.MINECRAFT, 254, 1, MapColorPalette.getColor(255, 255, 255),
                    properties.get("name", String.class).replace('_', ' '));
            //draw background strips
            getLayer().fillRectangle(0, 11, 256, 10, MapColorPalette.getColor(255, 255, 255));
            getLayer().fillRectangle(0, 21, 256, 10, MapColorPalette.getColor(200, 200, 200));
            getLayer().fillRectangle(0, 31, 256, 10, MapColorPalette.getColor(255, 255, 255));
            getLayer().fillRectangle(0, 41, 256, 10, MapColorPalette.getColor(200, 200, 200));
            getLayer().fillRectangle(0, 51, 256, 10, MapColorPalette.getColor(255, 255, 255));
            getLayer().fillRectangle(0, 61, 256, 10, MapColorPalette.getColor(200, 200, 200));
            getLayer().fillRectangle(0, 71, 256, 10, MapColorPalette.getColor(255, 255, 255));
            getLayer().fillRectangle(0, 81, 256, 10, MapColorPalette.getColor(200, 200, 200));
            getLayer().fillRectangle(0, 91, 256, 10, MapColorPalette.getColor(255, 255, 255));
            getLayer().fillRectangle(0, 101, 256, 10, MapColorPalette.getColor(200, 200, 200));

            //draw column titles:
            getLayer().setAlignment(MapFont.Alignment.LEFT);
            getLayer().draw(MapFont.MINECRAFT, 1, 12, MapColorPalette.getColor(0, 0, 255), "Time");
            getLayer().draw(MapFont.MINECRAFT, 80, 12, MapColorPalette.getColor(0, 0, 255), "Line");
            getLayer().draw(MapFont.MINECRAFT, 107, 12, MapColorPalette.getColor(0, 0, 255), "Destination");
            getLayer().draw(MapFont.MINECRAFT, 170, 12, MapColorPalette.getColor(0, 0, 255), "Pl.");
            getLayer().draw(MapFont.MINECRAFT, 190, 12, MapColorPalette.getColor(0, 0, 255), "Information");
            //draw last line, where time and date will be shown
            getLayer().fillRectangle(0, 111, 256, 18, MapColorPalette.getColor(0, 0, 0));
            getLayer().fillRectangle(0, 112, 256, 10, MapColorPalette.getColor(72, 129, 183));

            super.onAttached();
        }

        @Override
        public void onTick() {
            LocalDateTime now = LocalDateTime.now();

            getLayer(3).clear();
            getLayer(3).setAlignment(MapFont.Alignment.LEFT);
            getLayer(3).draw(MapFont.MINECRAFT, 1, 113, MapColorPalette.getColor(0, 0, 0), now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " UTC");
            getLayer(3).setAlignment(MapFont.Alignment.RIGHT);
            getLayer(3).draw(MapFont.MINECRAFT, 254, 113, MapColorPalette.getColor(0, 0, 0), now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            if( (tickCount % updateTime) == 0){

                int secondsToDisplayOnBoard = TrensMinecat.secondsToDisplayOnBoard;
                DepartureBoardTemplate template = TrensMinecat.departureBoards.get(properties.get("template", String.class));
                TreeMap<LocalDateTime, Departure> departureBoardTrains = BoardUtils.fillDepartureBoard(now, template.trainLines, template.length, properties.get("template", String.class), true);

                //print train lines on screen
                getLayer(1).clear();
                getLayer(1).setAlignment(MapFont.Alignment.LEFT);
                int i = 0;
                for(LocalDateTime departureTime : departureBoardTrains.keySet()){
                    Duration untilDeparture = Duration.between(now, departureTime);
                    if(untilDeparture.minusSeconds(secondsToDisplayOnBoard).isNegative()){
                        getLayer(1).draw(MapFont.MINECRAFT, 1, 22 + i*10,
                                MapColorPalette.getColor(255, 0, 0),
                                "now");
                    }else if(untilDeparture.minusMinutes(5).isNegative()){
                        getLayer(1).draw(MapFont.MINECRAFT, 1, 22 + i*10,
                                MapColorPalette.getColor(0, 0, 0),
                                departureTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " (" +
                                        (int) untilDeparture.getSeconds() / 60 + "min)");
                    }else{
                        getLayer(1).draw(MapFont.MINECRAFT, 1, 22 + i*10,
                                MapColorPalette.getColor(0, 0, 0),
                                departureTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }

                    getLayer(1).draw(MapFont.MINECRAFT, 80, 22 + i*10,
                            MapColorPalette.getColor(0, 0, 0),
                            departureBoardTrains.get(departureTime).name);
                    String destination = departureBoardTrains.get(departureTime).destination;
                    if(!destination.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 107, 22 + i*10,
                            MapColorPalette.getColor(0, 0, 0),
                            departureBoardTrains.get(departureTime).destination);
                    String platform = departureBoardTrains.get(departureTime).platform;
                    if(!platform.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 170, 22 + i*10,
                            MapColorPalette.getColor(0, 0, 0),
                            departureBoardTrains.get(departureTime).platform);
                    String information = departureBoardTrains.get(departureTime).information;
                    if(!information.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 190, 22 + i*10,
                            MapColorPalette.getColor(0, 0, 0),
                            departureBoardTrains.get(departureTime).information);

                    i++;
                }

            }

            tickCount++;
            super.onTick();
        }

    }

    public static class DepartureBoard2 extends MapDisplay{
        int tickCount = 0;

        @Override
        public void onAttached() {
            getLayer(2).clear();
            getLayer(2).draw(Assets.getMapTexture(imgDir + "DepartureBoard2.png"), 0, 0);
            getLayer(0).clear();
            getLayer(0).fillRectangle(0, 30, 128, 67, MapColorPalette.getColor(0, 0, 0));

            super.onAttached();
        }

        @Override
        public void onTick() {
            if( (tickCount % updateTime) == 0){

                getLayer(1).clear();
                getLayer(1).setAlignment(MapFont.Alignment.LEFT);

                int secondsToDisplayOnBoard = TrensMinecat.secondsToDisplayOnBoard;
                DepartureBoardTemplate template = TrensMinecat.departureBoards.get(properties.get("template", String.class));
                LocalDateTime now = LocalDateTime.now();
                TreeMap<LocalDateTime, Departure> departureBoardTrains = BoardUtils.fillDepartureBoard(now, template.trainLines, template.length, properties.get("template", String.class), true);

                LocalDateTime departureTime = departureBoardTrains.firstKey();
                Duration untilDeparture = Duration.between(now, departureTime);

                if(untilDeparture.minusSeconds(secondsToDisplayOnBoard).isNegative()){
                    getLayer(1).draw(MapFont.MINECRAFT, 5, 47,
                            MapColorPalette.getColor(255, 0, 0),
                            "now");
                }else if(untilDeparture.minusMinutes(5).isNegative()){
                    getLayer(1).draw(MapFont.MINECRAFT, 5, 47,
                            MapColorPalette.getColor(255, 201, 14),
                            departureTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " (" +
                                    (int) untilDeparture.getSeconds() / 60 + "min)");
                }else{
                    getLayer(1).draw(MapFont.MINECRAFT, 5, 47,
                            MapColorPalette.getColor(255, 201, 14),
                            departureTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
                getLayer(1).draw(MapFont.MINECRAFT, 5, 60,
                        MapColorPalette.getColor(255, 201, 14),
                        departureBoardTrains.get(departureTime).name);
                String destination = departureBoardTrains.get(departureTime).destination;
                if(!destination.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 5, 73,
                        MapColorPalette.getColor(255, 201, 14),
                        destination);
                String information = departureBoardTrains.get(departureTime).information;
                if(!information.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 5, 86,
                        MapColorPalette.getColor(255, 201, 14),
                        departureBoardTrains.get(departureTime).information);
            }

            tickCount++;
            super.onTick();
        }
    }

    public static class DepartureBoard3 extends MapDisplay{
        int tickCount = 0;

        @Override
        public void onAttached() {

            getLayer(1).clear();
            getLayer(1).fillRectangle(5, 14, 118, 28, MapColorPalette.getColor(40, 40, 40));
            getLayer(3).clear();
            getLayer(3).draw(Assets.getMapTexture(imgDir + "DepartureBoard3.png"), 0, 0);

            super.onAttached();
        }

        @Override
        public void onTick() {

            if( (tickCount % updateTime) == 0){

                int secondsToDisplayOnBoard = TrensMinecat.secondsToDisplayOnBoard;
                DepartureBoardTemplate template = TrensMinecat.departureBoards.get(properties.get("template", String.class));
                LocalDateTime now = LocalDateTime.now();
                TreeMap<LocalDateTime, Departure> departureBoardTrains = BoardUtils.fillDepartureBoard(now, template.trainLines, template.length, properties.get("template", String.class), true);
                LocalDateTime departureTime = departureBoardTrains.firstKey();
                Duration untilDeparture = Duration.between(now, departureTime);

                getLayer(4).clear();
                getLayer(4).setAlignment(MapFont.Alignment.LEFT);

                if(untilDeparture.minusSeconds(secondsToDisplayOnBoard).isNegative()){
                    //imprimeix el nom del tren gran
                    getLayer(4).draw(Assets.getMapTexture(imgDir + "28px/" +
                            departureBoardTrains.get(departureTime).name + ".png"), 5, 14);
                    getLayer(4).setAlignment(MapFont.Alignment.MIDDLE);
                    getLayer(4).draw(MapFont.MINECRAFT, 74, 23,
                            MapColorPalette.getColor(255, 255, 255),
                            departureBoardTrains.get(departureTime).destination.toUpperCase());

                }else if(untilDeparture.minusMinutes(5).isNegative()){
                    //imprimeix informació
                    getLayer(4).draw(Assets.getMapTexture(imgDir + "28px/" +
                            departureBoardTrains.get(departureTime).name + ".png"), 5, 14);
                    getLayer(4).draw(MapFont.TINY, 97, 15,
                            MapColorPalette.getColor(255, 255, 255),
                            "min");
                    getLayer(4).drawLine(95, 14, 95, 41, getLayer(4).readPixel(5, 14));
                    getLayer(4).setAlignment(MapFont.Alignment.MIDDLE);
                    getLayer(4).draw(MapFont.MINECRAFT, 63, 24,
                            MapColorPalette.getColor(255, 255, 255),
                            departureBoardTrains.get(departureTime).destination);
                    getLayer(4).draw(MapFont.MINECRAFT, 108, 24,
                            MapColorPalette.getColor(255, 255, 255),
                            String.valueOf(untilDeparture.getSeconds()/60));
                }else{
                    //imprimeix logo i hora
                    getLayer(4).draw(Assets.getMapTexture(imgDir + "28px/rodalies.png"), 5, 14);
                    getLayer(4).setAlignment(MapFont.Alignment.RIGHT);
                    getLayer(4).draw(MapFont.MINECRAFT, 119, 24, MapColorPalette.COLOR_WHITE,
                            now.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }

            tickCount++;
            super.onTick();
        }
    }

    public static class DepartureBoard4 extends MapDisplay{
        int tickCount = 0;
        final int maxAcceptableDelay = 20; //time in seconds

        @Override
        public void onAttached() {
            super.onAttached();
            setUpdateWithoutViewers(false);
            getLayer(0).draw(Assets.getMapTexture(imgDir + "DepartureBoard4.png"), 0, 0);
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
                DepartureBoardTemplate template = TrensMinecat.departureBoards.get(properties.get("template", String.class));
                String andana = properties.get("platform", String.class, "");
                TreeMap<LocalDateTime, Departure> departureBoardTrains = BoardUtils.fillDepartureBoard(now, template.trainLines, template.length, properties.get("template", String.class), false);
                TreeMap<LocalDateTime, Departure> departures = new TreeMap<>();

                if(!andana.equals("")){
                    departureBoardTrains.forEach( (time, departure) ->{
                        if(!departure.platform.equals(andana)) departures.put(time, departure);
                    });
                }else{
                    departures.putAll(departureBoardTrains);
                }

                //print train lines on screen
                getLayer(1).clear();
                getLayer(1).setAlignment(MapFont.Alignment.LEFT);
                int i = 0;
                for(LocalDateTime departureTime : departures.keySet()){
                    if(i > template.length) break;
                    Duration untilDeparture = Duration.between(now, departureTime);
                    Departure departure = departures.get(departureTime);
                    boolean isDelayed = !departure.delay.minusSeconds(maxAcceptableDelay).isNegative();

                    if(untilDeparture.minusSeconds(secondsToDisplayOnBoard).isNegative()) {
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i * 14,
                                MapColorPalette.getColor(0, 128, 0), "imminent");
                    }else if(untilDeparture.minusMinutes(5).isNegative()){
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i * 14,
                                MapColorPalette.getColor(0, 128, 0),
                                untilDeparture.getSeconds()/60 + "min");
                    }else{
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i*14,
                                MapColorPalette.getColor(0, 0, 0),
                                departureTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                    getLayer(1).draw(Assets.getMapTexture(imgDir + "11px/" +
                            departure.name + ".png"), 1, 33 + i*14);

                    String destination = departure.destination;
                    if(!destination.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 21, 34 + i*14,
                            MapColorPalette.getColor(0, 0, 0),
                            departure.destination);
                    String platform = departure.platform;
                    if(!platform.equals("_")) getLayer(1).draw(MapFont.MINECRAFT, 99, 34 + i*14,
                            MapColorPalette.getColor(0, 0, 0),
                            departure.platform);
                    String information = departure.information;
                    if(isDelayed){
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        getLayer(1).draw(MapFont.MINECRAFT, 162, 34 + i*14,
                                MapColorPalette.getColor(255, 0, 0),
                                "estim. " + formatter.format(departureTime.plus(departure.delay)));
                    }else if(!information.equals("_")){
                        getLayer(1).draw(MapFont.MINECRAFT, 162, 34 + i*14,
                                MapColorPalette.getColor(0, 0, 0),
                                departure.information);
                    }

                    i++;
                }
            }

            tickCount++;
            super.onTick();
        }

    }

    public static class DepartureBoard5 extends MapDisplay {
        int tickCount = 0;
        final int maxAcceptableDelay = 20; //time in seconds

        @Override
        public void onAttached() {
            super.onAttached();
            setUpdateWithoutViewers(false);
            getLayer(0).draw(Assets.getMapTexture(imgDir + "DepartureBoard5.png"), 0, 0); //pantalla fgc, igual que la manualdisplay 3
        }

        @Override
        public void onTick() {
            super.onTick();
            if(tickCount % updateTime == 0) {

                getLayer(4).clear();
                LocalDateTime now = LocalDateTime.now();
                getLayer(4).setAlignment(MapFont.Alignment.MIDDLE);
                getLayer(4).draw(MapFont.MINECRAFT, 28, 12, MapColorPalette.COLOR_WHITE,
                        now.format(DateTimeFormatter.ofPattern("HH:mm")));

                int secondsToDisplayOnBoard = TrensMinecat.secondsToDisplayOnBoard;
                DepartureBoardTemplate template = TrensMinecat.departureBoards.get(properties.get("template", String.class));
                String andana = properties.get("platform", String.class, "");
                TreeMap<LocalDateTime, Departure> departureBoardTrains = BoardUtils.fillDepartureBoard(now, template.trainLines, template.length, properties.get("template", String.class), false);
                TreeMap<LocalDateTime, Departure> departures = new TreeMap<>();

                if(!andana.equals("")){
                    departureBoardTrains.forEach( (time, departure) ->{
                        if(departure.platform.equals(andana)) departures.put(time, departure);
                    });
                }else{
                    departures.putAll(departureBoardTrains);
                }

                getLayer(2).clear();
                getLayer(3).clear();
                BufferedImage text = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = text.createGraphics();
                g.setFont(TrensMinecat.minecraftiaJavaFont);

                int i = 0;
                for(LocalDateTime departureTime : departures.keySet()) {
                    if (i > 3) break;
                    Departure departure = departures.get(departureTime);
                    LocalDateTime departureWithDelay = departureTime.plus(departure.delay);
                    Duration untilDeparture = Duration.between(now, departureWithDelay);
                    boolean isDelayed = !departure.delay.minusSeconds(maxAcceptableDelay).isNegative();

                    MapTexture lineIcon = Assets.getMapTexture(imgDir + "11px/" + departure.name + ".png");
                    if(!(lineIcon.getHeight() > 1)){
                        lineIcon = Assets.getMapTexture(imgDir + "11px/info.png");
                    }

                    getLayer(3).draw(lineIcon, 5, 47 + 15*i);
                    g.setColor(Color.BLACK);
                    g.drawString(departure.destination, 26, 47 + 15*i);
                    if(!departure.platform.equals("_")) g.drawString(departure.platform, 120, 47 + 15*i);
                    if(isDelayed) g.setColor(new Color(255, 0, 0));

                    if(untilDeparture.minusSeconds(secondsToDisplayOnBoard).isNegative()) {
                        g.drawString("immin.", 133, 47 + 15*i);
                    }else if(untilDeparture.minusMinutes(5).isNegative()){
                        g.drawString(untilDeparture.getSeconds()/60 + "min", 133, 47 + 15*i);
                    }else{
                        g.drawString(departureTime.format(DateTimeFormatter.ofPattern("HH:mm")), 133, 47 + 15*i);
                    }

                    i++;
                }

                g.dispose();
                getLayer(2).draw(MapTexture.fromImage(text),6 , 14); //text offset

            }
            tickCount++;
        }
    }
}