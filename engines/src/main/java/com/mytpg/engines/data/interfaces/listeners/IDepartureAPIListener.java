package com.mytpg.engines.data.interfaces.listeners;

import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.stops.Stop;

import java.util.List;

/**
 * Created by stalker-mac on 20.10.16.
 */

public interface IDepartureAPIListener extends IAPIListener<Departure> {
    void onSuccess(List<Departure> argDepartures, Stop argStop);
}
