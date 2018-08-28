package Modules;

import java.util.List;


/**
 * Created by Admon on 31/01/2017.
 */

// Se crean oyentes para utilizar en actividad principal

public interface DirectionFinderListener {

    void onDirectionFinderStart();

    void onDirectionFinderSuccess(List<Route> route);

}
