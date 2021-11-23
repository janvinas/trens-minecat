package io.github.janvinas.trensminecat.signactions;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import io.github.janvinas.trensminecat.ManualDisplays;
import io.github.janvinas.trensminecat.ManualDisplay;

import java.util.Arrays;
import java.util.Collection;

public class SignActionDisplayManual extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("displaymanual");
    }

    @Override
    public boolean canSupportRC() {
        return false;
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

        if (info.isTrainSign() && info.isAction(SignActionType.GROUP_ENTER)) {
            if (!info.isPowered()) return;
            String displayName = info.getGroup().getProperties().getDisplayName();
            String destination = info.getGroup().getProperties().getDestination();
            updateDisplay(displayId, displayName, destination, clearIn);
        }
    }

    private boolean updateDisplay(String displayId, String trainDisplayName, String destination, int clearIn){

        Class<?>[] classes = ManualDisplays.class.getDeclaredClasses();

        for (Class<?> c : classes) {
            @SuppressWarnings("unchecked")
            Collection<? extends ManualDisplay> displays = MapDisplay.getAllDisplays( (Class<ManualDisplay>) c);

            displays.forEach(display -> {
                display.updateInformation(displayId, trainDisplayName, destination, clearIn);

            });

        }

        return true;
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.isType("displaymanual")) {
            return false;
        }

        return SignBuildOptions.create()
                .setName("displaymanual")
                .setDescription("Mostra informació del tren a una pantalla amb el nom donat.")
                .handle(event.getPlayer());
    }
}
