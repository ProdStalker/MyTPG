/**
 * 
 */
package com.mytpg.engines.data.interfaces;

import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Line;

import java.util.List;

/**
 * @author stalker-mac
 *
 */
public interface IDepartureDAO {
    long countOfflinesGrouped();
	boolean deleteByLineAndStopAndDestination(long ArgLineId, long ArgStopId, long ArgDestinationId);
	List<Departure> getAllByMnemo(String ArgMnemo, Line[] ArgConnectionsFilter, int ArgDepartureCode);
	List<Departure> getAllByMnemo(String ArgMnemo, List<Line> ArgConnectionsFilter, int ArgDepartureCode);
	List<Departure> getAllGroupedByLineAndStop();
	List<Departure> getDayDepartures(String ArgLine, String ArgMnemo, String ArgDestination);

}
