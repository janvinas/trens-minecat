package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.MapTexture;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManualDisplays {
    public static class ManualDisplay1 extends MapDisplay {
        static String imgDir = "io/github/janvinas/trensminecat/img/";

        public boolean updateInformation(String DisplayID, String displayName, String destination){
            if(! properties.get("ID", String.class).equals(DisplayID)) return false;

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

            properties.set("t", true);
            return true;
        }

        public boolean clearInformation(String DisplayID){
            if(! properties.get("ID", String.class).equals(DisplayID)) return false;

            getLayer(4).clear();
            getLayer(4).draw(loadTexture(imgDir + "28px/rodalies.png"), 5, 14);

            getLayer(5).clear();
            getLayer(5).setAlignment(MapFont.Alignment.RIGHT);
            LocalDateTime now = LocalDateTime.now();
            getLayer(5).draw(MapFont.MINECRAFT, 119, 24, MapColorPalette.COLOR_WHITE,
                    now.format(DateTimeFormatter.ofPattern("HH:mm")));
            properties.set("t", false);
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
            properties.set("t", false);

        }

        @Override
        public void onTick() {
            super.onTick();
            if(!properties.get("t", boolean.class)){
                getLayer(5).clear();
                getLayer(5).setAlignment(MapFont.Alignment.RIGHT);
                LocalDateTime now = LocalDateTime.now();
                getLayer(5).draw(MapFont.MINECRAFT, 119, 24, MapColorPalette.COLOR_WHITE,
                        now.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
    }
}
