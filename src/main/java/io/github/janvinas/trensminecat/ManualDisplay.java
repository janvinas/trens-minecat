package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;

public abstract class ManualDisplay extends MapDisplay {
    public abstract boolean updateInformation(String displayID, String via, MinecartGroup dadesTren, Integer clearIn);

    public abstract boolean clearInformation(String displayID);
}
