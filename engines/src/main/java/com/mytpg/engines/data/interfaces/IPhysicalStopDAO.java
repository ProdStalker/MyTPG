/**
 * 
 */
package com.mytpg.engines.data.interfaces;

import android.location.Location;

import com.mytpg.engines.entities.stops.PhysicalStop;

import java.util.List;

/**
 * @author stalker-mac
 *
 */
public interface IPhysicalStopDAO {
    List<PhysicalStop> findByStopId(final long ArgStopId);
    List<PhysicalStop> findByStopId(final long ArgStopId, boolean ArgIsComplete);
    List<PhysicalStop> findByStopName(String ArgName);
	List<PhysicalStop> getAll(final boolean ArgIsComplete);
    List<PhysicalStop> getAllByStopId(final long ArgStopId);
	List<PhysicalStop> getByIds(List<Long> ArgStopIds);
	List<PhysicalStop> getByLocation(final Location ArgLoc, final int ArgNumber);
}
