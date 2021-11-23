package io.github.janvinas.trensminecat.signactions;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import io.github.janvinas.trensminecat.ManualDisplay;
import io.github.janvinas.trensminecat.ManualDisplays;

import java.util.Collection;

public class SignActionSenseParada extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("noparadisplay");
    }

    @Override
    public boolean canSupportRC() {
        return false;
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.isType("noparadisplay")) {
            return false;
        }

        return SignBuildOptions.create()
                .setName("noparadisplay")
                .setDescription("Mostra el missatge \"Tren sense parada\"")
                .handle(event.getPlayer());
    }

    @Override
    public void execute(SignActionEvent info) {
        String displayId = info.getLine(2);
        int clearIn;
        if(info.getLine(3).length() > 0) {
            clearIn = Integer.parseInt(info.getLine(3));
        }else{
            clearIn = 0;
        }

        if(displayId == null) return;

        if (info.isTrainSign() && info.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON)) {
            if (!info.isPowered()) return;
            String displayName = info.getGroup().getProperties().getDisplayName();
            noStopDisplay(displayId, displayName, clearIn);
        }
    }

    private void noStopDisplay(String displayId, String displayName, int clearIn){
        Class<?>[] classes = ManualDisplays.class.getDeclaredClasses();
        for (Class<?> c : classes) {
            @SuppressWarnings("unchecked")
            Collection<? extends ManualDisplay> displays = MapDisplay.getAllDisplays( (Class<ManualDisplay>) c);

            displays.forEach(display -> {
                display.updateInformation(displayId, displayName, "nopara", clearIn);

            });
        }
    }
}
