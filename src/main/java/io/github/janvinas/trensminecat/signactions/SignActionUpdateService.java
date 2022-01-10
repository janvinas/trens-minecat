package io.github.janvinas.trensminecat.signactions;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import io.github.janvinas.trensminecat.TrensMinecat;
import io.github.janvinas.trensminecat.trainTracker.TrackedTrain;
import io.github.janvinas.trensminecat.trainTracker.TrainTracker;

public class SignActionUpdateService extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("updateservice");
    }

    @Override
    public boolean canSupportRC() {
        return true;
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.isType("updateservice")) {
            return false;
        }

        return SignBuildOptions.create()
                .setName("updateservice")
                .setDescription("Updates the service of the train, based on the current destination and train name")
                .handle(event.getPlayer());
    }

    @Override
    public void execute(SignActionEvent info) {

        if (info.isTrainSign() && info.isAction(SignActionType.GROUP_ENTER)) {
            if (!info.isPowered()) return;
            TrainTracker tracker = TrensMinecat.getPlugin(TrensMinecat.class).trainTracker;
            MinecartGroup group = info.getGroup();
            TrackedTrain trackedTrain = tracker.searchTrain(group);
            if(trackedTrain == null) return;

            trackedTrain.trainName = group.getProperties().getTrainName();
            trackedTrain.destination = group.getProperties().getDestination();
            String line = trackedTrain.trainName.substring(0, trackedTrain.trainName.indexOf("_"));
            trackedTrain.linedest = line + " " + trackedTrain.destination;
            trackedTrain.addAllStations(tracker, trackedTrain.linedest);

        }
    }
}
