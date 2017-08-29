/**
 * 
 */
package com.mytpg.engines.data.interfaces;

import com.mytpg.engines.entities.Line;

import java.util.List;

/**
 * @author stalker-mac
 *
 */
public interface ILineDAO {
	List<Line> getAll(final boolean ArgIsDistinct);
	List<Line> getAllByName(final String ArgName);
}
