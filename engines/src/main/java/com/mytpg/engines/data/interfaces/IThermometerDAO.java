/**
 * 
 */
package com.mytpg.engines.data.interfaces;

import com.mytpg.engines.entities.Thermometer;

/**
 * @author stalker-mac
 *
 */
public interface IThermometerDAO {
	Thermometer findByCode(final int ArgDepartureCode);
}
