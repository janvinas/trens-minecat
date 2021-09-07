package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapDisplay;

public abstract class ManualDisplay extends MapDisplay {
    public abstract boolean updateInformation(String displayID, String displayName, String destination);

    public abstract boolean clearInformation(String displayID);
}
