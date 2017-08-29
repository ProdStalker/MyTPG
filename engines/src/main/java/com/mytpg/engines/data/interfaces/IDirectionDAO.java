package com.mytpg.engines.data.interfaces;

import com.mytpg.engines.entities.olddirections.Direction;

import java.util.Calendar;

/**
 * Created by stalker-mac on 01.01.15.
 */
public interface IDirectionDAO {
    Direction search(String ArgDeparture, String ArgArrival, Calendar ArgDate, boolean ArgIsArrival);
}
