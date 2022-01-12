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
import java.time.LocalDateTime;
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
                String platform = "_";
                if(tokenizer2.hasMoreTokens()) platform = tokenizer2.nextToken();
                line.trackedStations.add(new TrackedStation(stationName, timeFromSpawn, platform));
            }

            lineList.add(line);
        }

    }

    public boolean saveTrains(){
        try {
            FileOutputStream fileOut = new FileOutputStream("plugins/TrensMinecat/registeredtrains");
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
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream("plugins/TrensMinecat/registeredtrains")));
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

    /**
     * Registers a train to the system.
     *
     * @param minecartGroup Train to be registered.
     */
    public void registerTrain(MinecartGroup minecartGroup){
        String destination = minecartGroup.getProperties().getDestination();
        String trainName = minecartGroup.getProperties().getTrainName();
        trackedTrains.add(new TrackedTrain(this, trainName, destination));
    }

    /**
     * Registers a train, but pretends it spawned at another time. (useful if the train spawns at 15:01, but you still want it to be the 15:00 train)
     *
     * @param minecartGroup Train to be registered.
     * @param spawnTime Time we have to pretend the train spawned.
     */
    public void registerTrain(MinecartGroup minecartGroup, LocalDateTime spawnTime){
        String destination = minecartGroup.getProperties().getDestination();
        String trainName = minecartGroup.getProperties().getTrainName();
        TrackedTrain tt = new TrackedTrain(this, trainName, destination);
        tt.departureTime = spawnTime;
        trackedTrains.add(tt);
    }

    public boolean removeTrain(MinecartGroup minecartGroup){
        for(TrackedTrain train : trackedTrains) {
            if (train.trainName.equals(minecartGroup.getProperties().getTrainName())) {
                trackedTrains.remove(train);
                return true;
            }
        }
        return false;
    }

    public void clearTrainRegister(){
        trackedTrains.clear();
    }

    public TrackedTrain searchTrain(MinecartGroup m){
        String trainName = m.getProperties().getTrainName();
        return searchTrain(trainName);
    }

    /**
     * Searches for a tracked train with its name.
     * @param trainName Name of train that is being searched.
     * @return TrackedTrain if found, null if not.
     */
    public TrackedTrain searchTrain(String trainName){
        for(TrackedTrain t : trackedTrains){
            if(t.trainName.equals(trainName)) return t;
        }
        return null;
    }

    public List<TrackedTrain> getTrainsWithNextStation(String station){
        ArrayList<TrackedTrain> trains = new ArrayList<>();
        trackedTrains.forEach((trackedTrain) -> {
            if(trackedTrain.hasStation(station)){
                trains.add(trackedTrain);
            }
        });
        return trains;
    }

    public String getRegisteredStations(){
        String result = "{";
        for (TrackedLine trackedLine : lineList) {
            for (TrackedStation trackedStation : trackedLine.trackedStations) {
                result = result.concat("\"" + trackedStation.stationCode + "\",");
            }
        }
        if(result.charAt(result.length() - 1) == ','){
            result = result.substring(0, result.length() - 1);
        }
        result = result.concat("}");
        return result;
    }
}
