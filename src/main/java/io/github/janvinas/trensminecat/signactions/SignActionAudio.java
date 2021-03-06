package io.github.janvinas.trensminecat.signactions;

import com.bergerkiller.bukkit.common.utils.PlayerUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import io.github.janvinas.trensminecat.TrensMinecat;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.StringTokenizer;

public class SignActionAudio extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("tagaudio");
    }

    @Override
    public boolean canSupportRC() {
        return false;
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.isType("tagaudio")) {
            return false;
        }

        return SignBuildOptions.create()
                .setName("tagaudio")
                .setDescription("play an audio. The third line must match a name of a minecraft audio file in format name.of.audio or an alias defined in the train tags as audio|alias|real.audio.name")
                .handle(event.getPlayer());
    }

    @Override
    public void execute(SignActionEvent info) {

        if (info.isTrainSign() && info.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON)) {
            if (!info.isPowered()) return;

            MinecartGroup group = info.getGroup();
            playAudio(group, info);
        }
    }

    public static void playAudio(MinecartGroup group, SignActionEvent info){
        String audioName = info.getLine(2);
        for(String tag : group.getProperties().getTags()) {
            if (tag.startsWith("tagaudio") && tag.matches(".*[|].*[|].*")) {
                StringTokenizer t = new StringTokenizer(tag, "|");
                t.nextToken();
                String alias = t.nextToken();
                String name = t.nextToken();
                if (alias.equals(audioName)) audioName = name;
            }
        }
        long delay;
        try{
            delay = Long.parseLong(info.getLine(3));
        }catch(NullPointerException|NumberFormatException e) {
            delay = 0;
        }

        JavaPlugin plugin = TrensMinecat.getPlugin(TrensMinecat.class);
        String finalAudioName = audioName;

        if(info.getLine(1).equals("tagaudio in")){

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,() -> {
                group.forEach(minecartMember -> {
                   minecartMember.getEntity().getPlayerPassengers().forEach(player -> {
                       player.playSound(player.getEyeLocation(), finalAudioName, 1, 1);
                   });
                });
            }, delay * 20L);

        }else{

            World world = group.getWorld();
            Location location = group.get(0).getBlock().getLocation();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                    () -> world.playSound(location, finalAudioName, 1.0F, 1.0F),
                    delay * 20L);

        }

    }

}
