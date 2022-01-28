package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapTexture;
import com.bergerkiller.bukkit.sl.API.Variables;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Those displays contain one or more SignLink variable that are updated every tick.
 */
public class SignLinkDisplays {
    static String imgDir = "img/";

    public static class SignLinkDisplay1 extends MapDisplay {
        String signLinkVariable = "";
        String destination = "";

        @Override
        public void onAttached() {
            signLinkVariable = properties.get("variable", String.class, "");
            destination = properties.get("destination", String.class, "");

            getLayer(1).draw(Assets.getMapTexture(imgDir + "SLDisplay1.png"), 0, 0);

            super.onAttached();
        }

        @Override
        public void onTick() {
            getLayer(2).clear();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            BufferedImage variables = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = variables.createGraphics();
            g.setFont(TrensMinecat.minecraftiaJavaFont);
            g.setColor(new Color(143, 255, 53));
            g.drawString(LocalDateTime.now().format(formatter), 24, 67);
            g.drawString("PROPER", 95, 67);
            g.drawString("TREN", 95, 77);
            g.drawString("DIRECCIÓ: " + destination.toUpperCase(), 24, 109);
            g.setFont(TrensMinecat.minecraftiaJavaFont.deriveFont(16F));
            g.drawString(Variables.get(signLinkVariable).getDefault(), 140, 79);
            g.dispose();

            getLayer(2).draw(MapTexture.fromImage(variables), 0, 12); //global offset

            super.onTick();
        }

    }
}
