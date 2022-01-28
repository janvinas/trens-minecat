package io.github.janvinas.trensminecat;
import com.bergerkiller.bukkit.common.map.MapTexture;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Assets {
    private static final HashMap<String, BufferedImage> images = new HashMap<>();
    private static final JavaPlugin plugin = JavaPlugin.getPlugin(TrensMinecat.class);

    public static void loadAllAssets(){
        plugin.getLogger().log(Level.INFO, "Loading plugin resources");
        images.clear();
        loadAllImages();
        plugin.getLogger().log(Level.INFO, "Loaded " + images.size() + " images.");
    }

    static private void loadAllImages(){       //loads all images in img/ directory

        CodeSource src = TrensMinecat.class.getProtectionDomain().getCodeSource();
        if(src == null){
            plugin.getLogger().log(Level.SEVERE, "Failed to load jar resources!");
            return;
        }

        URL jar = src.getLocation();
        try {
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            ClassLoader classLoader = TrensMinecat.class.getClassLoader();
            while(true){
                ZipEntry e = zip.getNextEntry();
                if(e == null) break;
                if(e.isDirectory()) continue;
                String name = e.getName();
                if(!name.startsWith("img")) continue;
                InputStream inputStream = classLoader.getResourceAsStream(name);
                try{
                    images.put(name, ImageIO.read(Objects.requireNonNull(inputStream)));
                    inputStream.close();
                }catch(IOException|NullPointerException ignored){
                }

            }

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load jar resources!");
            e.printStackTrace();
        }

    }

    public static BufferedImage getImage(String path){
        return images.get(path);
    }

    public static MapTexture getMapTexture(String path) throws MapTexture.TextureLoadException{
        if(images.get(path) == null) throw new MapTexture.TextureLoadException("Couldn't find image in saved assets.");
        return MapTexture.fromImage(images.get(path));
    }
}
