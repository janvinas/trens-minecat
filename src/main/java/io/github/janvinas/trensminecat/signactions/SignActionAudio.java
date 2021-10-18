package io.github.janvinas.trensminecat.signactions;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;

import java.util.StringTokenizer;

public class SignActionAudio extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("audio");
    }

    @Override
    public boolean canSupportRC() {
        return false;
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.isType("audio")) {
            return false;
        }

        return SignBuildOptions.create()
                .setName("audio")
                .setDescription("play an audio. The third line must match a name of a minecraft audio file in format name.of.audio or an alias defined in the train tags as audio_alias_real.audio.name")
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
        for(String tag : group.getProperties().getTags()){
            if(tag.startsWith("audio_") && tag.matches("/.*_.*_.*/")){
                StringTokenizer t = new StringTokenizer("_");
                t.nextToken();
                String alias = t.nextToken();
                String name = t.nextToken();
                if(alias.equals(audioName)) audioName = name;
            }
        }
        group.getWorld().playSound(group.get(0).getBlock().getLocation(), audioName, 1.0F, 1.0F);
    }

}
