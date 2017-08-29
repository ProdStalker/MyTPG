/**
 * 
 */
package com.mytpg.engines.data.dao.bustedapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;

import com.mytpg.engines.data.dao.DAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.entities.bustedapp.BustedStop;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.tools.StopTools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class BustedStopDAO extends DAO<BustedStop> {
	public static final String BackgroundColorField = "bgCol";
	public static final String DestinationField = "dest";
	public static final String IdField = "_id";
	public static final String LatitudeField = "lat";
	public static final String LineField = "line";
	public static final String LongitudeField = "lng";
	public static final String NameField = "name";
	public static final String StopIdField = "stopId";
    public static final String TextColorField = "txtCol";

	public static final String FkMnemoField = "fkMnemo";

	public static final String TableName = "tblbustedstops";
    public static final String TABLE_SCRIPT = "CREATE TABLE " + TableName +
            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			StopIdField + " INTEGER NOT NULL DEFAULT -1, " +
            NameField + " TEXT NOT NULL collate nocase, " +
			LineField + " TEXT NOT NULL collate nocase, " +
			DestinationField + " TEXT NOT NULL collate nocase, " +
			LatitudeField + " DOUBLE NOT NULL, " +
			LongitudeField + " DOUBLE NOT NULL, " +
			TextColorField + " TEXT NOT NULL collate nocase, " +
            BackgroundColorField + " TEXT NOT NULL collate nocase);";

	public BustedStopDAO(DatabaseHelper ArgDBHelper) {
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
	public boolean create(BustedStop ArgBustedStop) {
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(StopIdField, ArgBustedStop.getStopId());
			cv.put(NameField, ArgBustedStop.getName());
			cv.put(LineField, ArgBustedStop.getLine());
			cv.put(DestinationField, ArgBustedStop.getDestination());
			cv.put(LatitudeField, ArgBustedStop.getLocation().getLatitude());
			cv.put(LongitudeField, ArgBustedStop.getLocation().getLongitude());
			cv.put(TextColorField, ArgBustedStop.getTextColor());
			cv.put(BackgroundColorField, ArgBustedStop.getBackgroundColor());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgBustedStop.setId(id);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean create(List<BustedStop> ArgBustedStops) {
		try
		{
            boolean success = true;
			
			String sql = "INSERT INTO " + TableName + " (" + StopIdField + ", " +
                    										 NameField + ", " +
		                                                     LineField + ", " +
					                                         DestinationField + ", " +
		                                                     LatitudeField + ", " +
                                                             LongitudeField + ", " +
															 TextColorField + ", " +
		                                                     BackgroundColorField + ") " +
		                                                     "VALUES (?,?,?,?,?,?,?,?)";
			
			SQLiteDatabase db = m_dbHelper.getWritableDatabase();
			
			db.beginTransaction();
			 
			SQLiteStatement stmt = db.compileStatement(sql);

			int i = 0;
			while (i < ArgBustedStops.size())
			{
				BustedStop bustedStop = ArgBustedStops.get(i);

				stmt.bindLong(1, bustedStop.getStopId());
				stmt.bindString(2, bustedStop.getName());
				stmt.bindString(3, bustedStop.getLine());
				stmt.bindString(4, bustedStop.getDestination());
				stmt.bindDouble(5, bustedStop.getLocation().getLatitude());
				stmt.bindDouble(6, bustedStop.getLocation().getLongitude());
				stmt.bindString(7, bustedStop.getTextColor());
				stmt.bindString(8, bustedStop.getBackgroundColor());
				
				//stmt.execute();
				final long Id = stmt.executeInsert();
				bustedStop.setId(Id);

				stmt.clearBindings();
				
				i++;
			}

            if (success){
                db.setTransactionSuccessful();

            }

            db.endTransaction();
			
			
			/*ContentValues cv = new ContentValues();
			cv.put(NameField, ArgStop.getName());
			cv.put(LatitudeField, ArgStop.getLocation().getLatitude());
			cv.put(LongitudeField, ArgStop.getLocation().getLongitude());
			cv.put(IsFavoriteField, ArgStop.isFavorite());
			cv.put(FkMnemoField, ArgStop.getMnemo().getId());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgStop.setId(id);*/
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
	public boolean delete(BustedStop ArgBustedStop) {
		boolean isOk = false;
		if (ArgBustedStop == null || ArgBustedStop.getId() < 1)
		{
			return isOk;
		}
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgBustedStop.getId())});
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
	public BustedStop find(long ArgId) {
		return find(ArgId,true);
	}

	@Override
	public BustedStop find(long ArgId, boolean ArgIsComplete) {
		BustedStop bustedStop = null;
		
		try
		{
			String[] finalSelect = new String[]{IdField,StopIdField, NameField, LineField, DestinationField, LatitudeField, LongitudeField, TextColorField, BackgroundColorField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IdField + " = ? ",new String[]{String.valueOf(ArgId)},null,null,NameField + " LIMIT 1");
			if (cursor.moveToFirst())
			{
				bustedStop = fromCursor(cursor,ArgIsComplete);
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			bustedStop = null;
		}
		
		return bustedStop;
	}

	@Override
	public BustedStop findByName(String ArgName) {
		return findByName(ArgName, true);
	}

	public BustedStop findByName(String ArgName, boolean ArgIsComplete) {
		BustedStop bustedStop = null;

        if (ArgName.isEmpty())
        {
            return bustedStop;
        }
		try
		{
			String[] finalSelect = new String[]{IdField,StopIdField, NameField, LineField, DestinationField, LatitudeField, LongitudeField, TextColorField, BackgroundColorField};

			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, NameField + " = ?",new String[]{ArgName},null,null,NameField + " LIMIT 1");

			if (cursor.moveToFirst())
			{
				bustedStop = fromCursor(cursor, ArgIsComplete);
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			bustedStop = null;
		}
		
		return bustedStop;
	}

	@Override
	protected BustedStop fromCursor(Cursor ArgCursor) {
		return fromCursor(ArgCursor,true);
	}

	@Override
	protected BustedStop fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
		return fromCursor(ArgCursor, ArgIsComplete, true);
	}


    public BustedStop fromCursor(Cursor ArgCursor, boolean ArgIsComplete, boolean ArgWithConnections) {
        if (ArgCursor.getColumnCount() == 0)
        {
            return null;
        }

        final int idIndex = ArgCursor.getColumnIndex(IdField);
		int stopIdIndex = ArgCursor.getColumnIndex(StopIdField);
        int nameIndex = ArgCursor.getColumnIndex(NameField);
		int lineIndex = ArgCursor.getColumnIndex(LineField);
		int destinationIndex = ArgCursor.getColumnIndex(DestinationField);
		int longitudeIndex = ArgCursor.getColumnIndex(LongitudeField);
		int latitudeIndex = ArgCursor.getColumnIndex(LatitudeField);
		int textColorIndex = ArgCursor.getColumnIndex(TextColorField);
		int backgroundColorIndex = ArgCursor.getColumnIndex(BackgroundColorField);

        BustedStop bustedStop = new BustedStop();

        if (idIndex != -1)
        {
            bustedStop.setId(ArgCursor.getLong(idIndex));
        }

        if (stopIdIndex !=  -1)
		{
			bustedStop.setStopId(ArgCursor.getLong(idIndex));
		}

        if (nameIndex != -1)
        {
            bustedStop.setName(ArgCursor.getString(nameIndex));
        }

		if (lineIndex != -1)
		{
			bustedStop.setLine(ArgCursor.getString(lineIndex));
		}

		if (destinationIndex != -1)
		{
			bustedStop.setDestination(ArgCursor.getString(destinationIndex));
		}

		if (latitudeIndex != -1)
		{
			bustedStop.getLocation().setLatitude(ArgCursor.getDouble(latitudeIndex));
		}

		if (longitudeIndex != -1)
		{
			bustedStop.getLocation().setLongitude(ArgCursor.getDouble(longitudeIndex));
		}

		if (textColorIndex != -1)
		{
			bustedStop.setTextColor(ArgCursor.getString(textColorIndex));
		}

		if (backgroundColorIndex != -1)
		{
			bustedStop.setBackgroundColor(ArgCursor.getString(backgroundColorIndex));
		}

        return bustedStop;
    }

	@Override
	public List<BustedStop> getAll() {
		return getAll(true,true);
	}
	

	public List<BustedStop> getAll(boolean ArgIsPhysical) {
		return getAll(ArgIsPhysical,true);
	}

	/* (non-Javadoc)
	 * @see com.otpg.engines.data.interfaces.IStopDAO#getAll(boolean)
	 */

	public List<BustedStop> getAll(boolean ArgIsPhysical, boolean ArgIsComplete) {
		return getAll(ArgIsPhysical, ArgIsComplete, true);
	}


    public List<BustedStop> getAll(boolean ArgIsPhysical, boolean ArgIsComplete, boolean ArgWithConnections) {
        List<BustedStop> bustedStops = new ArrayList<>();

        try
        {
			String[] finalSelect = new String[]{IdField,StopIdField, NameField, LineField, DestinationField, LatitudeField, LongitudeField, TextColorField, BackgroundColorField};

			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, null,null,null,null,NameField);

            while (cursor.moveToNext())
            {
                BustedStop bustedStop = fromCursor(cursor,ArgIsComplete, ArgWithConnections); // ArgIsComplete
                if (bustedStop != null)
                {
					bustedStops.add(bustedStop);

                }
            }

            cursor.close();

        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            bustedStops.clear();
        }

        return bustedStops;
    }

    public List<BustedStop> getAllByName(String ArgName, boolean ArgIsComplete) {
        List<BustedStop> bustedStops = new ArrayList<>();

        try
        {
			String[] finalSelect = new String[]{IdField,StopIdField, NameField, LineField, DestinationField, LatitudeField, LongitudeField, TextColorField, BackgroundColorField};

			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, NameField + " = ?",new String[]{ArgName},null,null,IdField);
            while (cursor.moveToNext())
            {
                BustedStop bustedStop = fromCursor(cursor,ArgIsComplete); // ArgIsComplete
                if (bustedStop != null)
                {
					bustedStops.add(bustedStop);
                }
            }

            cursor.close();

        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
			bustedStops.clear();
        }

        return bustedStops;
    }


	public List<BustedStop> getByIds(List<Long> ArgStopIds) {
        return getByIds(ArgStopIds,true);

	}

    public List<BustedStop> getByIds(List<Long> ArgStopIds, boolean ArgIsComplete) {
        List<BustedStop> bustedStops = new ArrayList<>();

        try
        {
            String inClause = "";
            final int NumberElements = ArgStopIds.size();
            int i = 0;
            while (i < NumberElements)
            {
                inClause += String.valueOf( ArgStopIds.get(i).longValue());

                i++;
                if (i < NumberElements)
                {
                    inClause += ",";
                }
            }

            String filter = IdField + " IN (" + inClause + ")";
            String[] finalSelect = new String[]{IdField,StopIdField, NameField, LineField, DestinationField, LatitudeField, LongitudeField, TextColorField, BackgroundColorField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, filter, null ,null,null,NameField);
            while (cursor.moveToNext())
            {
                BustedStop bustedStop = fromCursor(cursor,ArgIsComplete);
                if (bustedStop != null)
                {
					bustedStops.add(bustedStop);
                }
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
			bustedStops.clear();
        }

        return bustedStops;

    }

	public List<BustedStop> getByLocation(final Location ArgLoc, final int ArgNumber) {
		List<BustedStop> bustedStops = new ArrayList<>();

		if (ArgNumber < 1 || ArgLoc == null)
		{
			return bustedStops;
		}


		try
		{
			//String[] selectNormal = new String[]{IdField,LatitudeField,LongitudeField,FkStopField};

			final String StopSelect = StopDAO.TableName + "."+StopDAO.IdField + "," + StopDAO.TableName + "." + StopDAO.IsVisibleField;

			String select = "SELECT " + TableName + ".* FROM " + TableName;
			//Cursor cursor = m_dbHelper.getSqlDB().query(TableName, selectNormal, null,null,null,null,IdField);
			Cursor cursor = m_dbHelper.getSqlDB().rawQuery(select,null);
			while (cursor.moveToNext())
			{
				BustedStop bustedStop = fromCursor(cursor,false);
				if (bustedStop != null)
				{
					bustedStops.add(bustedStop);
				}
			}

			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			bustedStops.clear();
		}

		StopTools.sortByDistance(bustedStops, ArgLoc);


		List<Long> stopIds = new ArrayList<>();

		int j = 0;
		while (j < bustedStops.size())
		{
			BustedStop ps = bustedStops.get(j);
			if (!stopIds.contains(ps.getStopId()))
			{
				stopIds.add(ps.getStopId());

			}
			else
			{
				bustedStops.remove(j);
				j--;
			}
			j++;
		}
      /*  for (int j = physicalStops.size() -1; j >= 0; j--)
        {
            if (!stopIds.contains(physicalStops.get(j).getStopId()))
            {
                stopIds.add(physicalStops.get(j).getStopId());
            }
            else
            {
                physicalStops.remove(j);
            }
        }*/

		int i = bustedStops.size() -1;
		while (i > ArgNumber+5)
		{
			bustedStops.remove(i);
			i--;
		}

		List<Long> physicalStopIds = new ArrayList<Long>();
		for (BustedStop bustedStop : bustedStops)
		{
			physicalStopIds.add(bustedStop.getId());
		}

		//physicalStops = getByIds(physicalStopIds);

		StopTools.sortByDistance(bustedStops, ArgLoc);

		return bustedStops;
	}

	@Override
	public boolean update(BustedStop ArgBustedStop) {
		if (ArgBustedStop == null || ArgBustedStop.getId() < 1)
		{
			return false;
		}
		
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(StopIdField, ArgBustedStop.getStopId());
			cv.put(NameField, ArgBustedStop.getName());
			cv.put(LineField, ArgBustedStop.getLine());
			cv.put(DestinationField, ArgBustedStop.getDestination());
			cv.put(LatitudeField, ArgBustedStop.getLocation().getLatitude());
			cv.put(LongitudeField, ArgBustedStop.getLocation().getLongitude());
			cv.put(TextColorField, ArgBustedStop.getTextColor());
			cv.put(BackgroundColorField, ArgBustedStop.getBackgroundColor());
			
			int numberRowsUpdated = m_dbHelper.getSqlDB().update(TableName, cv, IdField + " = ?", new String[]{String.valueOf(ArgBustedStop.getId())});
			if (numberRowsUpdated > 0)
			{
				return true;
			}
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
		}
		
		return false;
	}

	public String[] getAllAutoSuggestedNames() {
		List<String> bustedStopNames = new ArrayList<>();

		try
		{
			String[] selectNormal = new String[]{NameField};

			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, selectNormal, null,new String[]{},null,null,NameField);
			while (cursor.moveToNext())
			{
				bustedStopNames.add(cursor.getString(cursor.getColumnIndex(NameField)));
			}

			cursor.close();

		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			bustedStopNames.clear();
		}

		String[] bustedStopNamesArray = new String[bustedStopNames.size()];
		bustedStopNames.toArray(bustedStopNamesArray);

		return bustedStopNamesArray;
	}
}
