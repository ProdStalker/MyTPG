/**
 * 
 */
package com.mytpg.engines.data.interfaces;

import com.mytpg.engines.entities.Connection;

import java.util.List;

/**
 * @author StalkerA
 *
 */
public interface IConnectionDAO {
	boolean delete(final long ArgId);
	List<Connection> getConnectionsByLine(final long ArgLineId);
	List<Connection> getConnectionsByLines(List<Long> ArgLineIds);
	List<Connection> getConnectionsByLinesAndPhysicalStop(List<Long> ArgLineIds, final long ArgPhysicalStopId);
	List<Connection> getConnectionsByPhysicalStop(final long ArgPhysicalStopId);
    List<Connection> getConnectionsByStop(final long ArgStopId);
	List<Connection> getConnectionsByStop(final long ArgStopId, boolean argOnlyFavorite);
	int removeAllFavorites(List<Connection> ArgConnections);

}
