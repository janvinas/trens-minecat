package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.MapTexture;
import io.github.janvinas.trensminecat.trainTracker.TrackedTrain;
import io.github.janvinas.trensminecat.trainTracker.TrainTracker;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class ManualDisplays {

    static String imgDir = "img/";

    public static class ManualDisplay1 extends ManualDisplay {
        boolean updateTime = true;
        String brand;

        public boolean updateInformation(String displayID, String name, String displayName, String destination, int clearIn){
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            brand = properties.get("brand", String.class, "rodalies"); //si no s'ha especificat una marca, retorna rodalies.

            String trainLine;
            String dest;
            if(destination.equals("nopara")){
                trainLine = "info";
                dest = "sense parada";
            }else{

                trainLine = BoardUtils.getTrainLine(name);
                dest = destination.toUpperCase();
                if(dest.length() == 0) dest = "DEST. DESCONEG.";
            }

            getLayer(5).clear();
            getLayer(4).clear();
            MapTexture lineIcon = loadTexture(imgDir + "28px/" + trainLine + ".png");
            if(!(lineIcon.getHeight() > 1)){
                dest = displayName;
                lineIcon = loadTexture(imgDir + "28px/what.png");
            }
            getLayer(4).draw(lineIcon, 5, 14);

            //print the destination, wrapping the lines and centering it vertically and horizontally.
            /*BufferedImage destinationText = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = destinationText.createGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setFont(TrensMinecat.minecraftiaJavaFont);

            AttributedString attributedString = new AttributedString(dest);
            LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(attributedString.getIterator(), g.getFontRenderContext());
            int lineCount = 0;
            final int lineSpacing = 11;
            while(lineBreakMeasurer.getPosition() < attributedString.getIterator().getEndIndex()){
                lineBreakMeasurer.nextLayout(100);
                lineCount++;
            }
            //center at 72, 22
            int i = 0;
            while(lineBreakMeasurer.getPosition() < attributedString.getIterator().getRunLimit()){
                TextLayout textLayout = lineBreakMeasurer.nextLayout(100);
                double width = textLayout.getBounds().getWidth();
                //textLayout.draw(g, (float) (72F - width/2F), 22 - lineSpacing*lineCount/2F + 11*i);
                textLayout.draw(g, 30, 30);
                i++;
            }
            g.dispose();
            getLayer(4).draw(MapTexture.fromImage(destinationText), 0, 0);
            */
            BufferedImage destinationText = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = destinationText.createGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            int offset = g.getFontMetrics().stringWidth(dest) / 2;
            g.drawString(dest, 77 - offset, 37);
            g.dispose();
            getLayer(4).draw(MapTexture.fromImage(destinationText), 0, 0);

            updateTime = false;

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                    this.clearInformation(properties.get("ID", String.class));
                }, clearIn * 20L);
            }

            return true;
        }

        public boolean clearInformation(String displayID){
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(4).clear();
            getLayer(4).draw(loadTexture(imgDir + "28px/" + brand + ".png"), 5, 14);
            getLayer(5).clear();

            updateTime = true;
            return true;
        }

        @Override
        public void onAttached() {
            super.onAttached();
            brand = properties.get("brand", String.class, "rodalies"); //si no s'ha especificat una marca, retorna rodalies.
            getLayer(1).clear();
            getLayer(1).fillRectangle(5, 14, 118, 28, MapColorPalette.getColor(40, 40, 40));
            getLayer(3).clear();
            getLayer(3).draw(loadTexture(imgDir + "DepartureBoard3.png"), 0, 0);
            getLayer(4).clear();
            getLayer(4).draw(loadTexture(imgDir + "28px/" + brand + ".png"), 5, 14);

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

    public static class ManualDisplay3 extends ManualDisplay{ //pantalla fgc primer tren (2*1)

        static Font minecraftiaWide = TrensMinecat.minecraftiaJavaFont;
        static MapFont<Character> minecraftia;

        static {
            Map<TextAttribute, Object> attributes = new HashMap<>();
            attributes.put(TextAttribute.TRACKING, -0.125);
            minecraftia = MapFont.fromJavaFont(minecraftiaWide.deriveFont(attributes).deriveFont(8F));
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

            //layer0: permanent background (never updated)
            //layer1: --
            //layer2: circumstancial background (updated from signs)
            //layer3: text and icons (updated every updateTime seconds)
            //layer4: time (updated every tick)

            getLayer(4).clear();
            LocalDateTime now = LocalDateTime.now();
            getLayer(4).setAlignment(MapFont.Alignment.MIDDLE);
            getLayer(4).draw(minecraftia, 38, 10, MapColorPalette.COLOR_WHITE,
                    now.format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        public boolean updateInformation(String displayID, String name, String displayName, String destination, int clearIn){
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(2).clear();
            getLayer(3).clear();

            getLayer(2).draw(minecraftia, 58, 11, MapColorPalette.getColor(0x2B, 0x3D, 0x3F), "Primer Tren");
            getLayer(2).fillRectangle(22, 30, 212, 16, MapColorPalette.getColor(200, 200, 200));

            getLayer(2).draw(minecraftia, 24, 34, MapColorPalette.getColor(0x2B, 0x3D, 0x3F), "Destinació");
            getLayer(2).draw(minecraftia, 162, 34, MapColorPalette.getColor(0x2B, 0x3D, 0x3F), "Observacions");

            String trainLine;
            String dest;
            if(destination.equals("nopara")){
                trainLine = "info";
                dest = "Sense parada";
            }else{
                trainLine = BoardUtils.getTrainLine(name);
                dest = destination;
            }

            MapTexture lineIcon = loadTexture(imgDir + "11px/" + trainLine + ".png");
            if(!(lineIcon.getHeight() > 1)){
                dest = displayName;
                lineIcon = loadTexture(imgDir + "11px/what.png");
            }

            getLayer(3).draw(minecraftia, 51, 49, MapColorPalette.COLOR_BLACK, dest);
            getLayer(3).draw(lineIcon, 22, 49);

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                    this.clearInformation(properties.get("ID", String.class));
                }, clearIn * 20L);
            }

            return true;
        }

        public boolean clearInformation(String displayID){
            if(! properties.get("ID", String.class).equals(displayID)) return false;
            getLayer(2).clear();
            getLayer(3).clear();
            return true;
        }
    }

    public static class ManualDisplay4 extends ManualDisplay{  //pantalla ADIF pròxima sortida. 2*1 blocs.

        int tickCount = 0;
        boolean sortidaImmediata = false;
        static MapTexture background = MapTexture.loadPluginResource(JavaPlugin.getPlugin(TrensMinecat.class), "img/ManualDisplay4.png");


        //layer0: black background (onAttached)
        //layer1: static text
        //layer2: image (onAttached)
        //layer3: dynamic text (every tick)
        //layer4: platform number
        //layer5: clock handles (onTick)

        @Override
        public void onAttached() {

            getLayer(0).fillRectangle(0, 10, 256, 85, MapColorPalette.getColor(0x2E, 0x2E, 0X2E));
            getLayer(2).draw(background, 0, 0);
            updatePlatformNumber();

            super.onAttached();
        }
        private void updatePlatformNumber(){
            BufferedImage platformNumber = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = platformNumber.createGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setFont(TrensMinecat.helvetica46JavaFont);
            g.drawString(properties.get("platform", String.class), 159, 80); //platform number
            g.dispose();
            getLayer(4).draw(MapTexture.fromImage(platformNumber), 0, 0);
        }

        @Override
        public void onTick() {
            super.onTick();

            LocalDateTime now = LocalDateTime.now();
            getLayer(5).clear();

            getLayer(5).drawLine(222, 52,
                    222 + getX(now.getSecond(), 60, 20),
                    52 + getY(now.getSecond(), 60, 20),
                    MapColorPalette.getColor(0x76, 0x76, 0x76)
            );
            getLayer(5).drawLine(222, 52,
                    222 + getX(now.getMinute(), 60, 18),
                    52 + getY(now.getMinute(), 60, 18),
                    MapColorPalette.getColor(0x3b, 0x3b, 0x3b)
            );
            getLayer(5).drawLine(222, 52,
                    222 + getX(now.getHour() + now.getMinute() / 60F, 12, 12),
                    52 + getY(now.getHour() + now.getMinute() / 60F, 12, 12),
                    MapColorPalette.getColor(0, 0, 0)
            );

            if(sortidaImmediata){

                int n = tickCount % 20;
                Color c = new Color(255, 242, 0);

                if(n ==0) c = new Color(255, 242, 49);
                if(n == 10) c = new Color(255, 0, 0);

                if(n == 0 || n == 10){
                    getLayer(3).clear();
                    BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = layer3.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    g.setColor(c);
                    g.setFont(TrensMinecat.minecraftiaJavaFont);
                    g.drawString("SORTIDA IMMEDIATA", 6, 88);
                    g.dispose();
                    getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
                }

            }

            tickCount++;
        }

        public boolean updateInformation(String displayID, String name, String displayName, String destination, int clearIn) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            String dest;
            if(destination.equals("nopara")){
                dest = "sense parada";
            }else{
                dest = destination.toUpperCase();
            }

            getLayer(1).clear();
            BufferedImage layer1 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = layer1.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setColor(new Color(255, 242, 0));
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            g.drawString(name, 6, 41); //tren
            g.drawString(displayName, 78, 41); //servei
            g.drawString(dest, 6, 73); //destinació
            sortidaImmediata = true;
            g.dispose();

            updatePlatformNumber();

            getLayer(1).draw(MapTexture.fromImage(layer1), 0, 5); //global offset because the text is off (idk why)

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                    this.clearInformation(properties.get("ID", String.class));
                }, clearIn * 20L);
            }

            return true;

        }

        public boolean clearInformation(String displayID) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;
            getLayer(1).clear();
            getLayer(4).clear();
            updatePlatformNumber();
            getLayer(3).clear();
            sortidaImmediata = false;
            return true;
        }

        private int getX(float angle, float divideBy, float length){
            return (int) Math.round(Math.sin(angle * 2 * Math.PI / divideBy) * length);
        }

        private int getY(float angle, int divideBy, float length){
            return - (int) Math.round(Math.cos(angle * 2 * Math.PI / divideBy) * length);
        }
    }


    public static class ManualDisplay5 extends ManualDisplay{

        static MapTexture background = MapTexture.loadPluginResource(JavaPlugin.getPlugin(TrensMinecat.class), "img/ManualDisplay5.png");

        @Override
        public boolean updateInformation(String displayID, String name, String displayName, String destination, int clearIn) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(1).clear();
            BufferedImage layer1 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = layer1.createGraphics();
            g.setColor(new Color(255, 242, 0));
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            g.drawString(destination, 22, 71);
            g.drawString(displayName, 22, 94);
            g.dispose();
            getLayer(1).draw(MapTexture.fromImage(layer1),0 , 0);

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                    this.clearInformation(properties.get("ID", String.class));
                }, clearIn * 20L);
            }

            return true;
        }

        @Override
        public boolean clearInformation(String displayID) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(1).clear();
            return true;
        }

        @Override
        public void onAttached() {
            super.onAttached();
            getLayer(0).clear();
            getLayer(0).draw(background, 0, 0);
        }

    }

    /*
    public static class ManualDisplay6 extends ManualDisplay{ //pantalla ADIF-rodalies pròximes sortides (idèntica que DepartureBoard4 però actualitza els trens)

        static MapTexture background = MapTexture.loadPluginResource(JavaPlugin.getPlugin(TrensMinecat.class), "img/DepartureBoard4.png");
        TreeMap<LocalDateTime, Departure> departures = new TreeMap<>();
        final int departureBoardLength = 6;
        //maximum time to display untracked delayed trains. If the train doesn't enter the station in this time from scheduled departure it will be removed from display.
        final int maxDisplayTime = 240; //time in seconds
        final int updateTime = 100; //time in ticks between graphics update.
        final Duration maxAllowedDelay = Duration.ofSeconds(20); //maximum allowed delay that won't show the red number
        DepartureBoardTemplate template;

        int tickCount = 0;

        @Override
        public boolean updateInformation(String displayID, String name, String displayName, String destination, int clearIn) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;
            TrainTracker trainTracker = ((TrensMinecat) getPlugin()).trainTracker;

            if(template == null){
                return false;
            }

            //train that has arrived will be removed from display past the clearIn time:
            TrackedTrain arrival = trainTracker.searchTrain(name);
            if(arrival != null){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                    departures.remove(arrival.departureTime);
                }, clearIn * 20L); //time in seconds, so it will be multiplied to be in ticks.
            }

            updateTrainList(template);

            return true;
        }

        @Override
        public boolean clearInformation(String displayID) {
            return false;
        }

        @Override
        public void onAttached() {
            super.onAttached();
            getLayer(0).draw(background , 0, 0);

            String templateName = properties.get("template", String.class);
            template = templateName.equals("") ? null : TrensMinecat.departureBoards.get(templateName);
        }

        @Override
        public void onTick() {
            super.onTick();

            LocalDateTime now = LocalDateTime.now();
            getLayer(2).clear();
            getLayer(2).setAlignment(MapFont.Alignment.MIDDLE);
            getLayer(2).draw(MapFont.MINECRAFT, 227, 7, MapColorPalette.COLOR_BLACK,
                    now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            if( (tickCount % updateTime) == 0){
                int secondsToDisplayOnBoard = TrensMinecat.secondsToDisplayOnBoard;

                if(template != null) updateTrainList(template);

                //print train lines on screen
                getLayer(1).clear();
                getLayer(1).setAlignment(MapFont.Alignment.LEFT);
                int i = 0;
                for(LocalDateTime departureTime : departures.keySet()){
                    if (i > departureBoardLength) break;
                    Departure departure = departures.get(departureTime);
                    Duration untilDeparture = Duration.between(now, departureTime).plus(departure.delay);
                    boolean late = maxAllowedDelay.minus(departure.delay).isNegative();

                    if(untilDeparture.isNegative()){
                        //don't do anything
                    }else if(untilDeparture.minusSeconds(secondsToDisplayOnBoard).isNegative()) {
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i * 14,
                                late ? MapColorPalette.getColor(255, 0, 0) : MapColorPalette.getColor(0, 128, 0),
                                "imminent");
                    }else if(untilDeparture.minusMinutes(5).isNegative()){
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i * 14,
                                late ? MapColorPalette.getColor(255, 0, 0) : MapColorPalette.getColor(0, 128, 0),
                                untilDeparture.getSeconds()/60 + "min");
                    }else{
                        getLayer(1).draw(MapFont.MINECRAFT, 113, 34 + i*14,
                                late ? MapColorPalette.getColor(255, 0, 0) : MapColorPalette.getColor(0, 0, 0),
                                departureTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                    getLayer(1).draw(loadTexture(imgDir + "11px/" +
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
                    if(!information.equals("_")){
                        getLayer(1).draw(MapFont.MINECRAFT, 162, 34 + i*14,
                                MapColorPalette.getColor(0, 0, 0),
                                departure.information);
                    }

                    i++;
                }
            }

            tickCount++;
        }

        private void updateTrainList(DepartureBoardTemplate template){
            LocalDateTime now = LocalDateTime.now();
            TrainTracker trainTracker = ((TrensMinecat) getPlugin()).trainTracker;

            //add next lines so that the panel is always full:
            TreeMap<LocalDateTime, Departure> computedDepartures =
                    BoardUtils.fillDepartureBoard(now, template.trainLines, departureBoardLength, true);
            computedDepartures.forEach((time, departure) -> departures.put(time, departure));

            ArrayList<LocalDateTime> departuresToRemove = new ArrayList<>();
            //iterate over all departures:
            departures.forEach((time, departure) -> {
                //remove very old trains from the display:
                if(Duration.between(now, time).plus(Duration.ofSeconds(maxDisplayTime)).isNegative()){
                    departuresToRemove.add(time);
                }

                //try to update times with TrainTracker:
                TrackedTrain trackedTrain = trainTracker.searchTrain(departure.name);
                if(trackedTrain != null){
                    departure.delay = trackedTrain.delay;
                    getPlugin().getLogger().fine("Train " + departure.name + " is " + departure.delay.toSeconds() + " seconds late");
                }

            });

            departuresToRemove.forEach((time) -> {
                departures.remove(time);
            });
        }
    }
    */
}
