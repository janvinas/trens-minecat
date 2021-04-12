package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Objects;
import java.util.StringTokenizer;

public class TrensMinecat extends JavaPlugin {

    static HashMap<String, DepartureBoardTemplate> departureBoards = new HashMap<>();
    static Integer secondsToDisplayOnBoard;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMainConfiguration();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //TODO feedback dels comandaments
        if(command.getName().equalsIgnoreCase("trensminecat")){
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
    }

}
