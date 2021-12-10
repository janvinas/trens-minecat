package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.bergerkiller.bukkit.tc.TCListener;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.events.GroupRemoveEvent;
import com.bergerkiller.bukkit.tc.events.TrainCartsListener;
import com.bergerkiller.bukkit.tc.properties.CartProperties;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.spawner.SpawnSign;
import io.github.janvinas.trensminecat.signactions.*;
import io.github.janvinas.trensminecat.trainTracker.TrainTracker;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class TrensMinecat extends JavaPlugin {

    static HashMap<String, DepartureBoardTemplate> departureBoards = new HashMap<>();
    static Integer secondsToDisplayOnBoard;

    int trainDestroyDelay;
    String dontDestroyTag;
    HashMap<String, Block> trainList = new HashMap<>();

    static Font minecraftiaJavaFont;
    static Font helvetica46JavaFont;

    TrainTracker trainTracker = new TrainTracker();


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
        SignAction.register(new SignActionHorn());
        SignAction.register(new SignActionAudio());

        minecraftiaJavaFont = new Font("minecraftia", Font.PLAIN, 8);
        helvetica46JavaFont = new Font("helvetica", Font.PLAIN, 46);

        trainTracker.loadTrains();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        //TODO feedback dels comandaments
        if(command.getName().equalsIgnoreCase("trensminecat") && args.length == 5 && args[0].equalsIgnoreCase("spawn")){
            List<SpawnSign> signs = TrainCarts.plugin.getSpawnSignManager().getSigns();
            for (SpawnSign sign : signs){
                if(sign.getWorld().getName().equals(args[1]) &&
                        sign.getLocation().getX() == Integer.parseInt(args[2]) &&
                        sign.getLocation().getY() == Integer.parseInt(args[3]) &&
                        sign.getLocation().getZ() == Integer.parseInt(args[4])){
                    sign.spawn();
                }
            }
            return true;
        }else if(args.length >=1 && command.getName().equalsIgnoreCase("trensminecat")){
            if(args[0].equalsIgnoreCase("crear")){
                if(args[1].equalsIgnoreCase("display")){
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
                }else if(args[1].equalsIgnoreCase("displaymanual") && args.length == 4){
                    if(args[2].equalsIgnoreCase("1")){
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay1.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }else if(args[2].equalsIgnoreCase("3")){
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay3.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }else if(args[2].equalsIgnoreCase("4")){
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay4.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ItemUtil.getMetaTag(display).putValue("platform", "0");
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }else if(args[2].equalsIgnoreCase("5")){
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay5.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ItemUtil.getMetaTag(display).putValue("platform", "0");
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }
                }else if(args[1].equalsIgnoreCase("estatdelservei") && args.length == 2){
                    ItemStack display = MapDisplay.createMapItem(ServiceStatusDisplay.class);
                    ((Player) sender).getInventory().addItem(display);

                    return true;
                }
            }else if(args.length == 1 && args[0].equalsIgnoreCase("recarregar")){
                loadMainConfiguration();
                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("horn")){
                MinecartGroup group = CartProperties.getEditing( (Player) sender).getGroup();
                if(group == null){
                    sender.sendMessage("No estàs editant cap tren!");
                    return false;
                }
                SignActionHorn.playSound(group);
                return true;
            }else if(args[0].equalsIgnoreCase("configurar") && args.length >= 2){
                ItemStack heldItem = ((Player) sender).getInventory().getItemInMainHand();
                if(args[1].equalsIgnoreCase("displaymanual")){

                    if(!heldItem.getType().equals(Material.FILLED_MAP)){
                        sender.sendMessage("Agafa el mapa amb la mà dreta per configurar-lo");
                        return false;
                    }

                    if(args.length == 4 && args[2].equalsIgnoreCase("andana")){
                        ItemUtil.getMetaTag(heldItem).putValue("platform", args[3]);
                        return true;
                    }else if(args.length == 4 && args[2].equalsIgnoreCase("marca")){
                        ItemUtil.getMetaTag(heldItem).putValue("brand", args[3]);
                    }else{
                        sender.sendMessage("Propietat desconeguda o argument incorrecte");
                    }
                }

            }else if(args.length == 1 && args[0].equalsIgnoreCase("actualitzarestat")){
                MapDisplay.getAllDisplays(ServiceStatusDisplay.class).forEach(ServiceStatusDisplay::updateDisplay);
                return true;

            }else if(args.length == 1 && args[0].equalsIgnoreCase("info")){
                sender.sendMessage("TrensMinecat versió " + getDescription().getVersion() + " programat per janitus1234 (janitus1234@gmail.com)");
                return true;
            }else if(args.length >= 6 && args[0].equalsIgnoreCase("spawntrain")){
                SpawnableGroup spawnableGroup = SpawnableGroup.parse(args[1]);
                SpawnableGroup.SpawnLocationList spawnLocationList = spawnableGroup.findSpawnLocations(
                        new Location(getServer().getWorld(args[2]),
                                Integer.parseInt(args[3]),
                                Integer.parseInt(args[4]),
                                Integer.parseInt(args[5])),
                        new Vector(1, 0, 0), //vector arbitrari. Intentarà spawnejar el tren en aquesta direcció.
                        SpawnableGroup.SpawnMode.DEFAULT
                        );
                spawnLocationList.loadChunks();
                MinecartGroup minecartGroup = spawnableGroup.spawn(spawnLocationList);
                if(args.length >= 7){
                    LocalDateTime spawningTime = LocalDateTime.now();
                    String formattedSpawnTime = spawningTime.format(DateTimeFormatter.ofPattern("HHmmss"));
                    minecartGroup.getProperties().setTrainName(args[6] + "_" + formattedSpawnTime);
                }
                if(args.length >= 8){ minecartGroup.getProperties().setDestination(args[7]); }
                if(args.length >= 9 && args[8].equalsIgnoreCase("register")){ trainTracker.registerTrain(minecartGroup); }
                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("gettrains")){
                sender.sendMessage(trainTracker.getTrackedTrains().toString());
                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("cleartrainregister")){
                trainTracker.clearTrainRegister();
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> options = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("trensminecat")){
            if(args.length == 0){
                options.add("crear"); options.add("configurar"); options.add("actualitzarestat");
                options.add("horn"); options.add("recarregar"); options.add("info");
            }else{
                if("crear".startsWith(args[0])){
                    if(args.length == 1) {
                        options.add("crear");
                    }else{
                        if("display".startsWith(args[1])) options.add("display <tipus> <id>");
                        if("displaymanual".startsWith(args[1])) options.add("displaymanual <tipus> <id>");
                        if("estatdelservei".startsWith(args[1])) options.add("estatdelservei");
                    }
                }
                if("configurar".startsWith(args[0])){
                    if(args.length == 1){
                        options.add("configurar");
                    }else if("displaymanual".startsWith(args[1])){
                        if(args.length == 2) {
                            options.add("displaymanual");
                        }else{
                            if("andana".startsWith(args[2])) options.add("andana [número]");
                            if("marca".startsWith(args[2])) options.add("marca [marca]");
                        }

                    }
                }
                if("spawn".startsWith(args[0])){
                    if(args.length == 1) options.add("spawn");
                    else if(args.length == 2) options.add ("<world>");
                    else if(args.length == 3) options.add ("<x>");
                    else if(args.length == 4) options.add ("<y>");
                    else if(args.length == 5) options.add ("<z>");
                }
                if("spawntrain".startsWith(args[0])){
                    if(args.length == 1) options.add("spawntrain");
                    if(args[0].equals("spawntrain")) {
                        if (args.length == 2) options.add("<train>");
                        else if (args.length == 3) options.add("<world>");
                        else if (args.length == 4) options.add("<x>");
                        else if (args.length == 5) options.add("<y>");
                        else if (args.length == 6) options.add("<z>");
                        else if (args.length == 7) options.add("[train name]");
                        else if (args.length == 8) options.add("[destination]");
                        else if (args.length == 9) options.add("register");
                    }
                }
                if("horn".startsWith(args[0]) && args.length == 1){
                    options.add("horn");
                }
                if("recarregar".startsWith(args[0]) && args.length == 1){
                    options.add("recarregar");
                }
                if("info".startsWith(args[0]) && args.length == 1){
                    options.add("info");
                }
                if("actualitzarestat".startsWith(args[0]) && args.length == 1){
                    options.add("actualitzarestat");
                }
            }


        }

        return options;
    }

    @Override
    public void onDisable() {
        trainTracker.saveTrains();
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

        trainTracker.registerAllStations();
    }

}
