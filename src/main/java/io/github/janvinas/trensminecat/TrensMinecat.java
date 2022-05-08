package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapTexture;
import com.bergerkiller.bukkit.common.nbt.CommonTag;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.properties.CartProperties;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.spawner.SpawnSign;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.janvinas.trensminecat.signactions.*;
import io.github.janvinas.trensminecat.trainTracker.TrackedStation;
import io.github.janvinas.trensminecat.trainTracker.TrackedTrain;
import io.github.janvinas.trensminecat.trainTracker.TrainTracker;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public class TrensMinecat extends JavaPlugin {

    static HashMap<String, DepartureBoardTemplate> departureBoards = new HashMap<>();
    static Integer secondsToDisplayOnBoard;

    int trainDestroyDelay;
    String dontDestroyTag;
    String dontReportTag = "";
    HashMap<String, Block> trainList = new HashMap<>();

    public static Font minecraftiaJavaFont;
    public static Font helvetica46JavaFont;

    public TrainTracker trainTracker = new TrainTracker();

    int port = 8176;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMainConfiguration();

        if(trainDestroyDelay != 0){
            getServer().getScheduler().scheduleSyncRepeatingTask(this,() ->{
                //check for trains that have not moved:
                for(String train : trainList.keySet()){
                    Collection<MinecartGroup> trainMatches = MinecartGroupStore.matchAll(train);
                    for(MinecartGroup matchingTrain : trainMatches){
                        if( (!matchingTrain.isUnloaded()) && (trainList.get(train).equals(matchingTrain.get(0).getBlock())) ){

                            if(!matchingTrain.getProperties().matchTag(dontDestroyTag)){
                                BlockLocation loc = matchingTrain.getProperties().getLocation();
                                matchingTrain.destroy();
                                getLogger().info("El tren " + train + " a [" + loc.x + "," + loc.y + "," + loc.z + "] ha estat destruït per inactivitat");
                            }
                        }
                    }
                }

                //check for trains very far from spawn:
                //TODO make this configurable
                for(MinecartGroup group : MinecartGroupStore.getGroups()){
                    BlockLocation location = group.get(0).getProperties().getLocation();
                    double distance = Math.sqrt( (location.x)^2 + (location.y - 1500)^2 ); //border center is offset by 1500 on y axis.
                    if(distance > 5500 && group.getWorld().getName().equals("world")){    //border radius is 5500
                        getLogger().info("El tren " + group.getProperties().getTrainName() + " a [" + location.x + "," + location.y + "," + location.z + "] ha estat destruït per estar massa lluny");
                        group.destroy();
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
        SignAction.register(new SignActionAudio(), true);
        SignAction.register(new SignActionUpdateService());



        minecraftiaJavaFont = new Font("minecraftia", Font.PLAIN, 8);
        helvetica46JavaFont = new Font("helvetica", Font.PLAIN, 46);

        trainTracker.loadTrains();
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api", new RequestHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            getLogger().log(Level.INFO, "Servidor web inicialitzat al port " + port);

        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error inicialitzant el servidor web al port " + port);
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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
                if(args[1].equalsIgnoreCase("display") && args.length == 5){
                    String template = args[3].replaceAll("_", " ");
                    String name = args[4].replaceAll("_", " ");
                    ItemStack display;

                    if(args[2].equalsIgnoreCase("1")){
                        display = MapDisplay.createMapItem(MapDisplays.DepartureBoard1.class);
                    }else if(args[2].equalsIgnoreCase("2")){
                        display = MapDisplay.createMapItem(MapDisplays.DepartureBoard2.class);
                    }else if(args[2].equalsIgnoreCase("3")){
                        display = MapDisplay.createMapItem(MapDisplays.DepartureBoard3.class);
                    }else if(args[2].equalsIgnoreCase("4")){
                        display = MapDisplay.createMapItem(MapDisplays.DepartureBoard4.class);
                    }else if(args[2].equalsIgnoreCase("5")){
                        display = MapDisplay.createMapItem(MapDisplays.DepartureBoard5.class);
                    }else{
                        sender.sendMessage("The display with ID " + args[2] + " does not exist");
                        return false;
                    }
                    ItemUtil.getMetaTag(display).putValue("template", template);
                    ItemUtil.getMetaTag(display).putValue("name", name);
                    ItemUtil.getMetaTag(display).putValue("platform", "");
                    ((Player) sender).getInventory().addItem(display);
                    return true;

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
                    }else if(args[2].equalsIgnoreCase("6")){
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay6.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }else if(args[2].equalsIgnoreCase("7")){
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay7.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }else if(args[2].equalsIgnoreCase("8")){
                        sender.sendMessage(ChatColor.AQUA + "Aquest display conté dos subtipus\n" +
                                                                   "Si us plau, especifiqui el tipus (8A o 8B)");
                        return true;
                    }else if(args[2].equalsIgnoreCase("8A")){
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay8A.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }else if(args[2].equalsIgnoreCase("8B")) {
                        ItemStack display = MapDisplay.createMapItem(ManualDisplays.ManualDisplay8B.class);
                        ItemUtil.getMetaTag(display).putValue("ID", args[3]);
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }
                }else if(args[1].equalsIgnoreCase("sldisplay") && args.length == 3){
                    ItemStack display;
                    if(args[2].equalsIgnoreCase("1")){
                        display = MapDisplay.createMapItem(SignLinkDisplays.SignLinkDisplay1.class);
                        ((Player) sender).getInventory().addItem(display);
                        return true;
                    }
                }else if(args[1].equalsIgnoreCase("estatdelservei") && args.length == 2){
                    ItemStack display = MapDisplay.createMapItem(ServiceStatusDisplay.class);
                    ((Player) sender).getInventory().addItem(display);

                    return true;
                }
            }else if(args.length == 1 && args[0].equalsIgnoreCase("recarregar")){
                sender.sendMessage(ChatColor.GOLD + "AVÍS PELS QUE UTILITZEU PAPERMC: " + ChatColor.AQUA + "Si surt per consola un thread dump, ignoreu-lo. A vegades el plugin pot tardar una mica en recàrregar.");
                loadMainConfiguration();
                sender.sendMessage(ChatColor.AQUA + "Configuració regarregada!");
                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("horn")){
                MinecartGroup group = CartProperties.getEditing( (Player) sender).getGroup();
                if (group == null) {
                    sender.sendMessage(ChatColor.RED + "No estàs editant cap tren!");
                    return true;
                }
                SignActionHorn.playSound(group);
                return true;
            }else if(args[0].equalsIgnoreCase("configurar") && args.length >= 2){
                ItemStack heldItem = ((Player) sender).getInventory().getItemInMainHand();
                if(args[1].equalsIgnoreCase("displaymanual")){

                    if(!heldItem.getType().equals(Material.FILLED_MAP)){
                        sender.sendMessage(ChatColor.RED + "Agafa el mapa amb la mà dreta per configurar-lo");
                        return true;
                    }

                    MapDisplay mapDisplay = MapDisplay.getHeldDisplay((Player) sender);
                    if(mapDisplay == null){
                        sender.sendMessage(ChatColor.RED + "No hi ha cap pantalla vinculada a aquest mapa!");
                        return true;
                    }

                    if(args.length == 4 && args[2].equalsIgnoreCase("andana")) {
                        if (args[3].equalsIgnoreCase("reset")) {
                            mapDisplay.properties.set("platform", "");
                            sender.sendMessage(ChatColor.AQUA + "S'ha reiniciat el número d'andana.");
                        } else {
                            mapDisplay.properties.set("platform", args[3]);
                            sender.sendMessage(ChatColor.AQUA + "S'ha configurat \"andana\" = " + args[3]);
                        }
                    }
//                    if(args.length == 4 && args[2].equalsIgnoreCase("idioma")){
//                        if(args[3].equalsIgnoreCase("catala") || args[3].equalsIgnoreCase("reinicia")){
//                            mapDisplay.properties.set("background", MapTexture.loadPluginResource(JavaPlugin.getPlugin(TrensMinecat.class), "img/ManualDisplay4.png"));
//                            sender.sendMessage(ChatColor.AQUA + "S'ha cambiat l'idioma del display a català.");
//                        }else if(args[3].equalsIgnoreCase("castella")){
//                            mapDisplay.properties.set("background", MapTexture.loadPluginResource(JavaPlugin.getPlugin(TrensMinecat.class), "img/ManualDisplay4B.png"));
//                            sender.sendMessage(ChatColor.AQUA + "S'ha cambiat l'idioma del display a castellà.");
//                        }else{
//                            sender.sendMessage(ChatColor.AQUA + "Especifiqui el llenguatge (catala/castella)");
//                        }
//                    }
                    else if(args.length == 4 && args[2].equalsIgnoreCase("marca")) {
                        mapDisplay.properties.set("brand", args[3]);
                        sender.sendMessage(ChatColor.AQUA + "S'ha configurat \"marca\" = " + args[3]);
                    }else if(args.length == 4 && args[2].equalsIgnoreCase("plantilla")) {
                        mapDisplay.properties.set("template", args[3].replaceAll("_", " "));
                        sender.sendMessage(ChatColor.AQUA + "S'ha configurat \"plantilla\" = " + args[3]);
                    }else{
                        sender.sendMessage(ChatColor.AQUA + "Propietat desconeguda o argument incorrecte");
                    }

                    mapDisplay.restartDisplay();
                    return true;
                }else if(args[1].equalsIgnoreCase("sldisplay")){
                    if(!heldItem.getType().equals(Material.FILLED_MAP)){
                        sender.sendMessage(ChatColor.RED + "Agafa el mapa amb la mà dreta per configurar-lo");
                        return true;
                    }
                    MapDisplay mapDisplay = MapDisplay.getHeldDisplay((Player) sender);
                    if(mapDisplay == null){
                        sender.sendMessage(ChatColor.RED + "No hi ha cap pantalla vinculada a aquest mapa!");
                        return true;
                    }

                    if(args.length == 4 && args[2].equalsIgnoreCase("destinacio")){
                        mapDisplay.properties.set("destination", args[3].replaceAll("_"," "));
                        sender.sendMessage(ChatColor.AQUA + "S'ha configurat \"destinació\" = " + args[3]);
                    }else if(args.length == 4 && args[2].equalsIgnoreCase("variable")){
                        mapDisplay.properties.set("variable", args[3].replaceAll("_", " "));
                        sender.sendMessage(ChatColor.AQUA + "S'ha configurat \"variable\" = " + args[3]);
                    }else{
                        sender.sendMessage("Propietat desconeguda o argument incorrecte");
                    }

                    mapDisplay.restartDisplay();
                    return true;
                }

            }else if(args.length == 1 && args[0].equalsIgnoreCase("actualitzarestat")){
                MapDisplay.getAllDisplays(ServiceStatusDisplay.class).forEach(ServiceStatusDisplay::updateDisplay);
                return true;

            }else if(args.length == 1 && args[0].equalsIgnoreCase("info")){
                sender.sendMessage("TrensMinecat versió " + getDescription().getVersion() + ". programat per janitus1234 (janitus1234@gmail.com)");
                return true;
            }else if(args[0].equalsIgnoreCase("spawntrain")){
                CommandLine commandLine = new CommandLine(new SpawnTrainCommand());
                commandLine.setUnmatchedOptionsArePositionalParams(true);
                int returnCode = commandLine.execute(args);
                if (returnCode != 0) {
                    sender.sendMessage(ChatColor.RED + "Error parsing command. For more information see console.");
                }
                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("gettrains")){
                sender.sendMessage(trainTracker.getTrackedTrains().toString());
                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("cleartrainregister")){
                trainTracker.clearTrainRegister();
                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("traininfo")){
                MinecartGroup group = CartProperties.getEditing( (Player) sender).getGroup();
                if(group == null){
                    sender.sendMessage(ChatColor.RED + "No estàs editant cap tren!");
                    return true;
                }
                TrackedTrain trackedTrain = trainTracker.searchTrain(group);
                if(trackedTrain == null){
                    sender.sendMessage("Aquest tren no està registrat!");
                    return true;
                }

                //imprimeix la informació del tren
                sender.sendMessage(ChatColor.AQUA + "S'està editant el tren: " + trackedTrain.trainName);
                sender.sendMessage(ChatColor.AQUA + "Hora de sortida: " + trackedTrain.departureTime);
                sender.sendMessage(ChatColor.AQUA + "Línia i destinació " + trackedTrain.linedest);
                sender.sendMessage(ChatColor.AQUA + "Retard: " + trackedTrain.delay.toSeconds() + " segons");
                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "Pròximes estacions" + ChatColor.RESET + "" + ChatColor.AQUA + " (Nom, hora d'arribada programada, hora d'arribada prevista)");
                for (TrackedStation station : trackedTrain.nextStations) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    LocalDateTime arrivalTime = trackedTrain.departureTime.plus(station.timeFromSpawn);
                    LocalDateTime realArrivalTime = arrivalTime.plus(trackedTrain.delay);
                    sender.sendMessage(ChatColor.AQUA + " - " + station.stationCode + ", " + arrivalTime.format(formatter) + ", " + realArrivalTime.format(formatter));
                }

                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("iteminfo")){

                try{
                    ItemStack heldItem = ((Player) sender).getInventory().getItemInMainHand();
                    Map<String, CommonTag> data = ItemUtil.getMetaTag(heldItem).getData();
                    sender.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "iteminfo");
                    data.forEach((key, value) ->
                            sender.sendMessage(ChatColor.AQUA + key + ChatColor.RESET + " = " + ChatColor.GRAY + value.toString()) );
                }catch(Exception e){
                    sender.sendMessage(ChatColor.RED + "Error obtenint la informació!");
                }
                return true;
            }else if(args.length == 4 && args[0].equalsIgnoreCase("justificant")){
                String playerName = args[1];
                String nomBitllet = args[2];
                String quantitatBitllets = args[3];
                Player target = sender.getServer().getPlayerExact(playerName);
                if (target == null) {
                    sender.sendMessage("Jugador " + playerName + " no es en línia.");
                    return true;
                }
                else{
                    if (nomBitllet == null || quantitatBitllets == null) {
                        sender.sendMessage("Propietat desconeguda o argument incorrecte");
                    } else {
                        //obtenir temps
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                        LocalDateTime now = LocalDateTime.now();

                        //Crear llibre justificant
                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta meta = (BookMeta) book.getItemMeta();
                        assert meta != null;
                        meta.setTitle("Rebut / Recibo / Receipt");
                        meta.setAuthor("MineCat Trànsit");
                        List<String> pages = new ArrayList<String>();
                        pages.add("Usuari §l" + playerName + "§r ha comprat el/s bitllet/s " + nomBitllet + " (x" + quantitatBitllets + ") " + "a les " + dtf.format(now) + "\n\n§o§4No llençi aquest llibre fins que acabi el viatge. Aquest llibre serveix de justificant de pagament.§r");
                        pages.add("Usuario §l" + playerName + "§r ha comprado el/los billete/s " + nomBitllet + " (x" + quantitatBitllets + ") " + "a las " + dtf.format(now) + "\n\n§o§4No tire este libro hasta que acabe el viaje. Este libro sirve como justificante de pago.§r");
                        pages.add("User §l" + playerName + "§r has bought ticket named " + nomBitllet + " (x" + quantitatBitllets + ") " + "at " + dtf.format(now) + "\n\n§o§4Do not throw this book until you end your trip. This book serves as a proof of payment.§r");
                        meta.setPages(pages);
                        book.setItemMeta(meta);
                        target.getInventory().addItem(book);
                    }
                }
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
                        if("display".startsWith(args[1])) options.add("display <tipus> <plantilla> <nom>");
                        if("displaymanual".startsWith(args[1])) options.add("displaymanual <tipus> <id>");
                        if("estatdelservei".startsWith(args[1])) options.add("estatdelservei");
                        if("sldisplay".startsWith(args[1])) options.add("sldisplay <tipus>");
                    }
                }
                if("configurar".startsWith(args[0])){
                    if(args.length == 1){
                        options.add("configurar");
                    }else{
                        if("displaymanual".startsWith(args[1])){
                            if(args.length == 2) {
                                options.add("displaymanual");
                            }else{
                                if("andana".startsWith(args[2])) options.add("andana [número|reset]");
                                if("marca".startsWith(args[2])) options.add("marca [marca]");
                                if("idioma".startsWith(args[2])) options.add("idioma [catala/castella] (sense accents)");
                            }

                        }
                        if("sldisplay".startsWith(args[1])){
                            if(args.length == 2){
                                options.add("sldisplay");
                            }else{
                                if("destinacio".startsWith(args[2])) options.add("destinacio");
                                if("variable".startsWith(args[2])) options.add("variable");
                            }
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
                /*
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
                */
                if("spawntrain".startsWith(args[0]) && args.length == 1){
                    options.add("spawntrain");
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
                if("cleartrainregister".startsWith(args[0]) && args.length == 1){
                    options.add("cleartrainregister");
                }
                if("traininfo".startsWith(args[0]) && args.length == 1){
                    options.add("traininfo");
                }
                if("iteminfo".startsWith(args[0]) && args.length == 1){
                    options.add("iteminfo");
                }
                if("justificant".startsWith(args[0]) && args.length == 1){
                    options.add("justificant");
                }
            }


        }

        return options;
    }

    @Override
    public void onDisable() {
        trainTracker.saveTrains();

        SignAction.unregister(new SignActionDisplayManual());
        SignAction.unregister(new SignActionClearDisplay());
        SignAction.unregister(new SignActionSenseParada());
        SignAction.unregister(new SignActionHorn());
        SignAction.unregister(new SignActionAudio());
        SignAction.unregister(new SignActionUpdateService());
    }

    public void loadMainConfiguration(){
        reloadConfig();
        ConfigurationSection displays = getConfig().getConfigurationSection("pantalles");
        if(displays == null){
            getLogger().log(Level.WARNING, "No s'ha trobat cap pantalla per carregar");
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
        dontReportTag = getConfig().getString("no-api");

        trainTracker.registerAllStations();
        Assets.loadAllAssets();
    }



    static class RequestHandler implements HttpHandler {

        public void handle(HttpExchange t) throws IOException {
            byte [] response;
            int rCode;
            String path = t.getRequestURI().getPath();
            if(path.equals("/api/gettrains")){
                response = getTrains().getBytes();
                rCode = 200;
            }else if(path.equals("/api/registeredstations")) {
                response = getPlugin(TrensMinecat.class).trainTracker.getRegisteredStations().getBytes();
                rCode = 200;
            }else if(path.matches("/api/departures/.*")){
                String station = path.substring(path.lastIndexOf("/") + 1);
                String departureList = getDepartures(station);
                if(departureList == null){
                    response = "ERR station not registered".getBytes();
                    rCode = 404;
                }else{
                    response = departureList.getBytes();
                    rCode = 200;
                }
            }else{
                response = "ERR invalid query".getBytes();
                rCode = 400;
            }
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(rCode, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    private static String getTrains(){
        String trains = "{";
        for(MinecartGroup group : MinecartGroupStore.getGroups()){
            if(group.getProperties().matchTag(getPlugin(TrensMinecat.class).dontReportTag)) continue;
            String trainName = group.getProperties().getTrainName();
            String trainWorld = group.getWorld().getName();
            String coordX = String.valueOf(group.get(0).getBlock().getLocation().getX());
            String coordY = String.valueOf(group.get(0).getBlock().getLocation().getY());
            String coordZ = String.valueOf(group.get(0).getBlock().getLocation().getZ());
            String coords = trainWorld + ":" + coordX + "," + coordY + "," + coordZ;
            trains = trains.concat("\"" + trainName + "\": \"" + coords + "\",");
        }
        if(trains.length() > 10) trains = trains.substring(0, trains.length() - 1);
        trains = trains.concat("}");
        return trains;
    }

    private static String getDepartures(String station){
        if(departureBoards.get(station) == null) return null;

        int length = 10;
        final String[] result = {"["}; //no es pot utilitzar la variable dins del forEach, però sí un arrray d'1 element??? wtf
        TreeMap<LocalDateTime, Departure> departureBoardTrains = BoardUtils.fillDepartureBoard(LocalDateTime.now(), departureBoards.get(station).trainLines, length, station, false);
        departureBoardTrains.forEach((time, departure) -> {
            String departureString = "{";
            departureString = departureString.concat("\"time\": \"" + time.toString() + "\",");
            departureString = departureString.concat("\"delay\": \"" + departure.delay.getSeconds() + "\",");
            departureString = departureString.concat("\"name\": \"" + departure.name + "\",");
            departureString = departureString.concat("\"destination\": \"" + departure.destination + "\",");
            departureString = departureString.concat("\"platform\": \"" + departure.platform + "\",");
            departureString = departureString.concat("\"information\": \"" + departure.information + "\"");
            departureString = departureString.concat("},");

            result[0] = result[0] + departureString;
        });

        if(result[0].charAt(result[0].length() - 1) == ','){
            result[0] = result[0].substring(0, result[0].length() - 1);
        }

        result[0] = result[0].concat("]");
        return result[0];
    }

    @picocli.CommandLine.Command(name="spawntrain", description="spawns a train on a given coordinates")
    static class SpawnTrainCommand implements Callable<Integer> {

        @picocli.CommandLine.Parameters(index="0", type=String.class, paramLabel = "spawntrain", hidden=true)
        String subcommand;
        @picocli.CommandLine.Parameters(index="1", type=String.class, paramLabel = "train", description = "tren a spawnejar")
        String train;
        @picocli.CommandLine.Parameters(index="2", type=String.class, paramLabel = "world", description = "mon on spawnejar el tren")
        String world;
        @picocli.CommandLine.Parameters(index="3", type=int.class, paramLabel = "x", description = "coordenada x")
        int x;
        @picocli.CommandLine.Parameters(index="4", type=int.class, paramLabel = "y", description = "coordenada y")
        int y;
        @picocli.CommandLine.Parameters(index="5", type=int.class, paramLabel = "z", description = "coordenada z")
        int z;
        @Deprecated
        @picocli.CommandLine.Parameters(index="6", type=String.class, paramLabel = "trainName", hidden=true, defaultValue = "")
        String oldTrainName;
        @Deprecated
        @picocli.CommandLine.Parameters(index="7", type=String.class, paramLabel = "destination", hidden=true, defaultValue = "")
        String oldDestination;
        @Deprecated
        @picocli.CommandLine.Parameters(index="8", type=String.class, paramLabel = "register", hidden=true, defaultValue ="")
        String oldRegister;

        @picocli.CommandLine.Option(names = {"-n", "--trainname"}, paramLabel="NAME", description="nom del tren")
        String trainName;
        @picocli.CommandLine.Option(names={"-d", "--destination"}, paramLabel="DEST", description="destinació del tren")
        String destination;
        @picocli.CommandLine.Option(names={"-s", "--setdefaults"}, paramLabel="DEF", description="inclou setdefaults al tren")
        String defaults;
        @picocli.CommandLine.Option(names={"-h", "--heading"}, paramLabel="H", description="intenta spawnejar el tren en aquesta direcció (n/s/e/w)")
        String heading;
        @picocli.CommandLine.Option(names={"-l", "--launch"}, description = "intenta donar energia al tren en la direcció -h")
        boolean launch;
        @picocli.CommandLine.Option(names={"-r", "--register"}, description ="registra el tren al TrainTracker. Requereix -n i -d")
        boolean register;
        @picocli.CommandLine.Option(names={"-o", "--dontround"}, description ="no arrodoneix la hora de spawn")
        boolean dontRound;

        @Override
        public Integer call() throws Exception{

            TrensMinecat plugin = TrensMinecat.getPlugin(TrensMinecat.class);

            SpawnableGroup spawnableGroup = SpawnableGroup.parse(train);

            Vector h;
            if(heading == null){
                h = new Vector (0, 0, 0);
            }else{
                switch (heading) {
                    case "n":
                        h = new Vector(0, 0, -1);
                        break;
                    case "s":
                        h = new Vector(0, 0, 1);
                        break;
                    case "e":
                        h = new Vector(1, 0, 0);
                        break;
                    case "w":
                        h = new Vector(-1, 0, 0);
                        break;
                    default:
                        h = new Vector(0, 0, 0);
                        break;
                }
            }

            SpawnableGroup.SpawnLocationList spawnLocationList = spawnableGroup.findSpawnLocations(
                    new Location(plugin.getServer().getWorld(world), x, y, z), h, SpawnableGroup.SpawnMode.DEFAULT
            );
            spawnLocationList.loadChunks();
            MinecartGroup minecartGroup = spawnableGroup.spawn(spawnLocationList);
            if(launch){ //launch train if the user has specified to do so
                minecartGroup.setForwardForce(10);
            }

            /////this code is kept for backwards compatibility: (it will ignore new flags)
            if(oldTrainName != null && !oldTrainName.equals("")){
                LocalDateTime spawningTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES); //will ignore seconds and nanos on spawnTime.
                String formattedSpawnTime = spawningTime.format(DateTimeFormatter.ofPattern("HHmmss"));
                minecartGroup.getProperties().setTrainName(oldTrainName + "_" + formattedSpawnTime);
                if(oldDestination != null && !oldDestination.equals("")){ minecartGroup.getProperties().setDestination(oldDestination.replaceAll("_", " ")); }
                if(oldRegister.equalsIgnoreCase("register")){ plugin.trainTracker.registerTrain(minecartGroup, spawningTime); }
                return 0;
            }
            /////


            if(destination != null){
                minecartGroup.getProperties().setDestination(destination.replaceAll("_", " "));
            }
            if(trainName != null){
                LocalDateTime spawningTime = LocalDateTime.now();
                if(!dontRound) spawningTime = spawningTime.truncatedTo(ChronoUnit.MINUTES);
                String formattedSpawnTime = spawningTime.format(DateTimeFormatter.ofPattern("HHmmss"));
                minecartGroup.getProperties().setTrainName(trainName + "_" + formattedSpawnTime);

                if(destination != null && register){ plugin.trainTracker.registerTrain(minecartGroup, spawningTime); }
            }
            if(defaults != null){ minecartGroup.getProperties().setDefault(defaults); }

            return 0;

        }
    }

}
