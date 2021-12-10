package io.github.janvinas.trensminecat;
import com.bergerkiller.bukkit.tc.events.GroupRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EventListener implements Listener {
    @EventHandler
    public void onGroupRemove(GroupRemoveEvent event){
        TrensMinecat.getPlugin(TrensMinecat.class).trainTracker.removeTrain(event.getGroup());
    }
}
