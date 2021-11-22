package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.properties.CartProperties;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionEject;
import com.bergerkiller.bukkit.tc.signactions.SignActionSpawn;
import com.bergerkiller.bukkit.tc.signactions.spawner.SpawnSign;
import com.bergerkiller.bukkit.tc.signactions.spawner.SpawnSignManager;
import io.github.janvinas.trensminecat.signactions.*;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
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

        /*
        try {
            //InputStream stream = getResource("fonts/Minecraftia-Regular.ttf");
            InputStream stream = this.getResource("fonts/Minecraftia-Regular.ttf");
            minecraftiaJavaFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(stream)).deriveFont(Font.PLAIN, 8);
            //stream = getResource("fonts/Helvetica.ttf");
            stream = this.getResource("fonts/Helvetica.ttf");
            helvetica46JavaFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(stream)).deriveFont(Font.PLAIN, 46);

        } catch (FontFormatException | IOException | NullPointerException e) {
            minecraftiaJavaFont = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
            helvetica46JavaFont = new Font(Font.SANS_SERIF, Font.PLAIN, 43);
            e.printStackTrace();
        }
        */
        minecraftiaJavaFont = new Font("minecraftia", Font.PLAIN, 8);
        helvetica46JavaFont = new Font("helvetica", Font.PLAIN, 46);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //TODO feedback dels comandaments
        if(args.length == 5 && args[0].equalsIgnoreCase("spawn")){
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
            }else if(args[0].equalsIgnoreCase("debug")){
                if(args.length == 2 && args[1].equalsIgnoreCase("templatelist")){
                    sender.sendMessage(departureBoards.toString());
                    return true;
                }
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
                    }
                }

            }else if(args.length == 1 && args[0].equalsIgnoreCase("actualitzarestat")){
                MapDisplay.getAllDisplays(ServiceStatusDisplay.class).forEach(ServiceStatusDisplay::updateDisplay);

                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("trensminecat")){
            if(args.length == 0){
                return Arrays.asList("crear", "configurar", "actualitzarestat", "horn");
            }else if(args.length == 1){
                List<String> options = new ArrayList<>();
                if("crear".startsWith(args[0])) options.add("crear <display|displaymanual|estatdelservei> [tipus] [ID]");
                if("configurar".startsWith(args[0])) options.add("configurar displaymanual andana <número>");
                if("actualitzarestat".startsWith(args[0])) options.add("actualitzarestat");
                if("horn".startsWith(args[0])) options.add("horn");
                return options;
            }
        }


        return super.onTabComplete(sender, command, alias, args);
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
