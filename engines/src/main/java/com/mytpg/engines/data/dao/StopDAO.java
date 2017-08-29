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

import com.mytpg.engines.data.interfaces.IStopDAO;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Mnemo;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.StopTools;
import com.mytpg.engines.tools.TextTools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class StopDAO extends DAO<Stop> implements IStopDAO {
	public static final String IdField = "pkStop";
	public static final String NameField = "name";
	public static final String NameWhenPSField = "sname";
	public static final String CodeField = "code";
	public static final String IsFavoriteField = "isFavorite";
	public static final String IsFavoriteDetailledField = "isFavoriteDetailled";
	public static final String IsVisibleField = "isVisible";
    public static final String FavoriteNumberField = "favoriteNumber";
	public static final String CFFField = "cff";
	
	public static final String FkMnemoField = "fkMnemo";
	
	public static final String TableName = "tblstops";
    public static final String TABLE_SCRIPT = "CREATE TABLE " + TableName +
            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NameField + " TEXT NOT NULL collate nocase, " +
            CodeField + " TEXT NOT NULL collate nocase, " +
			IsFavoriteDetailledField + " INTEGER NOT NULL DEFAULT 0, " +
            IsFavoriteField + " INTEGER NOT NULL DEFAULT 0, " +
            IsVisibleField + " INTEGER NOT NULL DEFAULT 1, " +
            FavoriteNumberField + " INTEGER NOT NULL DEFAULT 0," +
			CFFField + " TEXT NOT NULL collate nocase DEFAULT \"\"," +
            FkMnemoField + " INTEGER NOT NULL);";

	public StopDAO(DatabaseHelper ArgDBHelper) {
		super(ArgDBHelper);
		// TODO Auto-generated constructor stub
	}



	@Override
	public boolean addFavorite(Stop ArgStop, boolean argIsDetailled) {
		Stop stop = find(ArgStop.getMnemo().getName());
		if (stop != null)
		{
			if (!stop.isFavorite()) {
				long favoriteNumber = 0;
				Stop lastFavorite = getLastFavorite();
				if (lastFavorite != null) {
					favoriteNumber = lastFavorite.getFavoriteNumber();
				}
				favoriteNumber++;
				stop.setFavoriteNumber(favoriteNumber);
				stop.setFavorite(true);
			}
			stop.setFavoriteDetailled(argIsDetailled);
			return update(stop);
		}
		return false;
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
    public long countFavorites() {
        long number = 0;

        try
        {
            Cursor cursor = m_dbHelper.getSqlDB().rawQuery("SELECT COUNT(" + IdField + ") AS number FROM " + TableName + " WHERE " + IsFavoriteField + " = 1", null);
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
	public boolean create(Stop ArgStop) {
		try
		{
			MnemoDAO mnemoDAO = new MnemoDAO(getDB());
			Mnemo mnemo = mnemoDAO.find(ArgStop.getMnemo().getId());
			if (mnemo == null)
			{
				mnemo = mnemoDAO.find(ArgStop.getMnemo().getName());
			}
			
			if (mnemo == null)
			{
				if (!mnemoDAO.create(ArgStop.getMnemo()))
				{
					return false;
				}
			}
			else
			{
				ArgStop.setMnemo(mnemo);
			}
			
			if (ArgStop.getCode().isEmpty())
			{
				ArgStop.setCode(StopTools.getCode(ArgStop.getName()));
			}
			
			ContentValues cv = new ContentValues();
			cv.put(NameField, ArgStop.getName());
			cv.put(CodeField, ArgStop.getCode());
			cv.put(IsFavoriteField, ArgStop.isFavorite());
			cv.put(IsFavoriteDetailledField, ArgStop.isFavoriteDetailled());
			cv.put(IsVisibleField, ArgStop.isVisible());
            cv.put(FavoriteNumberField,ArgStop.getFavoriteNumber());
			cv.put(CFFField, ArgStop.getCFF());
			cv.put(FkMnemoField, ArgStop.getMnemo().getId());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgStop.setId(id);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean create(List<Stop> ArgStops) {
		try
		{
            boolean success = true;
			MnemoDAO mnemoDAO = new MnemoDAO(getDB());
			
			String sql = "INSERT INTO " + TableName + " (" + NameField + ", " +
                    										 CodeField + ", " +
		                                                     IsFavoriteField + ", " +
					                                         IsFavoriteDetailledField + ", " +
		                                                     IsVisibleField + ", " +
                                                             FavoriteNumberField + ", " +
															 CFFField + ", " +
		                                                     FkMnemoField + ") " +
		                                                     "VALUES (?,?,?,?,?,?,?,?)";
			
			List<Line> connections = new ArrayList<Line>();
			for (Stop currentStop : ArgStops)
			{
                for (PhysicalStop physicalStop : currentStop.getPhysicalStops()) {
                    for (Line currentConnection : physicalStop.getConnections()) {
                        boolean found = false;

                        int i = 0;
                        while (i < connections.size()) {
                            if (currentConnection.getName().equalsIgnoreCase(connections.get(i).getName()) &&
                                    currentConnection.getArrivalStop().getName().equalsIgnoreCase(connections.get(i).getArrivalStop().getName())) {
                                found = true;
                                break;
                            }
                            i++;
                        }

                        if (!found) {
                            connections.add(currentConnection);
                        }
                    }
                }
			}
			
			SQLiteDatabase db = m_dbHelper.getWritableDatabase();
			
			db.beginTransaction();
			 
			SQLiteStatement stmt = db.compileStatement(sql);

			int i = 0;
			while (i < ArgStops.size())
			{
				Stop stop = ArgStops.get(i);
				
				for (Line conn : connections)
				{
					if (conn.getArrivalStop().getName().equalsIgnoreCase(stop.getName()))
					{
						stop.setCode(conn.getArrivalStop().getMnemo().getName());
						break;
					}
				}
				
				if (stop.getCode().isEmpty())
				{
					if (stop.getCode().isEmpty())
					{
						stop.setCode(StopTools.getCode(stop.getName()));
					}
				}
				
				Mnemo mnemo = mnemoDAO.find(stop.getMnemo().getId());
				if (mnemo == null)
				{
					mnemo = mnemoDAO.find(stop.getMnemo().getName());
				}
				
				if (mnemo == null)
				{
					if (!mnemoDAO.create(stop.getMnemo()))
					{
						return false;
					}
				}
				else
				{
					stop.setMnemo(mnemo);
				}
				
				stmt.bindString(1, stop.getName());
				stmt.bindString(2, stop.getCode());
				
				long isFavoriteNumber = 0;
				if (stop.isFavorite())
				{
					isFavoriteNumber = 1;
				}
				stmt.bindLong(3, isFavoriteNumber);
				long isFavoriteDetailledNumber = 0;
				if (stop.isFavoriteDetailled())
				{
					isFavoriteDetailledNumber = 1;
				}
				stmt.bindLong(4, isFavoriteDetailledNumber);
				long isVisibleNumber = 0;
				if (stop.isVisible())
				{
					isVisibleNumber = 1;
				}
				stmt.bindLong(5, isVisibleNumber);
                stmt.bindLong(6, stop.getFavoriteNumber());
				stmt.bindString(7, stop.getCFF());
				stmt.bindLong(8, stop.getMnemo().getId());
				
				//stmt.execute();
				final long Id = stmt.executeInsert();
				stop.setId(Id);

                for (int j = 0; j < stop.getPhysicalStops().size(); j++){
                    stop.getPhysicalStops().get(j).setStopId(stop.getId());
                }

                PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(m_dbHelper);
                if (!physicalStopDAO.create(stop.getPhysicalStops())){
                    success = false;
                }
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
	public boolean delete(Stop ArgStop) {
		boolean isOk = false;
		if (ArgStop == null || ArgStop.getId() < 1)
		{
			return isOk;
		}
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgStop.getId())});
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
	public Stop find(long ArgId) {
		return find(ArgId,true);
	}

	@Override
	public Stop find(long ArgId, boolean ArgIsComplete) {
		Stop stop = null;
		
		try
		{
			String[] finalSelect = new String[]{IdField,NameField,CodeField,IsFavoriteField,IsFavoriteDetailledField,FavoriteNumberField,CFFField,FkMnemoField};
			
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
	public Stop find(String ArgMnemo) {
		return find(ArgMnemo, true);
	}


	public Stop find(String ArgMnemo, boolean ArgIsComplete) {
		Stop stop = null;
        if (ArgMnemo.isEmpty())
        {
            return stop;
        }
		try
		{
			Mnemo mnemo = new MnemoDAO(m_dbHelper).find(ArgMnemo);
			if (mnemo == null)
			{
				return stop;
			}
			
			String[] finalSelect = new String[]{IdField,NameField,CodeField,IsFavoriteField,IsFavoriteDetailledField,FavoriteNumberField,CFFField,FkMnemoField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, FkMnemoField + " = ? ",new String[]{String.valueOf(mnemo.getId())},null,null,NameField + " LIMIT 1");
			if (cursor.moveToFirst())
			{
				stop = fromCursor(cursor, ArgIsComplete);
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



	public Stop findByCFFName(String argCFFName) {
		return findByCFFName(argCFFName, true);
	}

	public Stop findByCFFName(String argCffName, boolean argIsComplete)
	{
		Stop stop = null;
		if (argCffName.isEmpty())
		{
			return stop;
		}
		try
		{
			String[] finalSelect = new String[]{IdField,NameField,CodeField,IsFavoriteField,IsFavoriteDetailledField,FavoriteNumberField,CFFField,FkMnemoField};

			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, CFFField + " = ? ",new String[]{argCffName},null,null,CodeField + " LIMIT 1");
			if (cursor.moveToFirst())
			{
				stop = fromCursor(cursor, argIsComplete);
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
	public Stop findByCode(String ArgCode) {
		return findByCode(ArgCode, true);
	}

	public Stop findByCode(String ArgCode, boolean ArgIsComplete) {
		Stop stop = null;
		if (ArgCode.isEmpty())
        {
            return stop;
        }
		try
		{
			String[] finalSelect = new String[]{IdField,NameField,CodeField,IsFavoriteField,IsFavoriteDetailledField,FavoriteNumberField,CFFField,FkMnemoField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, CodeField + " = ? ",new String[]{ArgCode},null,null,CodeField + " LIMIT 1");
			if (cursor.moveToFirst())
			{
				stop = fromCursor(cursor, ArgIsComplete);
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
	public Stop findByName(String ArgName) {
		return findByName(ArgName, true);
	}

	public Stop findByName(String ArgName, boolean ArgIsComplete) {
		Stop stop = null;

        if (ArgName.isEmpty())
        {
            return stop;
        }
		try
		{
			String[] finalSelect = new String[]{IdField,NameField,CodeField,IsFavoriteField,IsFavoriteDetailledField,FavoriteNumberField,FkMnemoField,CFFField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, NameField + " = ? OR " + CFFField + " = ? " ,new String[]{ArgName, ArgName},null,null,NameField + " LIMIT 1");

			if (cursor.moveToFirst())
			{
				stop = fromCursor(cursor, ArgIsComplete);
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
	protected Stop fromCursor(Cursor ArgCursor) {
		return fromCursor(ArgCursor,true);
	}

	@Override
	protected Stop fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
		return fromCursor(ArgCursor, ArgIsComplete, true);
	}

    @Override
    public Stop fromCursor(Cursor ArgCursor, boolean ArgIsComplete, boolean ArgWithConnections) {
        if (ArgCursor.getColumnCount() == 0)
        {
            return null;
        }

        final int idIndex = ArgCursor.getColumnIndex(IdField);
        int nameIndex = ArgCursor.getColumnIndex(NameField);
		if (ArgIsComplete && ArgCursor.getColumnIndex(NameWhenPSField) != -1)
		{
			nameIndex = ArgCursor.getColumnIndex(NameWhenPSField);
		}
        final int codeIndex = ArgCursor.getColumnIndex(CodeField);
        final int isFavoriteIndex = ArgCursor.getColumnIndex(IsFavoriteField);
		final int isFavoriteDetailledIndex = ArgCursor.getColumnIndex(IsFavoriteDetailledField);
        final int isVisibleIndex = ArgCursor.getColumnIndex(IsVisibleField);
        final int favoriteNumberIndex = ArgCursor.getColumnIndex(FavoriteNumberField);
		final int cffIndex = ArgCursor.getColumnIndex(CFFField);
        final int fkMnemoIndex = ArgCursor.getColumnIndex(FkMnemoField);

        Stop stop = new Stop();

        if (idIndex != -1)
        {
            stop.setId(ArgCursor.getLong(idIndex));
        }

        if (nameIndex != -1)
        {
            String name = ArgCursor.getString(nameIndex);
            stop.setName(name);
        }

        if (codeIndex != -1)
        {
            stop.setCode(ArgCursor.getString(codeIndex));
        }

        if (isFavoriteIndex != -1)
        {
            int isFavoriteNumber = ArgCursor.getInt(isFavoriteIndex);
            boolean isFavorite = false;
            if (isFavoriteNumber > 0)
            {
                isFavorite = true;
            }
            stop.setFavorite(isFavorite);
        }

		if (isFavoriteDetailledIndex != -1)
		{
			int isFavoriteDetailledNumber = ArgCursor.getInt(isFavoriteDetailledIndex);
			boolean isFavoriteDetailled = false;
			if (isFavoriteDetailledNumber > 0)
			{
				isFavoriteDetailled = true;
			}
			stop.setFavoriteDetailled(isFavoriteDetailled);
		}

        if (isVisibleIndex != -1)
        {
            int isVisibleNumber = ArgCursor.getInt(isVisibleIndex);
            boolean isVisible = false;
            if (isVisibleNumber > 0)
            {
                isVisible = true;
            }
            stop.setVisible(isVisible);
        }

        if (favoriteNumberIndex != -1)
        {
            stop.setFavoriteNumber(ArgCursor.getLong(favoriteNumberIndex));
        }

		if (cffIndex != -1)
		{
			stop.setCFF(ArgCursor.getString(cffIndex));
		}

        if (fkMnemoIndex != -1)
        {
            stop.setMnemo(new MnemoDAO(getDB()).find(ArgCursor.getLong(fkMnemoIndex)));
        }



        if (ArgIsComplete)
        {
			PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(m_dbHelper);

			if (ArgCursor.getColumnIndex(PhysicalStopDAO.IdField) != -1)
			{
				PhysicalStop ps = physicalStopDAO.fromCursor(ArgCursor, ArgWithConnections);
				stop.getPhysicalStops().add(ps);
			}
			else {
				List<PhysicalStop> physicalStops = physicalStopDAO.findByStopId(stop.getId(), ArgWithConnections);
				stop.setPhysicalStops(physicalStops);
			}
        }

        return stop;
    }

	@Override
	public List<Stop> getAll() {
		return getAll(true,true);
	}
	
	@Override
	public List<Stop> getAll(boolean ArgIsPhysical) {
		return getAll(ArgIsPhysical,true);
	}

	/* (non-Javadoc)
	 * @see com.otpg.engines.data.interfaces.IStopDAO#getAll(boolean)
	 */
	@Override
	public List<Stop> getAll(boolean ArgIsPhysical, boolean ArgIsComplete) {
		return getAll(ArgIsPhysical, ArgIsComplete, true);
	}

    @Override
    public List<Stop> getAll(boolean ArgIsPhysical, boolean ArgIsComplete, boolean ArgWithConnections) {
        List<Stop> stops = new ArrayList<Stop>();

        try
        {
            String[] selectNormal = new String[]{IdField,NameField,CodeField,FkMnemoField,IsFavoriteField,IsFavoriteDetailledField,IsVisibleField,FavoriteNumberField,CFFField};
            String[] selectPhysical = new String[]{IdField,NameField,CodeField,IsFavoriteField,IsFavoriteDetailledField,IsVisibleField,FkMnemoField,FavoriteNumberField,CFFField};

            String[] finalSelect = selectNormal;
            if (ArgIsPhysical)
            {
                finalSelect = selectPhysical;
            }

			String orderBy = FavoriteNumberField + "," + IdField;
            Cursor cursor;
			if (ArgIsComplete)
			{
				// "SELECT * FROM " + TableName + " s LEFT JOIN " + PhysicalStopDAO.TableName + " ps ON " + IdField + " = "
				String query = String.format("SELECT s.%1s AS %2s, * FROM %3s s LEFT JOIN %4s ps ON %5s = %6s WHERE %7s = ? ORDER BY %8s",NameField,
						                                                                                       NameWhenPSField,
																										       TableName,
						         																	           PhysicalStopDAO.TableName,
																											   IdField,
																											   PhysicalStopDAO.FkStopField,
																											   IsVisibleField,
																											   orderBy);

				cursor = m_dbHelper.getSqlDB().rawQuery(query,new String[]{String.valueOf(1)});
			}
			else
			{
				cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IsVisibleField + " = 1",null,null,null,orderBy);
			}

			long currentId = -1;
            while (cursor.moveToNext())
            {
                Stop stop = fromCursor(cursor,ArgIsComplete, ArgWithConnections); // ArgIsComplete
                if (stop != null)
                {
					if (ArgIsComplete) {
						if (stop.getId() != currentId) {
							stops.add(stop);
							currentId = stop.getId();
						} else {
							stops.get(stops.size() - 1).getPhysicalStops().addAll(stop.getPhysicalStops());
						}
					}
					else
					{
						stops.add(stop);
					}
                }
            }

            cursor.close();

           /* PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(m_dbHelper);
            List<PhysicalStop> physicalStops = physicalStopDAO.getAll();
            for (Stop stop : stops)
            {
                for (PhysicalStop physicalStop : physicalStops)
                {
                    if (physicalStop.getStopId() == stop.getId())
                    {
                        stop.getPhysicalStops().add(physicalStop);
                    }
                }
            }*/

        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            stops.clear();
        }

        return stops;
    }

    @Override
    public List<Stop> getAllByName(String ArgName, boolean ArgIsComplete) {
        List<Stop> stops = new ArrayList<Stop>();

        try
        {
            String[] selectNormal = new String[]{IdField,NameField,CodeField,FkMnemoField,IsFavoriteField,IsFavoriteDetailledField,IsVisibleField,CFFField,FavoriteNumberField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, selectNormal, IsVisibleField + " = 1 AND " + NameField + " = ?",new String[]{ArgName},null,null,IdField);
            while (cursor.moveToNext())
            {
                Stop stop = fromCursor(cursor,ArgIsComplete); // ArgIsComplete
                if (stop != null)
                {
                    stops.add(stop);
                }
            }

            cursor.close();

        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            stops.clear();
        }

        return stops;
    }

    @Override
	public List<Stop> getAllFavorites(boolean ArgIsPhysical, final boolean ArgIsComplete) {
		List<Stop> stops = new ArrayList<Stop>();
		
		try
		{
			String[] selectNormal = new String[]{IdField,NameField,FkMnemoField,IsFavoriteField,IsFavoriteDetailledField,IsVisibleField,CFFField,FavoriteNumberField};
			String[] selectPhysical = new String[]{IdField,NameField,IsFavoriteField,IsFavoriteDetailledField,IsVisibleField,FkMnemoField,CFFField,FavoriteNumberField};
			
			String[] finalSelect = selectNormal;
			if (ArgIsPhysical)
			{
				finalSelect = selectPhysical;
			}
			
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IsFavoriteField + " = 1",null,null,null,FavoriteNumberField);
			while (cursor.moveToNext())
			{
				Stop stop = fromCursor(cursor, ArgIsComplete);
				if (stop != null)
				{
					stops.add(stop);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			stops.clear();
		}
		
		return stops;
	}

	@Override
	public List<Stop> getByIds(List<Long> ArgStopIds) {
        return getByIds(ArgStopIds,true);

	}

    public List<Stop> getByIds(List<Long> ArgStopIds, boolean ArgIsComplete) {
        List<Stop> stops = new ArrayList<Stop>();

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
            String[] selectPhysical = new String[]{IdField,NameField,IsFavoriteField,IsFavoriteDetailledField,IsVisibleField,FkMnemoField,CFFField,FavoriteNumberField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, selectPhysical, filter, null ,null,null,NameField);
            while (cursor.moveToNext())
            {
                Stop stop = fromCursor(cursor,ArgIsComplete);
                if (stop != null)
                {
                    stops.add(stop);
                }
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            stops.clear();
        }

        return stops;

    }

    @Override
    public List<Stop> getByLocation(Location ArgLoc, int ArgNumber) {
        List<Stop> stops = new ArrayList<Stop>();

        if (ArgNumber < 1 || ArgLoc == null)
        {
            return stops;
        }


        try
        {
            PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(m_dbHelper);
            List<PhysicalStop> physicalStops = physicalStopDAO.getByLocation(ArgLoc,ArgNumber);

            List<Long> stopIds = new ArrayList<Long>();
            for (PhysicalStop physicalStop : physicalStops)
            {
                stopIds.add(physicalStop.getStopId());
            }

            List<Stop> newStops = getByIds(stopIds);



            for (Long id : stopIds)
            {
                for (Stop stop : newStops)
                {
                    if (stop.getId() == id)
                    {
                        stops.add(stop);
                        break;
                    }
                }
            }

            List<String> names = new ArrayList<String>();
            for (int i = stops.size()-1; i >= 0; i--)
            {
                if (names.contains(stops.get(i).getName()))
                {
                    stops.remove(i);
                }
                else
                {
                    names.add(stops.get(i).getName());
                }
            }

            for (int i = stops.size()-1; i > ArgNumber; i--)
            {
                stops.remove(i);
            }

           // stops = StopTools.sortStopsByDistance(stops,ArgLoc);


        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            stops.clear();
        }

        return stops;
    }

    @Override
    public Stop getLastFavorite() {
        Stop stop = null;

        try
        {
            String[] finalSelect = new String[]{IdField,NameField,CodeField,IsFavoriteField,IsFavoriteDetailledField,FavoriteNumberField,CFFField,FkMnemoField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IsFavoriteField + " = 1 AND " + FavoriteNumberField + " != 9999",null,null,null,FavoriteNumberField + " DESC LIMIT 1");
            if (cursor.moveToFirst())
            {
                stop = fromCursor(cursor,false);
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
    public int removeAllFavorites(List<Stop> ArgStops) {
        if (ArgStops == null || ArgStops.size() == 0)
        {
            return 0;
        }

        int numberUpdated = 0;
        try
        {
            for (Stop stop : ArgStops)
            {
                long favoriteNumber = stop.getFavoriteNumber();

                stop.setFavoriteNumber(9999);
                stop.setFavorite(false);
				stop.setFavoriteDetailled(false);

                if (update(stop))
                {
                    numberUpdated++;
                }
                else
                {
                    stop.setFavorite(true);
                    stop.setFavoriteNumber(favoriteNumber);
                }
            }

        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
        }

        return numberUpdated;
    }

    @Override
	public boolean removeFavorite(Stop ArgStop) {
		Stop stop = find(ArgStop.getMnemo().getName());
		if (stop != null)
		{
            List<Stop> favorites = getAllFavorites(false,false);
            for (Stop favorite : favorites)
            {
                if (favorite.getId() == stop.getId())
                {
                    continue;
                }
                long favoriteNumber = favorite.getFavoriteNumber();
                if (favoriteNumber > stop.getFavoriteNumber())
                {
                    favoriteNumber--;
                    favorite.setFavoriteNumber(favoriteNumber);
                    update(favorite);
                }
            }
            stop.setFavoriteNumber(9999);
			stop.setFavorite(false);
			stop.setFavoriteDetailled(false);
			if (update(stop))
			{
				ConnectionDAO connDAO = new ConnectionDAO(getDB());
				connDAO.removeAllFavorites(connDAO.getConnectionsByStop(stop.getId()));
			}
			return true;
		}
		return false;
	}



	@Override
	public Stop search(String ArgTextToSearch) {
		return search(ArgTextToSearch,0,true);
	}

	public Stop search(String ArgTextToSearch, int argNumberOfTimes)
	{
		return search(ArgTextToSearch, argNumberOfTimes, true);
	}

	public Stop search(String ArgTextToSearch, boolean ArgIsComplete)
	{
		return search(ArgTextToSearch, 0, ArgIsComplete);
	}

	public Stop search(String ArgTextToSearch, int argNumberOfTimes, boolean ArgIsComplete)
	{
		String textToSearch = ArgTextToSearch.trim();
		//Log.d("TEXT TO SEARCH", textToSearch);
		Stop stop;

		stop = find(textToSearch,ArgIsComplete);

		if (stop != null)
		{
			return stop;
		}

		stop = findByName(textToSearch,ArgIsComplete);

		if (stop != null)
		{
			return stop;
		}

		stop = findByCode(textToSearch);

		if (stop != null)
		{
			return stop;
		}

		textToSearch = textToSearch.replace(' ', '-');
		if (!textToSearch.equalsIgnoreCase(ArgTextToSearch))
		{

			stop = findByName(textToSearch,ArgIsComplete);

			if (stop != null)
			{
				return stop;
			}

			stop = findByCode(textToSearch,ArgIsComplete);

			if (stop != null)
			{
				return stop;
			}
		}
		else
		{
			textToSearch = textToSearch.replace('-', ' ');
			if (!textToSearch.equalsIgnoreCase(ArgTextToSearch))
			{

				stop = findByName(textToSearch);

				if (stop != null)
				{
					return stop;
				}

				stop = findByCode(textToSearch);

				if (stop != null)
				{
					return stop;
				}
			}
		}

		stop = findByName(StopTools.getRealStopName(ArgTextToSearch),ArgIsComplete);

		if (stop == null && argNumberOfTimes == 0)
		{
			String textWithoutAccent = ArgTextToSearch;
			if (!ArgTextToSearch.equalsIgnoreCase(textWithoutAccent))
			{
				return search(TextTools.removeAccent(textWithoutAccent),++argNumberOfTimes,ArgIsComplete);
			}
		}

		return stop;
	}

	@Override
	public boolean update(Stop ArgStop) {
		if (ArgStop == null || ArgStop.getId() < 1)
		{
			return false;
		}
		
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(NameField, ArgStop.getName());
			cv.put(CodeField, ArgStop.getCode());
			cv.put(IsFavoriteField, ArgStop.isFavorite());
			cv.put(IsFavoriteDetailledField, ArgStop.isFavoriteDetailled());
			cv.put(FkMnemoField, ArgStop.getMnemo().getId());
            cv.put(FavoriteNumberField, ArgStop.getFavoriteNumber());
			
			int numberRowsUpdated = m_dbHelper.getSqlDB().update(TableName, cv, IdField + " = ?", new String[]{String.valueOf(ArgStop.getId())});
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
		List<String> stopNames = new ArrayList<>();

		try
		{
			String[] selectNormal = new String[]{NameField};

			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, selectNormal, IsVisibleField + " = 1",new String[]{},null,null,NameField);
			while (cursor.moveToNext())
			{
				stopNames.add(cursor.getString(cursor.getColumnIndex(NameField)));
			}

			cursor.close();

		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			stopNames.clear();
		}

		String[] stopNamesArray = new String[stopNames.size()];
		stopNames.toArray(stopNamesArray);

		return stopNamesArray;
	}
}
