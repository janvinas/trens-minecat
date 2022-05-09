package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.MapTexture;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ManualDisplays {

    static String imgDir = "img/";

    public static class ManualDisplay1 extends ManualDisplay {
        String type = "/";
        boolean updateTime = true;
        String brand;

        public boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn){
            if (via != null) {
                displayID = displayID + via;
            }

            String codiParada = displayID.replaceAll("[0-9]","");

            if(! properties.get("ID", String.class).equals(displayID)) return false;

            brand = properties.get("brand", String.class, "rodalies"); //si no s'ha especificat una marca, retorna rodalies.

            if (Objects.equals(brand, "rodalies")){
                type = "_orange/";
            } else if (Objects.equals(brand, "renfe")){
                type = "_white/";
            }

            String trainLine;
            String dest;
            if(!dadesTren.getProperties().matchTag(codiParada)){
                trainLine = "info";
                dest = "TREN SENSE PARADA";
            } else {
                trainLine = BoardUtils.getTrainLine(dadesTren.getProperties().getTrainName());
                dest = dadesTren.getProperties().getDestination().toUpperCase();
                if(dest.length() == 0) dest = dadesTren.getProperties().getDisplayName();
            }

            getLayer(5).clear();
            getLayer(4).clear();
            MapTexture lineIcon;
            try{
                lineIcon = Assets.getMapTexture(imgDir + "28px" + type + trainLine + ".png");
            } catch(MapTexture.TextureLoadException e) {
                dest = dadesTren.getProperties().getDisplayName();
                lineIcon = Assets.getMapTexture(imgDir + "28px" + type + "what.png");
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
            if (Objects.equals(brand, "rodalies")) {
                g.setColor(new Color(252, 76, 0));
            } else {
                g.setColor(new Color(255, 255, 255));
            }
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            int offset = g.getFontMetrics().stringWidth(dest) / 2;
            g.drawString(dest, 77 - offset, 37);
            g.dispose();
            getLayer(4).draw(MapTexture.fromImage(destinationText), 0, 0);

            updateTime = false;

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
                        this.clearInformation(properties.get("ID", String.class)), clearIn * 20L);
            }

            return true;
        }

        public boolean clearInformation(String displayID){
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(4).clear();
            getLayer(4).draw(Assets.getMapTexture(imgDir + "28px" + type + brand + ".png"), 5, 14);
            getLayer(5).clear();

            updateTime = true;
            return true;
        }

        @Override
        public void onAttached() {
            super.onAttached();
            setUpdateWithoutViewers(false);
            brand = properties.get("brand", String.class, "rodalies"); //si no s'ha especificat una marca, retorna rodalies.
            getLayer(1).clear();
            getLayer(1).fillRectangle(5, 14, 118, 28, MapColorPalette.getColor(40, 40, 40));
            getLayer(3).clear();
            getLayer(3).draw(Assets.getMapTexture(imgDir + "DepartureBoard3.png"), 0, 0);
            getLayer(4).clear();
            getLayer(4).draw(Assets.getMapTexture(imgDir + "28px" + type + brand + ".png"), 5, 14);

        }

        @Override
        public void onTick() {
            super.onTick();
            if(updateTime){
                getLayer(5).clear();
                getLayer(5).setAlignment(MapFont.Alignment.RIGHT);
                LocalDateTime now = LocalDateTime.now();
                if (Objects.equals(brand, "rodalies")) {
                    getLayer(5).draw(MapFont.MINECRAFT, 119, 24, MapColorPalette.getColor(252, 76, 0), now.format(DateTimeFormatter.ofPattern("HH:mm")));
                } else if (Objects.equals(brand, "renfe")){
                    getLayer(5).draw(MapFont.MINECRAFT, 119, 24, MapColorPalette.COLOR_WHITE, now.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        }
    }

    /*
    public static class ManualDisplay2 extends MapDisplay{
        TreeMap<String, LocalDateTime> lastTrains = new TreeMap<>();
        HashMap<String, Duration> trainIntervals = properties.get("trainIntervals", HashMap.class);
        int tickCount = 0;

        public boolean updateInformation(String displayID, String displayName, String destination){
            if (via != null) {
                displayID = displayID + via;
            }
            String codiParada = displayID.replaceAll("[0-9]","");
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
            setUpdateWithoutViewers(false);
            getLayer(0).clear();
            getLayer(0).draw(Assets.getMapTexture(imgDir + "ManualDisplay3.png"), 0, 0);
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

        public boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn){
            if (via != null) {
                displayID = displayID + via;
            }
            String codiParada = displayID.replaceAll("[0-9]","");
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(2).clear();
            getLayer(3).clear();

            getLayer(2).draw(minecraftia, 58, 11, MapColorPalette.getColor(0x2B, 0x3D, 0x3F), "Primer Tren");
            getLayer(2).fillRectangle(22, 30, 212, 16, MapColorPalette.getColor(200, 200, 200));

            getLayer(2).draw(minecraftia, 24, 34, MapColorPalette.getColor(0x2B, 0x3D, 0x3F), "Destinació");
            getLayer(2).draw(minecraftia, 162, 34, MapColorPalette.getColor(0x2B, 0x3D, 0x3F), "Observacions");

            String trainLine;
            String dest;
            if(!dadesTren.getProperties().matchTag(codiParada)){
                trainLine = "info";
                dest = "Sense parada / Sin parada";
            }else{
                trainLine = BoardUtils.getTrainLine(dadesTren.getProperties().getTrainName());
                dest = dadesTren.getProperties().getDestination();
            }

            MapTexture lineIcon = Assets.getMapTexture(imgDir + "11px/" + trainLine + ".png");
            if(!(lineIcon.getHeight() > 1)){
                dest = dadesTren.getProperties().getDestination();
                lineIcon = Assets.getMapTexture(imgDir + "11px/what.png");
            }

            getLayer(3).draw(minecraftia, 51, 49, MapColorPalette.COLOR_BLACK, dest);
            getLayer(3).draw(lineIcon, 22, 49);

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> this.clearInformation(properties.get("ID", String.class)), clearIn * 20L);
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
        Integer retard = 0;
        int tickCount = 0;
        LocalDateTime horaActual;
        boolean textInSpanish = false;
        boolean senseParada = false;
        boolean sortidaImmediata = false;
        boolean sortidaAmbRetard = false;
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
            setUpdateWithoutViewers(false);
            super.onAttached();
        }

        private void updatePlatformNumber(){
            BufferedImage platformNumber = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = platformNumber.createGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setFont(TrensMinecat.helvetica46JavaFont);
            String platform = properties.get("platform", String.class, "");
            int x = 159;
            if(platform.length() > 1) x = 145; //shift platform number slightly to the left
            g.drawString(platform, x, 80); //platform number
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


            if (tickCount % 100 == 0){
                textInSpanish = !textInSpanish;
            }

            if(senseParada && !sortidaImmediata && !sortidaAmbRetard){ //TREN SENSE PARADA
                Color c = new Color(255, 75, 75);
                getLayer(3).clear();
                BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = layer3.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setColor(c);
                g.setFont(TrensMinecat.minecraftiaJavaFont);
                if (!textInSpanish) {
                    g.drawString("NO SE ACERQUEN A LA VÍA", 6, 88); //ANOTACIONES
                } else g.drawString("NO S'APROPIN A LA VIA", 6, 88); //ANOTACIONES
                g.dispose();
                getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
            }

            if(!senseParada && sortidaImmediata && !sortidaAmbRetard){ //TREN AMB SORTIDA INMEDIATA
                int n = tickCount % 20;
                Color c = new Color(255, 196, 0);

                if(n == 0) c = new Color(255, 196, 0);
                if(n == 10) c = new Color(255, 75, 75);

                if(n == 0 || n == 10){
                    getLayer(3).clear();
                    BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = layer3.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    g.setColor(c);
                    g.setFont(TrensMinecat.minecraftiaJavaFont);
                    if (!textInSpanish) {
                        g.drawString("SORTIDA IMMEDIATA", 6, 88); //ANOTACIONES
                    } else g.drawString("SALIDA INMEDIATA", 6, 88); //ANOTACIONES
                    g.dispose();
                    getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
                }
            }

            if(!senseParada && !sortidaImmediata && sortidaAmbRetard){ //TREN AMB RETARD
                Color c = new Color(255, 75, 75);
                getLayer(3).clear();
                BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = layer3.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setColor(c);
                g.setFont(TrensMinecat.minecraftiaJavaFont);
                g.drawString("ESTIMAT / ESTIMADO: " + horaActual.plusSeconds(retard).format(DateTimeFormatter.ofPattern("HH:mm")), 6, 88); //ANOTACIONES
                g.dispose();
                getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
            }

            tickCount++;
        }

        public boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn) {
            if (via != null) {
                displayID = displayID + via;
            }
            String codiParada = displayID.replaceAll("[0-9]","");
            if(!properties.get("ID", String.class).equals(displayID)) return false;

            if(!dadesTren.getProperties().matchTag(codiParada)){
                senseParada = true;
                sortidaImmediata = false;
                sortidaAmbRetard = false;
            }

            getLayer(1).clear();
            BufferedImage layer1 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = layer1.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setColor(new Color(255, 242, 0));
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            horaActual = LocalDateTime.now();

            if (!senseParada) {
                g.drawString(horaActual.plusSeconds(clearIn).format(DateTimeFormatter.ofPattern("HH:mm")), 6, 41); //HORA
                g.drawString(dadesTren.getProperties().getDisplayName().toUpperCase(), 78, 41); //SERVEI
                g.drawString(dadesTren.getProperties().getDestination().toUpperCase(), 6, 73); //DESTINACIÓ

                if (clearIn >= 120){
                    retard = clearIn;
                    sortidaAmbRetard = true;
                    sortidaImmediata = false;
                } else {
                    sortidaAmbRetard = false;
                    sortidaImmediata = true;
                }
            } else {
                g.drawString("00:10", 6, 41); //HORA
                g.drawString("NO S'ATURA", 78, 41); //SERVEI
                g.drawString("TREN SIN PARADA", 6, 73); //DESTINACIÓ
            }

            g.dispose();

            updatePlatformNumber();

            getLayer(1).draw(MapTexture.fromImage(layer1), 0, 5); //global offset because the text is off (idk why)

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
                        this.clearInformation(properties.get("ID", String.class)), clearIn * 20L);
            }

            return true;

        }

        public boolean clearInformation(String displayID) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;
            retard = 0;
            textInSpanish = false;
            senseParada = false;
            sortidaImmediata = false;
            sortidaAmbRetard = false;
            getLayer(1).clear();
            getLayer(4).clear();
            updatePlatformNumber();
            getLayer(3).clear();
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
        public boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;
            getLayer(1).clear();
            BufferedImage layer1 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = layer1.createGraphics();
            g.setColor(new Color(58, 93, 57));
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            g.drawString(dadesTren.getProperties().getDestination(), 22, 67); //Destino
            if (via != null) {
                g.drawString(via, 120, 67); //Vía
            }
            g.drawString(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), 210, 67); //Hora
            g.drawString("SERVICIO: " + dadesTren.getProperties().getDisplayName(), 22, 90); //Servicio (va debajo de "Destino")
            g.dispose();
            getLayer(1).draw(MapTexture.fromImage(layer1),0 , 0);

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
                        this.clearInformation(properties.get("ID", String.class)), clearIn * 20L);
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

    public static class ManualDisplay6 extends ManualDisplay{
        int animationLength = 1;
        String trainDataDest;
        String trainDataName;
        boolean PMR = false;
        boolean hasTrain = false;
        String brand;

        static final Font helvetica = TrensMinecat.helvetica46JavaFont.deriveFont(Font.BOLD, 14);

        @Override
        public void onAttached() {
            getLayer(1).draw(Assets.getMapTexture(imgDir + "ManualDisplay6.png"), 0, 0);
            brand = properties.get("brand", String.class, "rodalies"); //si no s'ha especificat una marca, retorna rodalies.
            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px/" + brand + ".png"), 13, 41);
            setUpdateWithoutViewers(false);
            super.onAttached();
        }

        @Override
        public void onDetached() {
            super.onDetached();
        }

        @Override
        public void onTick() {
            super.onTick();

            if(!hasTrain){
                animationLength = 1;
                getLayer(3).clear();
                getLayer(5).clear();
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                BufferedImage time = new BufferedImage(255,128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = time.createGraphics();
                g.setFont(helvetica);
                g.setColor(new Color(255, 255, 255));
                g.drawString(formatter.format(now), 200, 70);
                g.dispose();
                getLayer(3).draw(MapTexture.fromImage(time), 0, 0);
            }

            else {
                if (PMR) { getLayer(5).draw(Assets.getMapTexture(imgDir + "PMR20px.png"), 222, 66); }
                try {
                    if (trainDataName.toLowerCase().contains("r1_serveia") && trainDataDest.equalsIgnoreCase("Hospitalet de Llobregat") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R1/A/HospitaletDeLlobregat/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r1_serveia") && trainDataDest.equalsIgnoreCase("Maçanet Massanes") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R1/A/MaçanetMassanes/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r1_serveib") && trainDataDest.equalsIgnoreCase("Molins de Rei") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R1/B/MolinsDeRei/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r1_serveib") && trainDataDest.equalsIgnoreCase("Arenys de Mar") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R1/B/ArenysDeMar/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r2s_serveia") && trainDataDest.equalsIgnoreCase("Barcelona - Estació de França") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R2/A/Barcelona-EstacióDeFrança/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r2s_serveia") && trainDataDest.equalsIgnoreCase("Sant Vicenç de Calders") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R2/A/SantVicençDeCalders/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r2s_serveib") && trainDataDest.equalsIgnoreCase("Barcelona - Estació de França") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R2/B/Barcelona-EstacióDeFrança/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r2s_serveib") && trainDataDest.equalsIgnoreCase("Vilanova i la Geltrú") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R2/B/VilanovaILaGeltrú/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r8") && trainDataDest.equalsIgnoreCase("Martorell") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R8/Martorell/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r8") && trainDataDest.equalsIgnoreCase("Granollers Centre") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R8/GranollersCentre/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r10") && trainDataDest.equalsIgnoreCase("Aeroport del Prat") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R10/AeroportDelPrat/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                    if (trainDataName.toLowerCase().contains("r10") && trainDataDest.equalsIgnoreCase("Barcelona - Estació de França") && animationLength > 0 && animationLength < 602) {
                        if (animationLength != 601) {
                            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px_animated/Rodalies/R10/Barcelona-EstacióDeFrança/fotograma" + animationLength + ".png"), 13, 41);
                            animationLength++;
                        }
                        else {
                            animationLength = 0;
                        }
                    }
                } catch (MapTexture.TextureLoadException e){
                    Bukkit.getServer().getConsoleSender().sendMessage("Oops! Errada al carregar animació pel tren [\" + trainDataName + \" amb destinació \" + trainDataDest + \"] (pot ser d'un tren que no tingui cap animació codificada?");
                }
            }
        }

        @Override
        public boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn) {
            if (via != null) {
                displayID = displayID + via;
            }

            if (dadesTren.getProperties().matchTag("-PMR-")){
                PMR = true;
            }

            String codiParada = displayID.replaceAll("[0-9]","");
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            hasTrain = true;
            brand = properties.get("brand", String.class, "rodalies"); //si no s'ha especificat una marca, retorna rodalies.

            getLayer(2).clear();
            getLayer(3).clear();

            String trainLine;
            String dest;
            if (!dadesTren.getProperties().matchTag(codiParada)){
                trainLine = "info";
                dest = "Sense parada / Sin parada";
            }
            else {
                trainLine = BoardUtils.getTrainLine(dadesTren.getProperties().getTrainName());
                dest = dadesTren.getProperties().getDestination();
            }

            MapTexture lineIcon;
            try {
                lineIcon = Assets.getMapTexture(imgDir + "46px/" + trainLine + ".png");
            } catch (MapTexture.TextureLoadException e) {
                lineIcon = Assets.getMapTexture(imgDir + "46px/info.png");
                dest = dadesTren.getProperties().getDestination();
            }

            BufferedImage text = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = text.createGraphics();
            g.setFont(helvetica);
            g.setColor(new Color(255, 255, 255));
            g.drawString(dest.toUpperCase(), 65, 70);

            g.dispose();
            getLayer(2).draw(MapTexture.fromImage(text),0 , 0);
            getLayer(2).draw(lineIcon, 13, 41);
            trainDataDest = dadesTren.getProperties().getDestination();
            trainDataName = dadesTren.getProperties().getTrainName();

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
                        this.clearInformation(properties.get("ID", String.class)), clearIn * 20L);
            }

            return true;
        }

        @Override
        public boolean clearInformation(String displayID) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            hasTrain = false;
            PMR = false;
            getLayer(2).clear();
            getLayer(2).draw(Assets.getMapTexture(imgDir + "46px/" + brand + ".png"), 13, 41);
            getLayer(5).clear();
            return true;
        }
    }

    public static class ManualDisplay7 extends ManualDisplay{
        static final Font helvetica19 = TrensMinecat.helvetica46JavaFont.deriveFont(Font.BOLD, 19);
        static final Font helvetica12 = TrensMinecat.helvetica46JavaFont.deriveFont(Font.BOLD, 12);
        @Override
        public void onAttached() {
            getLayer(1).draw(Assets.getMapTexture(imgDir + "ManualDisplay7.png"), 0, 0);
            this.clearInformation(properties.get("ID", String.class));
            setUpdateWithoutViewers(false);
            super.onAttached();
        }

        @Override
        public boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn) {
            if (via != null) {
                displayID = displayID + via;
            }
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(2).clear();
            BufferedImage departure = new BufferedImage(230,46, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = departure.createGraphics();
            g.setFont(helvetica12);
            g.setColor(new Color(255, 0, 0));
            g.drawString(dadesTren.getProperties().getDestination().toUpperCase(), 4, 13);
            g.drawString(dadesTren.getProperties().getDisplayName().toUpperCase(), 4, 28);
            g.drawString("", 4, 34); //observacions (no en tenim)
            g.dispose();
            getLayer(2).draw(MapTexture.fromImage(departure),13 , 55);

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
                        this.clearInformation(properties.get("ID", String.class)), clearIn * 20L);
            }
            return true;
        }

        @Override
        public boolean clearInformation(String displayID) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;

            getLayer(2).clear();
            BufferedImage renfeRodalies = new BufferedImage(230,46, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = renfeRodalies.createGraphics();
            g.setFont(helvetica19);
            g.setColor(new Color(255, 0, 0));
            g.drawString("RENFE", 84, 20);
            g.drawString("RODALIES", 68, 40);
            g.dispose();
            getLayer(2).draw(MapTexture.fromImage(renfeRodalies),13 , 55);
            return true;
        }
    }

    public static class ManualDisplay8A extends ManualDisplay{  //pantalla ADIF 2 pròxima sortida. 2*1 blocs. (rellotge a la dreta)
        Integer retard = 0;
        int tickCount = 0;
        LocalDateTime horaActual;
        boolean textInSpanish = false;
        boolean senseParada = false;
        boolean sortidaImmediata = false;
        boolean sortidaAmbRetard = false;
        static MapTexture background = MapTexture.loadPluginResource(JavaPlugin.getPlugin(TrensMinecat.class), "img/ManualDisplay8A.png");


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
            setUpdateWithoutViewers(false);
            super.onAttached();
        }

        private void updatePlatformNumber(){
            BufferedImage platformNumber = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = platformNumber.createGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setFont(TrensMinecat.helvetica46JavaFont);
            String platform = properties.get("platform", String.class, "");
            int x = 30;
            if(platform.length() > 1) x = 16; //shift platform number slightly to the left
            g.drawString(platform, x, 85); //platform number
            g.dispose();
            getLayer(4).draw(MapTexture.fromImage(platformNumber), 0, 0);
        }

        @Override
        public void onTick() {

            super.onTick();

            LocalDateTime now = LocalDateTime.now();
            getLayer(5).clear();

            getLayer(5).drawLine(224, 52,
                    224 + getX(now.getSecond(), 60, 20),
                    52 + getY(now.getSecond(), 60, 20),
                    MapColorPalette.getColor(0x76, 0x76, 0x76)
            );
            getLayer(5).drawLine(224, 52,
                    224 + getX(now.getMinute(), 60, 18),
                    52 + getY(now.getMinute(), 60, 18),
                    MapColorPalette.getColor(0x3b, 0x3b, 0x3b)
            );
            getLayer(5).drawLine(224, 52,
                    224 + getX(now.getHour() + now.getMinute() / 60F, 12, 12),
                    52 + getY(now.getHour() + now.getMinute() / 60F, 12, 12),
                    MapColorPalette.getColor(0, 0, 0)
            );


            if (tickCount % 100 == 0){
                textInSpanish = !textInSpanish;
            }

            if(senseParada && !sortidaImmediata && !sortidaAmbRetard){ //TREN SENSE PARADA
                Color c = new Color(255, 75, 75);
                getLayer(3).clear();
                BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = layer3.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setColor(c);
                g.setFont(TrensMinecat.minecraftiaJavaFont);
                if (!textInSpanish) {
                    g.drawString("NO SE ACERQUEN A LA VÍA", 68, 85); //ANOTACIONES
                } else g.drawString("NO S'APROPIN A LA VIA", 68, 85); //ANOTACIONES
                g.dispose();
                getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
            }

            if(!senseParada && sortidaImmediata && !sortidaAmbRetard){ //TREN AMB SORTIDA INMEDIATA
                int n = tickCount % 20;
                Color c = new Color(255, 196, 0);

                if(n == 0) c = new Color(255, 196, 0);
                if(n == 10) c = new Color(255, 75, 75);

                if(n == 0 || n == 10){
                    getLayer(3).clear();
                    BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = layer3.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    g.setColor(c);
                    g.setFont(TrensMinecat.minecraftiaJavaFont);
                    if (!textInSpanish) {
                        g.drawString("SORTIDA IMMEDIATA", 68, 85); //ANOTACIONES
                    } else g.drawString("SALIDA INMEDIATA", 68, 85); //ANOTACIONES
                    g.dispose();
                    getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
                }
            }

            if(!senseParada && !sortidaImmediata && sortidaAmbRetard){ //TREN AMB RETARD
                Color c = new Color(255, 75, 75);
                getLayer(3).clear();
                BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = layer3.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setColor(c);
                g.setFont(TrensMinecat.minecraftiaJavaFont);
                g.drawString("ESTIMAT / ESTIMADO: " + horaActual.plusSeconds(retard).format(DateTimeFormatter.ofPattern("HH:mm")), 68, 85); //ANOTACIONES
                g.dispose();
                getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
            }

            tickCount++;
        }

        public boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn) {
            if (via != null) {
                displayID = displayID + via;
            }
            String codiParada = displayID.replaceAll("[0-9]","");
            if(!properties.get("ID", String.class).equals(displayID)) return false;

            if(!dadesTren.getProperties().matchTag(codiParada)){
                senseParada = true;
                sortidaImmediata = false;
                sortidaAmbRetard = false;
            }

            getLayer(1).clear();
            BufferedImage layer1 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = layer1.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setColor(new Color(255, 242, 0));
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            horaActual = LocalDateTime.now();

            if (!senseParada) {
                g.drawString(horaActual.plusSeconds(clearIn).format(DateTimeFormatter.ofPattern("HH:mm")), 68, 38); //HORA
                g.drawString(dadesTren.getProperties().getDisplayName().toUpperCase(), 107, 38); //SERVEI
                g.drawString(dadesTren.getProperties().getDestination().toUpperCase(), 68, 70); //DESTINACIÓ

                if (clearIn >= 120){
                    retard = clearIn;
                    sortidaAmbRetard = true;
                    sortidaImmediata = false;
                } else {
                    sortidaAmbRetard = false;
                    sortidaImmediata = true;
                }
            } else {
                g.drawString("00:10", 68, 38); //HORA
                g.drawString("NO S'ATURA", 107, 38); //SERVEI
                g.drawString("TREN SIN PARADA", 68, 70); //DESTINACIÓ
            }

            g.dispose();

            updatePlatformNumber();

            getLayer(1).draw(MapTexture.fromImage(layer1), 0, 5); //global offset because the text is off (idk why)

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
                        this.clearInformation(properties.get("ID", String.class)), clearIn * 20L);
            }

            return true;

        }

        public boolean clearInformation(String displayID) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;
            retard = 0;
            textInSpanish = false;
            senseParada = false;
            sortidaImmediata = false;
            sortidaAmbRetard = false;
            getLayer(1).clear();
            getLayer(4).clear();
            updatePlatformNumber();
            getLayer(3).clear();
            return true;
        }

        private int getX(float angle, float divideBy, float length){
            return (int) Math.round(Math.sin(angle * 2 * Math.PI / divideBy) * length);
        }

        private int getY(float angle, int divideBy, float length){
            return - (int) Math.round(Math.cos(angle * 2 * Math.PI / divideBy) * length);
        }
    }

    public static class ManualDisplay8B extends ManualDisplay{  //pantalla ADIF 2 pròxima sortida. 2*1 blocs. (rellotge a la esquerra)
        Integer retard = 0;
        int tickCount = 0;
        LocalDateTime horaActual;
        boolean textInSpanish = false;
        boolean senseParada = false;
        boolean sortidaImmediata = false;
        boolean sortidaAmbRetard = false;
        static MapTexture background = MapTexture.loadPluginResource(JavaPlugin.getPlugin(TrensMinecat.class), "img/ManualDisplay8B.png");


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
            setUpdateWithoutViewers(false);
            super.onAttached();
        }

        private void updatePlatformNumber(){
            BufferedImage platformNumber = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = platformNumber.createGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setFont(TrensMinecat.helvetica46JavaFont);
            String platform = properties.get("platform", String.class, "");
            int x = 194;
            if(platform.length() > 1) x = 202; //shift platform number slightly to the left
            g.drawString(platform, x, 85); //platform number
            g.dispose();
            getLayer(4).draw(MapTexture.fromImage(platformNumber), 0, 0);
        }

        @Override
        public void onTick() {

            super.onTick();

            LocalDateTime now = LocalDateTime.now();
            getLayer(5).clear();

            getLayer(5).drawLine(31, 52,
                    31 + getX(now.getSecond(), 60, 20),
                    52 + getY(now.getSecond(), 60, 20),
                    MapColorPalette.getColor(0x76, 0x76, 0x76)
            );
            getLayer(5).drawLine(31, 52,
                    31 + getX(now.getMinute(), 60, 18),
                    52 + getY(now.getMinute(), 60, 18),
                    MapColorPalette.getColor(0x3b, 0x3b, 0x3b)
            );
            getLayer(5).drawLine(31, 52,
                    31 + getX(now.getHour() + now.getMinute() / 60F, 12, 12),
                    52 + getY(now.getHour() + now.getMinute() / 60F, 12, 12),
                    MapColorPalette.getColor(0, 0, 0)
            );


            if (tickCount % 100 == 0){
                textInSpanish = !textInSpanish;
            }

            if(senseParada && !sortidaImmediata && !sortidaAmbRetard){ //TREN SENSE PARADA
                Color c = new Color(255, 75, 75);
                getLayer(3).clear();
                BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = layer3.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setColor(c);
                g.setFont(TrensMinecat.minecraftiaJavaFont);
                if (!textInSpanish) {
                    g.drawString("NO SE ACERQUEN A LA VÍA", 63, 85); //ANOTACIONES
                } else g.drawString("NO S'APROPIN A LA VIA", 63, 85); //ANOTACIONES
                g.dispose();
                getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
            }

            if(!senseParada && sortidaImmediata && !sortidaAmbRetard){ //TREN AMB SORTIDA INMEDIATA
                int n = tickCount % 20;
                Color c = new Color(255, 196, 0);

                if(n == 0) c = new Color(255, 196, 0);
                if(n == 10) c = new Color(255, 75, 75);

                if(n == 0 || n == 10){
                    getLayer(3).clear();
                    BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = layer3.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    g.setColor(c);
                    g.setFont(TrensMinecat.minecraftiaJavaFont);
                    if (!textInSpanish) {
                        g.drawString("SORTIDA IMMEDIATA", 63, 85); //ANOTACIONES
                    } else g.drawString("SALIDA INMEDIATA", 63, 85); //ANOTACIONES
                    g.dispose();
                    getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
                }
            }

            if(!senseParada && !sortidaImmediata && sortidaAmbRetard){ //TREN AMB RETARD
                Color c = new Color(255, 75, 75);
                getLayer(3).clear();
                BufferedImage layer3 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = layer3.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setColor(c);
                g.setFont(TrensMinecat.minecraftiaJavaFont);
                g.drawString("ESTIMAT / ESTIMADO: " + horaActual.plusSeconds(retard).format(DateTimeFormatter.ofPattern("HH:mm")), 63, 85); //ANOTACIONES
                g.dispose();
                getLayer(3).draw(MapTexture.fromImage(layer3),0 , 5);
            }

            tickCount++;
        }

        public boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn) {
            if (via != null) {
                displayID = displayID + via;
            }
            String codiParada = displayID.replaceAll("[0-9]","");
            if(!properties.get("ID", String.class).equals(displayID)) return false;

            if(!dadesTren.getProperties().matchTag(codiParada)){
                senseParada = true;
                sortidaImmediata = false;
                sortidaAmbRetard = false;
            }

            getLayer(1).clear();
            BufferedImage layer1 = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = layer1.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setColor(new Color(255, 242, 0));
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            horaActual = LocalDateTime.now();

            if (!senseParada) {
                g.drawString(horaActual.plusSeconds(clearIn).format(DateTimeFormatter.ofPattern("HH:mm")), 63, 38); //HORA
                g.drawString(dadesTren.getProperties().getDisplayName().toUpperCase(), 102, 38); //SERVEI
                g.drawString(dadesTren.getProperties().getDestination().toUpperCase(), 63, 70); //DESTINACIÓ

                if (clearIn >= 120){
                    retard = clearIn;
                    sortidaAmbRetard = true;
                    sortidaImmediata = false;
                } else {
                    sortidaAmbRetard = false;
                    sortidaImmediata = true;
                }
            } else {
                g.drawString("00:10", 63, 38); //HORA
                g.drawString("NO S'ATURA", 102, 38); //SERVEI
                g.drawString("TREN SIN PARADA", 63, 70); //DESTINACIÓ
            }

            g.dispose();

            updatePlatformNumber();

            getLayer(1).draw(MapTexture.fromImage(layer1), 0, 5); //global offset because the text is off (idk why)

            if(clearIn != 0){
                getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
                        this.clearInformation(properties.get("ID", String.class)), clearIn * 20L);
            }

            return true;

        }

        public boolean clearInformation(String displayID) {
            if(! properties.get("ID", String.class).equals(displayID)) return false;
            retard = 0;
            textInSpanish = false;
            senseParada = false;
            sortidaImmediata = false;
            sortidaAmbRetard = false;
            getLayer(1).clear();
            getLayer(4).clear();
            updatePlatformNumber();
            getLayer(3).clear();
            return true;
        }

        private int getX(float angle, float divideBy, float length){
            return (int) Math.round(Math.sin(angle * 2 * Math.PI / divideBy) * length);
        }

        private int getY(float angle, int divideBy, float length){
            return - (int) Math.round(Math.cos(angle * 2 * Math.PI / divideBy) * length);
        }
    }

}
