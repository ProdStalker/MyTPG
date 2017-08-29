/**
 * 
 */
package com.mytpg.engines.data.oldapi;

import android.database.Cursor;

import com.mytpg.engines.data.abstracts.AbstractDAO;

import java.util.List;


/**
 * @author stalker-mac
 *
 */
public abstract class APIDAO<T> extends AbstractDAO<T> {
	
	public abstract boolean create(T ArgObj);
	
	public long count()
	{
		return 0;
	}

	public abstract boolean delete(T ArgObj);
	
	public boolean deleteAll()
	{
		return false;
	}
	
	public abstract T find(final long ArgId, final boolean ArgIsComplete);
	
	protected T fromCursor(final Cursor ArgCursor)
	{
		return null;
	}
	
	public abstract List<T> getAll();
	
	public abstract boolean update(T ArgObj);

}
