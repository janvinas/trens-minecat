package io.github.janvinas.trensminecat.signactions;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import io.github.janvinas.trensminecat.ManualDisplays;

import java.util.Collection;

public class SignActionClearDisplay extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("reiniciardisplay");
    }

    @Override
    public boolean canSupportRC() {
        return false;
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.isType("reiniciardisplay")) {
            return false;
        }

        return SignBuildOptions.create()
                .setName("reiniciardisplay")
                .setDescription("Reinicia la informació de la pantalla amb un nom donat.")
                .handle(event.getPlayer());
    }

    @Override
    public void execute(SignActionEvent info) {

        String displayId = info.getLine(2);

        if(displayId == null) return;

        if (info.isTrainSign() && info.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON)) {
            if (!info.isPowered()) return;
            clearDisplay(displayId);
        }
    }

    private void clearDisplay(String displayId){
        Collection<ManualDisplays.ManualDisplay1> displays = MapDisplay.getAllDisplays(ManualDisplays.ManualDisplay1.class);
        displays.forEach(display -> {
            display.clearInformation(displayId);
        });
    }
}
