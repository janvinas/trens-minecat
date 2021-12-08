package io.github.janvinas.trensminecat.trainTracker;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import io.github.janvinas.trensminecat.TrensMinecat;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TrainTracker {
    ArrayList<TrackedTrain> trackedTrains = new ArrayList<>();
    ArrayList<TrackedLine> lineList = new ArrayList<>();

    public void registerAllStations(){
        JavaPlugin plugin = TrensMinecat.getPlugin(TrensMinecat.class);

        File stationListFile = new File(plugin.getDataFolder(), "stationList.yml");
        if (!stationListFile.exists()) {
            plugin.saveResource("stationList.yml", false);
        }
        FileConfiguration stationListConfig = new YamlConfiguration();
        try{
            stationListConfig.load(stationListFile);
        }catch(IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }

        lineList.clear(); //clear line list before adding all stations again.

        Set<String> lines = Objects.requireNonNull(stationListConfig.getConfigurationSection("lines")).getKeys(false);
        for(String section : lines){
            TrackedLine line = new TrackedLine();
            StringTokenizer tokenizer = new StringTokenizer(section, "|");
            line.lineName = tokenizer.nextToken();
            line.spawnFrequency = Duration.ofSeconds(Long.parseLong(tokenizer.nextToken()));

            for(String station : stationListConfig.getStringList("lines." + section)){
                StringTokenizer tokenizer2 = new StringTokenizer(station, "|");
                String stationName = tokenizer2.nextToken();
                Duration timeFromSpawn = Duration.ofSeconds(Long.parseLong(tokenizer2.nextToken()));
                line.trackedStations.add(new TrackedStation(stationName, timeFromSpawn));
            }

            lineList.add(line);
        }

    }

    public boolean saveTrains(){
        try {
            FileOutputStream fileOut = new FileOutputStream("registeredtrains");
            GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(gzOut);
            out.writeObject(trackedTrains);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean loadTrains(){
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream("registeredtrains")));
            trackedTrains = (ArrayList<TrackedTrain>) in.readObject();
            in.close();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            TrensMinecat.getPlugin(TrensMinecat.class).getLogger().log(Level.WARNING, "could not load any train storage file");
            return false;
        }
    }

    public List<TrackedLine> getLines(){
        return lineList;
    }
    public TrackedLine getLineMatching(String linedest) {
        for(TrackedLine line : getLines()){
            if(line.lineName.equals(linedest)) return line;
        }
        return null;
    }
    public ArrayList<TrackedTrain> getTrackedTrains(){
        return trackedTrains;
    }

    public void registerTrain(MinecartGroup minecartGroup){
        String destination = minecartGroup.getProperties().getDestination();
        String trainName = minecartGroup.getProperties().getTrainName();
        trackedTrains.add(new TrackedTrain(this, trainName, destination));
    }
    public void clearTrainRegister(){
        trackedTrains.clear();
    }
}
