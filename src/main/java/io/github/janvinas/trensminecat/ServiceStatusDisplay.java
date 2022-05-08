package io.github.janvinas.trensminecat;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.sl.API.Variables;

public class ServiceStatusDisplay extends MapDisplay {
    //màximes files: 14
    //màximes columnes: 3
    static String[][] lines = {
            {"R1", "R2", "R2N", "R2S", "R3", "R4", "R7", "R8", "R10", "R11", "R12", "R13", "R14", "R15"},
            {"R16", "R17", "Ca3", "RL1", "RL2", "RT1", "RT2", "RG1", "L6", "L7", "L8", "L12", "R5", "R50"},
            {"R6", "R60", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"},
    };


    @Override
    public void onAttached() {
        super.onAttached();

        getLayer(0).clear();
        getLayer(0).draw(loadTexture("img/ServiceStatusDisplay.png"), 0, 0);
        updateDisplay();

    }

    public void updateDisplay(){
        getLayer(2).clear();

        for(int i = 0; i < lines.length; i++){
            for(int j = 0; j < lines[i].length; j++){
                getLayer(2).draw(
                        loadTexture("img/11px/" + lines[i][j] + ".png"),
                        8 + i * 164,
                        35 + j * 15);

                getLayer(2).draw(
                        MapFont.MINECRAFT,
                        37 + i * 164,
                        37 + j * 15,
                        MapColorPalette.COLOR_BLACK,
                        Variables.get("status" + lines[i][j]).getDefault());
            }
        }
    }
}
