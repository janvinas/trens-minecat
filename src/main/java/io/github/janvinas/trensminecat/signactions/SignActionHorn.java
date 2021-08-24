package io.github.janvinas.trensminecat.signactions;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;

public class SignActionHorn extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("horn");
    }

    @Override
    public boolean canSupportRC() {
        return false;
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.isType("horn")) {
            return false;
        }

        return SignBuildOptions.create()
                .setName("horn")
                .setDescription("play a horn sound defined in the train properties")
                .handle(event.getPlayer());
    }

    @Override
    public void execute(SignActionEvent info) {

        if (info.isTrainSign() && info.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON)) {
            if (!info.isPowered()) return;

            MinecartGroup group = info.getGroup();
            playSound(group);
        }
    }

    public static void playSound(MinecartGroup group){
        for(String tag : group.getProperties().getTags()){
            if(tag.startsWith("horn_")){
                String sound = tag.substring(5);
                group.getWorld().playSound(group.get(0).getBlock().getLocation(), sound, 1.0F, 1.0F);
            }
        }
    }
}
