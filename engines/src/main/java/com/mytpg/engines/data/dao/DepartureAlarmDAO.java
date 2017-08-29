/**
 * 
 */
package com.mytpg.engines.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mytpg.engines.data.interfaces.IDepartureAlarmDAO;
import com.mytpg.engines.entities.DepartureAlarm;
import com.mytpg.engines.entities.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author StalkerA
 *
 */
public class DepartureAlarmDAO extends DAO<DepartureAlarm> implements IDepartureAlarmDAO {
	public final static String IdField = "pkDepartureAlarm";
	public final static String DepartureCodeField = "departureCode";
	public final static String DateField = "date";
	public final static String MinutesField = "minutes";
	
	public final static String FkLineField = "fkLine";
	public final static String FkStopField = "fkStop";
	public final static String FkDestinationField = "fkDestination";
	
	public final static String TableName = "tbldeparturealarms";
	
	public final static String TABLE_SCRIPT = "CREATE TABLE " + TableName +
									            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
									            	  DepartureCodeField + " INTEGER NOT NULL, " +
									            	  DateField + " INTEGER NOT NULL, " +
			                                          MinutesField + " INTEGER NOT NULL, " +
									            	  FkLineField + " INTEGER NOT NULL, " +
									            	  FkStopField + " INTEGER NOT NULL, " +
									            	  FkDestinationField + " INTEGER NOT NULL);";
	
	private final static String[] BASE_SELECT = new String[]{IdField,DepartureCodeField,DateField,MinutesField,FkLineField,FkStopField,FkDestinationField};
	
	public DepartureAlarmDAO(DatabaseHelper ArgDBHelper) {
		super(ArgDBHelper);
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
	public boolean create(DepartureAlarm ArgDepartureAlarm) {
		try
		{
			if (ArgDepartureAlarm.getDepartureCode() < 1 ||
				ArgDepartureAlarm.getLine().getId() < 1 ||
				ArgDepartureAlarm.getStop().getId() < 1 ||
				ArgDepartureAlarm.getLine().getArrivalStop().getId() < 1)
			{
				return false;
			}
			ContentValues cv = new ContentValues();
			cv.put(DepartureCodeField, ArgDepartureAlarm.getDepartureCode());
			cv.put(DateField, ArgDepartureAlarm.getDate().getTimeInMillis());
			cv.put(MinutesField, ArgDepartureAlarm.getMinutes());
			cv.put(FkLineField, ArgDepartureAlarm.getLine().getId());
			cv.put(FkStopField, ArgDepartureAlarm.getStop().getId());
			cv.put(FkDestinationField, ArgDepartureAlarm.getLine().getArrivalStop().getId());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgDepartureAlarm.setId(id);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean create(List<DepartureAlarm> ArgDepartureAlarms) {
		if (ArgDepartureAlarms == null || ArgDepartureAlarms.size() == 0)
		{
			return false;
		}
		
		try
		{
			String sql = "INSERT INTO " + TableName + " (" + DepartureCodeField + ", " +  
                											 DateField + ", " +
												   			 MinutesField + ", " +
															 FkLineField + ", " +
										                     FkStopField + ", " +
										                     FkDestinationField + ") " +
										                    "VALUES (?,?,?,?,?,?)";

			SQLiteDatabase db = m_dbHelper.getWritableDatabase();
			
			db.beginTransaction();
			
			final int NumberElements = ArgDepartureAlarms.size();
			
			SQLiteStatement stmt = db.compileStatement(sql);
			int i = 0;
			while (i < NumberElements)
			{
				DepartureAlarm departureAlarm = ArgDepartureAlarms.get(i);
				
				if (departureAlarm.getDepartureCode() < 1 ||
					departureAlarm.getLine().getId() < 1 ||
					departureAlarm.getStop().getId() < 1 ||
					departureAlarm.getLine().getArrivalStop().getId() < 1)
				{
					i++;
					continue;
				}
				
				stmt.bindLong(1, departureAlarm.getDepartureCode());
				stmt.bindLong(2, departureAlarm.getDate().getTimeInMillis());
				stmt.bindLong(3, departureAlarm.getMinutes());
				stmt.bindLong(4, departureAlarm.getLine().getId());
				stmt.bindLong(5, departureAlarm.getStop().getId());
				stmt.bindLong(6, departureAlarm.getLine().getArrivalStop().getId());
				
				final long Id = stmt.executeInsert();
				departureAlarm.setId(Id);
				stmt.clearBindings();
				
				i++;
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
	public boolean delete(DepartureAlarm ArgDepartureAlarm) {
		boolean isOk = false;
		if (ArgDepartureAlarm == null || ArgDepartureAlarm.getId() < 1)
		{
			return isOk;
		}
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgDepartureAlarm.getId())});
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
		
		if (count() == 0)
		{
			deleteAll();
		}
		
		return isOk;
	}

	@Override
	public boolean deleteByCode(int ArgDepartureCode) {
		boolean isOk = false;
	
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, DepartureCodeField + " = ?", new String[]{String.valueOf(ArgDepartureCode)});
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
		
		if (count() == 0)
		{
			deleteAll();
		}
		
		return isOk;
	}

	@Override
	public boolean deleteAll() {
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

    /**
     * @param ArgId
     * @param ArgIsComplete
     * @return
     */
    @Override
    public DepartureAlarm find(long ArgId, boolean ArgIsComplete) {
        return null;
    }

    /**
     * @param ArgName
     * @return
     */
    @Override
    public DepartureAlarm findByName(String ArgName) {
        return null;
    }

	@Override
	protected DepartureAlarm fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
		if (ArgCursor.getCount() == 0)
		{
			return null;
		}
		
		DepartureAlarm departureAlarm = new DepartureAlarm();
		
		int idIndex = ArgCursor.getColumnIndex(IdField);
		int departureCodeIndex = ArgCursor.getColumnIndex(DepartureCodeField);
		int dateIndex = ArgCursor.getColumnIndex(DateField);
		int minutesIndex = ArgCursor.getColumnIndex(MinutesField);
		final int fkLineIndex = ArgCursor.getColumnIndex(FkLineField);
		final int fkStopIndex = ArgCursor.getColumnIndex(FkStopField);
		final int fkDestinationIndex = ArgCursor.getColumnIndex(FkDestinationField);
		
		if (idIndex != -1)
		{
			departureAlarm.setId(ArgCursor.getLong(idIndex));
		}
		
		if (departureCodeIndex != -1)
		{
			departureAlarm.setDepartureCode(ArgCursor.getInt(departureCodeIndex));
		}
		
		if (dateIndex != -1)
		{
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(ArgCursor.getLong(dateIndex));
			departureAlarm.setDate(cal);
		}

		if (minutesIndex != -1)
		{
			departureAlarm.setMinutes(ArgCursor.getInt(minutesIndex));
		}
		
		if (fkLineIndex != -1)
		{
			LineDAO lineDAO = new LineDAO(m_dbHelper);
			departureAlarm.setLine(lineDAO.find(ArgCursor.getLong(fkLineIndex)));
		}
		
		if (fkStopIndex != -1)
		{
			StopDAO stopDAO = new StopDAO(m_dbHelper);
			departureAlarm.setStop(stopDAO.find(ArgCursor.getLong(fkStopIndex)));
		}
		
		if (fkDestinationIndex != -1)
		{
			StopDAO stopDAO = new StopDAO(m_dbHelper);
			departureAlarm.getLine().setArrivalStop(stopDAO.find(ArgCursor.getLong(fkDestinationIndex)));
		}
		
		return departureAlarm;
	}

	@Override
	public List<DepartureAlarm> getAll() {
		List<DepartureAlarm> departureAlarms = new ArrayList<DepartureAlarm>();
		
		try
		{
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, null, null, null, null, DateField);
			while (cursor.moveToNext())
			{
				DepartureAlarm departureAlarm = fromCursor(cursor);
				if (departureAlarm != null)
				{
					departureAlarms.add(departureAlarm);
				}
			}
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			departureAlarms.clear();
		}
		
		return departureAlarms;
	}

	public List<DepartureAlarm> getAllByCode(int argCode) {
		List<DepartureAlarm> departureAlarms = new ArrayList<DepartureAlarm>();

		try
		{
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, DepartureCodeField + " = ?", new String[]{String.valueOf(argCode)}, null, null, DateField);
			while (cursor.moveToNext())
			{
				DepartureAlarm departureAlarm = fromCursor(cursor);
				if (departureAlarm != null)
				{
					departureAlarms.add(departureAlarm);
				}
			}
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			departureAlarms.clear();
		}

		return departureAlarms;
	}

	@Override
	public boolean update(DepartureAlarm ArgDepartureAlarm) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
