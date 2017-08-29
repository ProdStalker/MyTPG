/**
 * 
 */
package com.mytpg.engines.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mytpg.engines.data.interfaces.IDepartureDAO;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class DepartureDAO extends DAO<Departure> implements IDepartureDAO {
	public static final String IdField = "pkDeparture";
	public static final String CodeField = "code";
	public static final String DateField = "date";
	public static final String IsPRMField = "isPRM";

	public static final String FkDestinationField = "fkDestination";
	public static final String FkLineField = "fkLine";
	public static final String FkStopField = "fkStop";
	
	public static final String TableName = "tbldepartures";
    public static final String TABLE_SCRIPT = "CREATE TABLE " + TableName +
            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CodeField + " TEXT NOT NULL collate nocase, " +
            DateField + " INTEGER NOT NULL, " +
            IsPRMField + " INTEGER NOT NULL DEFAULT 0, " +
            FkLineField + " INTEGER NOT NULL, " +
            FkStopField + " INTEGER NOT NULL, " +
            FkDestinationField + " INTEGER NOT NULL);";
	
	public DepartureDAO(DatabaseHelper ArgDBHelper) {
		super(ArgDBHelper);
		// TODO Auto-generated constructor stub
	}

	@Override
	public long count() {
		long number = 0;
		
		try
		{
			Cursor cursor = m_dbHelper.getSqlDB().rawQuery("SELECT COUNT(" + IdField + ") AS number FROM " + TableName , null);
			if (cursor.moveToFirst())
			{
				number = cursor.getLong(cursor.getColumnIndex("number"));
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			number = 0;
		}
		
		return number;
	}

    @Override
    public long countOfflinesGrouped() {
        long number = 0;

        try
        {
            String[] finalSelect = new String[]{IdField,CodeField,DateField,IsPRMField,FkLineField,FkStopField, FkDestinationField};

           /* Cursor cursor = m_dbHelper.getSqlDB().rawQuery("SELECT COUNT(" + FkLineField + ") AS number FROM " + TableName + " GROUP BY " +
                                                            FkLineField + ", " + FkStopField + ", " + FkDestinationField, null);*/
            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, null,null,FkLineField + ", " + FkStopField + ", " + FkDestinationField,null,FkLineField);

            if (cursor.moveToFirst())
            {
                number = cursor.getCount();//cursor.getLong(cursor.getColumnIndex("number"));
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            number = 0;
        }

        return number;
    }

	@Override
	public boolean create(Departure ArgDeparture) {
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(CodeField, ArgDeparture.getCode());
			cv.put(DateField, ArgDeparture.getDate().getTimeInMillis());
			cv.put(IsPRMField, ArgDeparture.isPRM());
			cv.put(FkLineField, ArgDeparture.getLine().getId());
			cv.put(FkStopField, ArgDeparture.getStop().getId());
			cv.put(FkDestinationField,ArgDeparture.getLine().getArrivalStop().getId());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgDeparture.setId(id);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean create(List<Departure> ArgDepartures) {
		if (ArgDepartures == null || ArgDepartures.size() == 0)
		{
			return false;
		}
		
		try
		{
			String sql = "INSERT INTO " + TableName + " (" + CodeField + ", " +
                    										 DateField + ", " +
		                                                     IsPRMField + ", " +
		                                                     FkLineField + ", " +
		                                                     FkStopField + ", " +
		                                                     FkDestinationField + ") " +
		                                                     "VALUES (?,?,?,?,?,?)";
			
			
			SQLiteDatabase db = m_dbHelper.getWritableDatabase();
			
			db.beginTransaction();
			 
			SQLiteStatement stmt = db.compileStatement(sql);
			
			
			for (Departure dep : ArgDepartures)
			{
				stmt.bindLong(1, dep.getCode());
				stmt.bindLong(2, dep.getDate().getTimeInMillis());
				long isPRMNumber = 0;
				if (dep.isPRM())
				{
					isPRMNumber = 1;
				}
				stmt.bindLong(3, isPRMNumber);
				stmt.bindLong(4, dep.getLine().getId());
				stmt.bindLong(5, dep.getStop().getId());
				stmt.bindLong(6, dep.getLine().getArrivalStop().getId());
				
				stmt.execute();
				stmt.clearBindings();
			}
			 
			db.setTransactionSuccessful();
			db.endTransaction();
	
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public void createTable() {
		try
        {
	
			m_dbHelper.getSqlDB().execSQL(TABLE_SCRIPT);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
		}
	}

	@Override
	public boolean delete(Departure ArgDeparture) {
		boolean isOk = false;
		if (ArgDeparture == null || ArgDeparture.getId() < 1)
		{
			return isOk;
		}
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgDeparture.getId())});
			if (numberRowsDeleted > 0)
			{
				isOk = true;
			}
			
			m_dbHelper.getSqlDB().setTransactionSuccessful();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
		}
		catch (IllegalStateException ise)
		{
			ise.printStackTrace();
		}
		finally
		{
			m_dbHelper.getSqlDB().endTransaction();
		}
		
		return isOk;
	}
	
	@Override
	public boolean deleteAll()
	{
		boolean isOk = false;
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, null, null);
			if (numberRowsDeleted > 0)
			{
				isOk = true;
			}
			
			m_dbHelper.getSqlDB().delete("sqlite_sequence", "name=?", new String[]{TableName});//delete from sqlite_sequence where name='your_table';
			
			m_dbHelper.getSqlDB().setTransactionSuccessful();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
		}
		catch (IllegalStateException ise)
		{
			ise.printStackTrace();
		}
		finally
		{
			m_dbHelper.getSqlDB().endTransaction();
		}
		
		return isOk;
	}

    @Override
	public boolean deleteByLineAndStopAndDestination(long ArgLineId, long ArgStopId, long ArgDestinationId) {
		boolean isOk = false;
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, FkLineField + " = ? AND " + FkStopField + " = ? AND " + FkDestinationField + " = ?", new String[]{String.valueOf(ArgLineId), String.valueOf(ArgStopId), String.valueOf(ArgDestinationId)});
			if (numberRowsDeleted > 0)
			{
				isOk = true;
			}
			
			m_dbHelper.getSqlDB().setTransactionSuccessful();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
		}
		catch (IllegalStateException ise)
		{
			ise.printStackTrace();
		}
		finally
		{
			m_dbHelper.getSqlDB().endTransaction();
		}
		
		return isOk;
	}

    /**
     * @param ArgId
     * @param ArgIsComplete
     * @return
     */
    @Override
    public Departure find(long ArgId, boolean ArgIsComplete) {
        Departure departure = null;

        try
        {
            String[] finalSelect = new String[]{IdField,CodeField,DateField,IsPRMField,FkLineField,FkStopField, FkDestinationField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IdField + " = ? ",new String[]{String.valueOf(ArgId)},null,null," LIMIT 1");
            if (cursor.moveToFirst())
            {
                departure = fromCursor(cursor);
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            departure = null;
        }

        return departure;
    }

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Departure findByName(String ArgName) {
        return null;
    }

	@Override
	protected Departure fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
		
		if (ArgCursor.getColumnCount() == 0)
		{
			return null;
		}
		
		final int idIndex = ArgCursor.getColumnIndex(IdField);
		final int codeIndex = ArgCursor.getColumnIndex(CodeField);
		final int dateIndex = ArgCursor.getColumnIndex(DateField);
		final int isPRMIndex = ArgCursor.getColumnIndex(IsPRMField);
		final int fkLineIndex = ArgCursor.getColumnIndex(FkLineField);
		final int fkStopIndex = ArgCursor.getColumnIndex(FkStopField);
		final int fkDestinationIndex = ArgCursor.getColumnIndex(FkDestinationField);
		
		Departure departure = new Departure();
		
		if (idIndex != -1)
		{
			departure.setId(ArgCursor.getLong(idIndex));
		}
		
		if (codeIndex != -1)
		{
			departure.setCode(ArgCursor.getInt(codeIndex));
		}
		
		if (dateIndex != -1)
		{
			departure.getDate().setTimeInMillis(ArgCursor.getLong(dateIndex));
		}
		
		if (isPRMIndex != -1)
		{
			int isPRMNumber = ArgCursor.getInt(isPRMIndex);
			boolean isPRM = false;
			if (isPRMNumber > 0)
			{
				isPRM = true;
			}
			departure.setPRM(isPRM);
		}
		
		if (fkLineIndex != -1)
		{
			LineDAO lineDAO = new LineDAO(m_dbHelper);
			departure.setLine(lineDAO.find(ArgCursor.getLong(fkLineIndex)));
		}
		
		if (fkStopIndex != -1)
		{
			StopDAO stopDAO = new StopDAO(m_dbHelper);
			departure.setStop(stopDAO.find(ArgCursor.getLong(fkStopIndex),false));
		}
		
		if (fkDestinationIndex != -1)
		{
			StopDAO stopDAO = new StopDAO(m_dbHelper);
			departure.getLine().setArrivalStop(stopDAO.find(ArgCursor.getLong(fkDestinationIndex),ArgIsComplete));
		}
		
		return departure;
	}

	@Override
	public List<Departure> getAll() {
		List<Departure> departures = new ArrayList<Departure>();
		
		try
		{
			String[] finalSelect = new String[]{IdField,CodeField,DateField,IsPRMField,FkLineField,FkStopField, FkDestinationField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, null,null,null,null,DateField);
			while (cursor.moveToNext())
			{
				Departure departure = fromCursor(cursor);
				if (departure != null)
				{
					departures.add(departure);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			departures.clear();
		}
		
		return departures;
	}

	@Override
	public List<Departure> getAllByMnemo(String ArgMnemo, Line[] ArgConnectionsFilter, int ArgDepartureCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Departure> getAllByMnemo(String ArgMnemo, List<Line> ArgConnectionsFilter, int ArgDepartureCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Departure> getAllGroupedByLineAndStop() {
		List<Departure> departures = new ArrayList<Departure>();
		
		try
		{
			String[] finalSelect = new String[]{IdField,CodeField,DateField,IsPRMField,FkLineField,FkStopField, FkDestinationField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, null,null,FkLineField + ", " + FkStopField + ", " + FkDestinationField,null,FkLineField);
			while (cursor.moveToNext())
			{
				Departure departure = fromCursor(cursor);
				if (departure != null)
				{
					departures.add(departure);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			departures.clear();
		}
		
		return departures;
	}

	@Override
	public List<Departure> getDayDepartures(String ArgLineName, String ArgMnemo, String ArgDestination) {
		List<Departure> departures = new ArrayList<Departure>();
	
		try
		{
			LineDAO lineDAO = new LineDAO(m_dbHelper);
			Line line = lineDAO.findByName(ArgLineName);
			
			StopDAO stopDAO = new StopDAO(m_dbHelper);
			Stop stop = stopDAO.find(ArgMnemo);
			
			Stop destination = stopDAO.search(ArgDestination);
			
			String[] finalSelect = new String[]{IdField,CodeField,DateField,IsPRMField,FkLineField,FkStopField, FkDestinationField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, FkLineField + " = ? AND " + FkStopField + " = ? AND " + FkDestinationField + " = ?", new String[]{String.valueOf(line.getId()), String.valueOf(stop.getId()), String.valueOf(destination.getId())}, null, null,FkLineField);
			while (cursor.moveToNext())
			{
				Departure departure = fromCursor(cursor,false);
				if (departure != null)
				{
					departures.add(departure);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			departures.clear();
		}
		
		return departures;
	}

	@Override
	public boolean update(Departure ArgObj) {
		// TODO Auto-generated method stub
		return false;
	}

}
