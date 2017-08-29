/**
 * 
 */
package com.mytpg.engines.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;

import com.mytpg.engines.data.interfaces.IPhysicalStopDAO;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.StopTools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class PhysicalStopDAO extends DAO<PhysicalStop>  implements IPhysicalStopDAO {
	public static final String IdField = "pkPhysicalStop";
	public static final String NameField = "name";
	public static final String LatitudeField = "latitude";
	public static final String LongitudeField = "longitude";

	public static final String FkStopField = "fkStop";
	
	public static final String TableName = "tblphysicalstops";
    public static final String TABLE_SCRIPT = "CREATE TABLE " + TableName +
            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NameField + " TEXT NOT NULL collate nocase, " +
            LatitudeField + " DOUBLE NOT NULL, " +
            LongitudeField + " DOUBLE NOT NULL, " +
            FkStopField + " INTEGER NOT NULL);";
	
	public PhysicalStopDAO(DatabaseHelper ArgDBHelper) {
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
	public boolean create(PhysicalStop ArgPhysicalStop) {
		try
		{


			ContentValues cv = new ContentValues();
			cv.put(NameField, ArgPhysicalStop.getName());
			cv.put(LatitudeField, ArgPhysicalStop.getLocation().getLatitude());
			cv.put(LongitudeField, ArgPhysicalStop.getLocation().getLongitude());
			cv.put(FkStopField, ArgPhysicalStop.getStopId());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgPhysicalStop.setId(id);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean create(List<PhysicalStop> ArgPhysicalStops) {
		try
		{

			String sql = "INSERT INTO " + TableName + " (" + NameField + ", " +
                                                             LatitudeField + ", " +
		                                                     LongitudeField + ", " +
		                                                     FkStopField + ") " +
		                                                     "VALUES (?,?,?,?)";
			
			List<Line> connections = new ArrayList<Line>();
			for (PhysicalStop currentPhysicalStop : ArgPhysicalStops)
			{
				for (Line currentConnection : currentPhysicalStop.getConnections())
				{
					boolean found = false;
					
					int i = 0;
					while (i < connections.size())
					{
						if (currentConnection.getName().equalsIgnoreCase(connections.get(i).getName()) &&
						    currentConnection.getArrivalStop().getName().equalsIgnoreCase(connections.get(i).getArrivalStop().getName()))
					    {
							found = true;
							break;
					    }
						i++;
					}
					
					if (!found)
					{
						connections.add(currentConnection);
					}
				}
			}
			
			SQLiteDatabase db = m_dbHelper.getWritableDatabase();
			
			db.beginTransaction();

            boolean success = true;

			SQLiteStatement stmt = db.compileStatement(sql);

			int i = 0;
			while (i < ArgPhysicalStops.size())
			{
				PhysicalStop physicalStop = ArgPhysicalStops.get(i);

				stmt.bindString(1, physicalStop.getName());
				stmt.bindDouble(2, physicalStop.getLocation().getLatitude());
				stmt.bindDouble(3, physicalStop.getLocation().getLongitude());
                stmt.bindLong(4,physicalStop.getStopId());
				
				//stmt.execute();
				final long Id = stmt.executeInsert();
				physicalStop.setId(Id);

                List<Connection> realConnections = new ArrayList<Connection>();
                for (int j = 0; j < physicalStop.getConnections().size();j++){
                    Connection conn = new Connection();
                    conn.getPhysicalStop().setId(physicalStop.getId());
                    conn.setLine(physicalStop.getConnections().get(j));

                    realConnections.add(conn);
                }

                ConnectionDAO connDAO = new ConnectionDAO(m_dbHelper);
                if (!connDAO.create(realConnections))
                {
                    success = false;
                    break;
                }

				stmt.clearBindings();
				
				i++;
			}

            if (success) {
                db.setTransactionSuccessful();
            }


            db.endTransaction();
			
			
			
			/*ContentValues cv = new ContentValues();
			cv.put(NameField, ArgPhysicalStop.getName());
			cv.put(LatitudeField, ArgPhysicalStop.getLocation().getLatitude());
			cv.put(LongitudeField, ArgPhysicalStop.getLocation().getLongitude());
			cv.put(IsFavoriteField, ArgPhysicalStop.isFavorite());
			cv.put(FkMnemoField, ArgPhysicalStop.getMnemo().getId());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgPhysicalStop.setId(id);*/
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
	public boolean delete(PhysicalStop ArgPhysicalStop) {
		boolean isOk = false;
		if (ArgPhysicalStop == null || ArgPhysicalStop.getId() < 1)
		{
			return isOk;
		}
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgPhysicalStop.getId())});
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
	public PhysicalStop find(long ArgId, boolean ArgIsComplete) {
		PhysicalStop stop = null;
		
		try
		{
			String[] finalSelect = new String[]{IdField,NameField,LatitudeField,LongitudeField,FkStopField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IdField + " = ? ",new String[]{String.valueOf(ArgId)},null,null,NameField + " LIMIT 1");
			if (cursor.moveToFirst())
			{
				stop = fromCursor(cursor,ArgIsComplete);
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			stop = null;
		}
		
		return stop;
	}

	@Override
	public List<PhysicalStop> findByStopId(long ArgStopId) {
		return findByStopId(ArgStopId,true);
	}

    @Override
    public List<PhysicalStop> findByStopId(long ArgStopId, boolean ArgIsComplete) {
        List<PhysicalStop> physicalStops = new ArrayList<PhysicalStop>();

        try
        {
            String[] finalSelect = new String[]{IdField,NameField,LatitudeField,LongitudeField,FkStopField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, FkStopField + " = ? ",new String[]{String.valueOf(ArgStopId)},null,null,LatitudeField);
            while (cursor.moveToNext())
            {
                PhysicalStop physicalStop = fromCursor(cursor, ArgIsComplete);
                if (physicalStop != null)
                {
                    physicalStops.add(physicalStop);
                }
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            physicalStops.clear();
        }

        return physicalStops;
    }

    @Override
    public List<PhysicalStop> findByStopName(String ArgName) {
        List<PhysicalStop> physicalStops = new ArrayList<PhysicalStop>();
        if (ArgName.isEmpty())
        {
            return physicalStops;
        }
        try
        {
            StopDAO stopDAO = new StopDAO(m_dbHelper);
            List<Stop> stops = stopDAO.getAllByName(ArgName,true);
            if (stops.isEmpty())
            {
                return physicalStops;
            }


            for (Stop stop : stops)
            {
                physicalStops.addAll(stop.getPhysicalStops());
            }

        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            physicalStops.clear();
        }

        return physicalStops;
    }

    @Override
	public PhysicalStop findByName(String ArgName) {
		PhysicalStop stop = null;
		
		try
		{

			String[] finalSelect = new String[]{IdField,NameField,LatitudeField,LongitudeField,FkStopField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, NameField + " = ? ",new String[]{ArgName},null,null,NameField + " LIMIT 1");
			if (cursor.moveToFirst())
			{
				stop = fromCursor(cursor);
			}
			
			cursor.close();

        }
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			stop = null;
		}
		
		return stop;
	}

	@Override
	protected PhysicalStop fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
		if (ArgCursor.getColumnCount() == 0)
		{
			return null;
		}
		
		final int idIndex = ArgCursor.getColumnIndex(IdField);
		final int nameIndex = ArgCursor.getColumnIndex(NameField);
		final int latitudeIndex = ArgCursor.getColumnIndex(LatitudeField);
		final int longitudeIndex = ArgCursor.getColumnIndex(LongitudeField);
		final int fkStopIndex = ArgCursor.getColumnIndex(FkStopField);
		
		PhysicalStop physicalStop = new PhysicalStop();
		
		if (idIndex != -1)
		{
			physicalStop.setId(ArgCursor.getLong(idIndex));
		}
		
		if (nameIndex != -1)
		{
			String name = ArgCursor.getString(nameIndex);
			physicalStop.setName(name);
		}
		
		if (latitudeIndex != -1)
		{
			physicalStop.getLocation().setLatitude(ArgCursor.getDouble(latitudeIndex));
		}
		
		if (longitudeIndex != -1)
		{
			physicalStop.getLocation().setLongitude(ArgCursor.getDouble(longitudeIndex));
		}

		if (fkStopIndex != -1)
		{
            physicalStop.setStopId(ArgCursor.getLong(fkStopIndex));
		}
		
		if (ArgIsComplete)
		{
            ConnectionDAO connDAO = new ConnectionDAO(m_dbHelper);
			List<Connection> connections = connDAO.getConnectionsByPhysicalStop(physicalStop.getId());
			
			for (Connection conn : connections)
			{
				physicalStop.getConnections().add(conn.getLine());
			}
		}
		
		return physicalStop;
	}

	@Override
	public List<PhysicalStop> getAll() {
		return getAll(true);
	}

	/* (non-Javadoc)
	 * @see com.otpg.engines.data.interfaces.IPhysicalStopDAO#getAll(boolean)
	 */
	@Override
	public List<PhysicalStop> getAll(boolean ArgIsComplete) {
		List<PhysicalStop> physicalStops = new ArrayList<PhysicalStop>();
		
		try
		{
			String[] selectNormal = new String[]{IdField,NameField,LatitudeField,LongitudeField,FkStopField};

			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, selectNormal, null,null,null,null,NameField);
			while (cursor.moveToNext())
			{
				PhysicalStop physicalStop = fromCursor(cursor,ArgIsComplete);
				if (physicalStop != null)
				{
					physicalStops.add(physicalStop);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			physicalStops.clear();
		}
		
		return physicalStops;
	}

    @Override
    public List<PhysicalStop> getAllByStopId(final long ArgStopId) {
        List<PhysicalStop> physicalStops = new ArrayList<PhysicalStop>();

        try
        {
            String[] selectNormal = new String[]{IdField,NameField,LatitudeField,LongitudeField,FkStopField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, selectNormal, FkStopField + " = ?",new String[]{String.valueOf(ArgStopId)},null,null,NameField);
            while (cursor.moveToNext())
            {
                PhysicalStop physicalStop = fromCursor(cursor,false);
                if (physicalStop != null)
                {
                    physicalStops.add(physicalStop);
                }
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            physicalStops.clear();
        }

        return physicalStops;
    }

    @Override
	public List<PhysicalStop> getByIds(List<Long> ArgPhysicalStopIds) {
		List<PhysicalStop> physicalStops = new ArrayList<PhysicalStop>();
		
		try
		{
			String inClause = "";
			final int NumberElements = ArgPhysicalStopIds.size();
			int i = 0;
			while (i < NumberElements)
			{
				inClause += String.valueOf( ArgPhysicalStopIds.get(i).longValue());
				
				i++;
				if (i < NumberElements)
				{
					inClause += ",";
				}
			}
			
			String filter = IdField + " IN (" + inClause + ")";
			String[] selectPhysical = new String[]{IdField,NameField,LatitudeField,LongitudeField,FkStopField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, selectPhysical, filter, null ,null,null,NameField);
			while (cursor.moveToNext())
			{
				PhysicalStop physicalStop = fromCursor(cursor);
				if (physicalStop != null)
				{
					physicalStops.add(physicalStop);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			physicalStops.clear();
		}
		
		return physicalStops;
	}

	@Override
	public List<PhysicalStop> getByLocation(final Location ArgLoc, final int ArgNumber) {
		List<PhysicalStop> physicalStops = new ArrayList<PhysicalStop>();
		
		if (ArgNumber < 1 || ArgLoc == null)
		{
			return physicalStops;
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
				PhysicalStop physicalStop = fromCursor(cursor,false);
				if (physicalStop != null)
				{
					physicalStops.add(physicalStop);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			physicalStops.clear();
		}

		StopTools.sortByDistance(physicalStops, ArgLoc);


        List<Long> stopIds = new ArrayList<Long>();

        int j = 0;
        while (j < physicalStops.size())
        {
            PhysicalStop ps = physicalStops.get(j);
            if (!stopIds.contains(ps.getStopId()))
            {
                stopIds.add(ps.getStopId());

            }
            else
            {
                physicalStops.remove(j);
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

		int i = physicalStops.size() -1;
		while (i > ArgNumber+5)
		{
			physicalStops.remove(i);
			i--;
		}
		
		List<Long> physicalStopIds = new ArrayList<Long>();
		for (PhysicalStop physicalStop : physicalStops)
		{
			physicalStopIds.add(physicalStop.getId());
		}
		
		//physicalStops = getByIds(physicalStopIds);

		StopTools.sortByDistance(physicalStops, ArgLoc);
		
		return physicalStops;
	}

	@Override
	public boolean update(PhysicalStop ArgPhysicalStop) {
		if (ArgPhysicalStop == null || ArgPhysicalStop.getId() < 1)
		{
			return false;
		}
		
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(NameField, ArgPhysicalStop.getName());
			cv.put(LatitudeField, ArgPhysicalStop.getLocation().getLatitude());
			cv.put(LongitudeField, ArgPhysicalStop.getLocation().getLongitude());
			cv.put(FkStopField, ArgPhysicalStop.getStopId());
			
			int numberRowsUpdated = m_dbHelper.getSqlDB().update(TableName, cv, IdField + " = ?", new String[]{String.valueOf(ArgPhysicalStop.getId())});
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

}
