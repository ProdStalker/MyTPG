/**
 * 
 */
package com.mytpg.engines.data.interfaces;

import android.database.Cursor;
import android.location.Location;

import com.mytpg.engines.entities.stops.Stop;

import java.util.List;

/**
 * @author stalker-mac
 *
 */
public interface IStopDAO {
	boolean addFavorite(Stop ArgStop, boolean argIsDetailled);
    long countFavorites();
	Stop find(String ArgMnemo);
	Stop findByCode(String ArgCode);
    Stop fromCursor(Cursor ArgCursor, boolean ArgIsComplete, boolean ArgWithConnections);
	List<Stop> getAll(final boolean ArgIsPhysical);
	List<Stop> getAll(final boolean ArgIsPhysical, final boolean ArgIsComplete);
    List<Stop> getAll(final boolean ArgIsPhysical, final boolean ArgIsComplete, final boolean ArgWithConnections);
    List<Stop> getAllByName(final String ArgName, final boolean ArgIsComplete);
	List<Stop> getAllFavorites(final boolean ArgIsPhysical, final boolean ArgIsComplete);
	List<Stop> getByIds(List<Long> ArgStopIds);
    List<Stop> getByIds(List<Long> ArgStopIds, boolean ArgIsComplete);
    List<Stop> getByLocation(Location ArgLoc, int ArgNumber);
    Stop getLastFavorite();
    int removeAllFavorites(List<Stop> ArgStops);
	boolean removeFavorite(Stop ArgStop);
	Stop search(String ArgTextToSearch);

}
