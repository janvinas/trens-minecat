package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.spawner.SpawnSign;
import io.github.janvinas.trensminecat.signactions.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.StringTokenizer;

public class TrensMinecat extends JavaPlugin {

    static HashMap<String, DepartureBoardTemplate> departureBoards = new HashMap<>();
    static Integer secondsToDisplayOnBoard;

    int trainDestroyDelay;
    String dontDestroyTag;
    HashMap<String, Block> trainList = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMainConfiguration();

        if(trainDestroyDelay != 0){
            getServer().getScheduler().scheduleSyncRepeatingTask(this,() ->{
                for(String train : trainList.keySet()){
                    Collection<MinecartGroup> trainMatches = MinecartGroupStore.matchAll(train);
                    for(MinecartGroup matchingTrain : trainMatches){
                        if( (!matchingTrain.isUnloaded()) && (trainList.get(train).equals(matchingTrain.get(0).getBlock())) ){

                            if(!matchingTrain.getProperties().matchTag(dontDestroyTag)){
                                BlockLocation loc = matchingTrain.getProperties().getLocation();
                                matchingTrain.destroy();
                                getLogger().info("El tren " + train + " a " + loc.x + "," + loc.y + "," + loc.z + " ha estat destruït");
                            }
                        }
                    }
                }

                trainList.clear();
                for(MinecartGroup train : MinecartGroupStore.getGroups()){
                    trainList.put(train.getProperties().getTrainName(), train.get(0).getBlock());
                }
            } , 0, trainDestroyDelay);
        }

        SignAction.register(new SignActionDisplayManual());
        SignAction.register(new SignActionClearDisplay());
        SignAction.register(new SignActionSenseParada());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //TODO feedback dels comandaments
        if(args.length == 5 && args[0].equalsIgnoreCase("spawn")){
            Location location = new Location(getServer().getWorld(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
            new SpawnSign(new BlockLocation(location)).spawn();
            return true;
        }else if(command.getName().equalsIgnoreCase("trensminecat")){
            if(args[0].equalsIgnoreCase("crear")){
                if(args[1].equalsIgnoreCase("pantalla")){
                    if(args[2].equalsIgnoreCase("1")){
                        ItemStack display = MapDisplay.createMapItem(MapDisplays.DepartureBoard1.class);
                        ItemUtil.getMetaTag(display).putValue("template", args[3]);
                        ItemUtil.getMetaTag(display).putValue("name", args[4]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;

                    }else if(args[2].equalsIgnoreCase("2")){
                        ItemStack display = MapDisplay.createMapItem(MapDisplays.DepartureBoard2.class);
                        ItemUtil.getMetaTag(display).putValue("template", args[3]);
                        ItemUtil.getMetaTag(display).putValue("name", args[4]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;

                    }else if(args[2].equalsIgnoreCase("3")){
                        ItemStack display = MapDisplay.createMapItem(MapDisplays.DepartureBoard3.class);
                        ItemUtil.getMetaTag(display).putValue("template", args[3]);
                        ItemUtil.getMetaTag(display).putValue("name", args[4]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;

                    }else if(args[2].equalsIgnoreCase("4")){
                        ItemStack display = MapDisplay.createMapItem(MapDisplays.DepartureBoard4.class);
                        ItemUtil.getMetaTag(display).putValue("template", args[3]);
                        ItemUtil.getMetaTag(display).putValue("name", args[4]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;

                    }
                }else if(args[1].equalsIgnoreCase("displaymanual")){
                    if(args[2].equalsIgnoreCase("1")){
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay1.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }
                }
            }else if(args.length == 1 && args[0].equalsIgnoreCase("recarregar")){
                loadMainConfiguration();
                return true;
            }else if(args[0].equalsIgnoreCase("debug")){
                if(args.length == 2 && args[1].equalsIgnoreCase("templatelist")){
                    sender.sendMessage(departureBoards.toString());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDisable() {

    }

    public void loadMainConfiguration(){
        reloadConfig();
        ConfigurationSection displays = getConfig().getConfigurationSection("pantalles");
        ConsoleCommandSender console = getServer().getConsoleSender();
        if(displays == null){
            console.sendMessage("No s'ha trobat cap pantalla per carregar");
            return;
        }
        for(String boardName : Objects.requireNonNull(displays).getKeys(false)){
            DepartureBoardTemplate board = new DepartureBoardTemplate();
            board.length = Integer.parseInt(Objects.requireNonNull(displays.getString(boardName + ".longitud")));

            for(String line : Objects.requireNonNull(displays.getStringList(boardName + ".linies"))){
                StringTokenizer lineTokenizer = new StringTokenizer(line, "|");
                TrainLine trainLine = new TrainLine();

                trainLine.name = lineTokenizer.nextToken();
                trainLine.cron = lineTokenizer.nextToken();
                if(lineTokenizer.hasMoreTokens()) trainLine.destination = lineTokenizer.nextToken();
                if(lineTokenizer.hasMoreTokens()) trainLine.platform = lineTokenizer.nextToken();
                if(lineTokenizer.hasMoreTokens()) trainLine.information = lineTokenizer.nextToken();

                board.trainLines.add(trainLine);
            }
            departureBoards.put(boardName, board);
        }

        secondsToDisplayOnBoard = getConfig().getInt("temps-minim-en-pantalla");
        trainDestroyDelay = getConfig().getInt("destruir-trens-en");
        dontDestroyTag = getConfig().getString("no-destrueixis");
    }

}
