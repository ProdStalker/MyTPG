/**
 * 
 */
package com.mytpg.engines.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mytpg.engines.data.interfaces.IConnectionDAO;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.PhysicalStop;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class ConnectionDAO extends DAO<Connection> implements IConnectionDAO {
	public static final String IdField = "pkConnection";
	public static final String IsFavoriteField = "isFavorite";
	
	public static final String FkLineField = "fkLine";
	public static final String FkPhysicalStopField = "fkPhysicalStop";
	
	public static final String TableName = "tblconnections";
	
	private static final String[] BASE_SELECT = new String[]{IdField,IsFavoriteField, FkLineField,FkPhysicalStopField};
    public static final String TABLE_SCRIPT = "CREATE TABLE " + TableName +
            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			      IsFavoriteField + " INTEGER NOT NULL DEFAULT 0, " +
                  FkLineField + " INTEGER NOT NULL, " +
                  FkPhysicalStopField + " INTEGER NOT NULL);";
	
	public ConnectionDAO(DatabaseHelper ArgDBHelper) {
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
	public boolean create(Connection ArgConnection) {
		if (ArgConnection == null || ArgConnection.getPhysicalStop().getId() < 1 || ArgConnection.getLine().getId() < 1)
		{
			return false;
		}
		
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(IsFavoriteField, ArgConnection.isFavorite());
			cv.put(FkLineField, ArgConnection.getLine().getId());
			cv.put(FkPhysicalStopField, ArgConnection.getPhysicalStop().getId());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean create(List<Connection> ArgConnections) {
		if (ArgConnections == null || ArgConnections.size() == 0)
		{
			return false;
		}
		
		try
		{
			String sql = "INSERT INTO " + TableName + " (" + IsFavoriteField + ", " +
					                                         FkLineField + ", " +
                    										 FkPhysicalStopField + ") " +
                    										 "VALUES (?,?,?)";

			SQLiteDatabase db = m_dbHelper.getWritableDatabase();
			
			db.beginTransaction();
			
			final int NumberElements = ArgConnections.size();
			
			SQLiteStatement stmt = db.compileStatement(sql);
			int i = 0;
			while (i < NumberElements)
			{
				Connection conn = ArgConnections.get(i);
				if (conn.getLine().getName().equalsIgnoreCase("A"))
				{
					conn.getPhysicalStop().setId(conn.getPhysicalStop().getId());
				}
				if (conn.getLine().getId() < 1 || conn.getPhysicalStop().getId() < 1)
				{
					i++;
					continue;
				}

				long isFavoriteNumber = 0;
				if (conn.isFavorite())
				{
					isFavoriteNumber = 1;
				}
				stmt.bindLong(1, isFavoriteNumber);
				stmt.bindLong(2, conn.getLine().getId());
				stmt.bindLong(3, conn.getPhysicalStop().getId());
				
				final long Id = stmt.executeInsert();
				conn.setId(Id);
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
	public boolean delete(Connection ArgConnection) {
		return false;
	}
	
	@Override
	public boolean delete(long ArgId) {
		boolean isOk = false;
		if (ArgId < 1)
		{
			return isOk;
		}
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgId)});
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

    /**
     * @param ArgId
     * @param ArgIsComplete
     * @return
     */
    @Override
    public Connection find(long ArgId, boolean ArgIsComplete) {
        Connection connection = null;

        try
        {
            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, IdField + " = ? ",new String[]{String.valueOf(ArgId)},null,null,FkLineField + " LIMIT 1");
            if (cursor.moveToFirst())
            {
                connection = fromCursor(cursor,ArgIsComplete);
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            connection = null;
        }

        return connection;
    }


    /**
     * @param ArgName
     * @return
     */
    @Override
    public Connection findByName(String ArgName) {
        return null;
    }

	@Override
	protected Connection fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
        if (ArgCursor.getColumnCount() == 0)
        {
            return null;
        }

        final int idIndex = ArgCursor.getColumnIndex(IdField);
		final int isFavoriteIndex = ArgCursor.getColumnIndex(IsFavoriteField);
        final int fkLineIndex = ArgCursor.getColumnIndex(FkLineField);
        final int fkPhysicalStopIndex = ArgCursor.getColumnIndex(FkPhysicalStopField);

        Connection connection = new Connection();

        if (idIndex != -1)
        {
            connection.setId(ArgCursor.getLong(idIndex));
        }

		if (isFavoriteIndex != -1)
		{
			int isFavoriteNumber = ArgCursor.getInt(isFavoriteIndex);
			boolean isFavorite = false;
			if (isFavoriteNumber > 0)
			{
				isFavorite = true;
			}
			connection.setFavorite(isFavorite);
		}


        if (fkLineIndex != -1)
        {
            LineDAO lineDAO = new LineDAO(m_dbHelper);
            connection.setLine(lineDAO.find(ArgCursor.getLong(fkLineIndex)));
        }

        if (fkPhysicalStopIndex != -1)
        {
            //connection.getPhysicalStop().setId(ArgCursor.getLong(fkStopIndex));
            PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(m_dbHelper);
            PhysicalStop physicalStop = physicalStopDAO.find(ArgCursor.getLong(fkPhysicalStopIndex),false);
            connection.setPhysicalStop(physicalStop);
        }

        return connection;
	}

	@Override
	public List<Connection> getAll() {
		List<Connection> connections = new ArrayList<Connection>();
		
		try
		{
			
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, null,null,null,null,IdField);
			while (cursor.moveToNext())
			{
				Connection connection = fromCursor(cursor);
				if (connection != null)
				{
					connections.add(connection);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			connections.clear();
		}
		
		return connections;
	}

	public List<Connection> getAllFavorites() {
		List<Connection> connections = new ArrayList<Connection>();

		try
		{


			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, IsFavoriteField + "= 1",null,null,null,IdField);
			while (cursor.moveToNext())
			{
				Connection connection = fromCursor(cursor);
				if (connection != null)
				{
					connections.add(connection);
				}
			}

			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			connections.clear();
		}

		return connections;
	}

	@Override
	public List<Connection> getConnectionsByLine(long ArgLineId) {
		List<Connection> connections = new ArrayList<Connection>();
		
		try
		{
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, FkLineField + " = ? AND " + FkPhysicalStopField + " != -1", new String[]{String.valueOf(ArgLineId)},null,null,FkLineField);
			while (cursor.moveToNext())
			{
				Connection connection = fromCursor(cursor);
				if (connection != null)
				{
					connections.add(connection);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			connections.clear();
		}
		
		return connections;
	}

	@Override
	public List<Connection> getConnectionsByLines(List<Long> ArgLineIds) {
		List<Connection> connections = new ArrayList<Connection>();
		
		try
		{
			String inClause = "";
			final int NumberElements = ArgLineIds.size();
			int i = 0;
			while (i < NumberElements)
			{
				inClause += String.valueOf( ArgLineIds.get(i).longValue());
				
				i++;
				if (i < NumberElements)
				{
					inClause += ",";
				}
			}
			
			String filter = FkLineField + " IN (" + inClause + ")";
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, filter + " AND " + FkPhysicalStopField + " != -1", null ,null,null,FkLineField);
			while (cursor.moveToNext())
			{
				Connection connection = fromCursor(cursor);
				if (connection != null)
				{
					connections.add(connection);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			connections.clear();
		}
		
		return connections;
	}
	
	@Override
	public List<Connection> getConnectionsByLinesAndPhysicalStop(List<Long> ArgLineIds, final long ArgPhysicalStopId) {
		List<Connection> connections = new ArrayList<Connection>();
		
		try
		{
			String inClause = "";
			final int NumberElements = ArgLineIds.size();
			int i = 0;
			while (i < NumberElements)
			{
				inClause += String.valueOf( ArgLineIds.get(i).longValue());
				
				i++;
				if (i < NumberElements)
				{
					inClause += ",";
				}
			}
			
			String filter = FkLineField + " IN (" + inClause + ")";
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, filter + " AND " + FkPhysicalStopField + " = ?", new String[]{String.valueOf(ArgPhysicalStopId)} ,null,null,FkLineField);
			while (cursor.moveToNext())
			{
				Connection connection = fromCursor(cursor);
				if (connection != null)
				{
					connections.add(connection);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			connections.clear();
		}
		
		return connections;
	}


	@Override
	public List<Connection> getConnectionsByPhysicalStop(final long ArgPhysicalStopId) {
		List<Connection> connections = new ArrayList<Connection>();
		
		try
		{
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, FkPhysicalStopField + " = ? AND " + FkLineField + " != -1", new String[]{String.valueOf(ArgPhysicalStopId)},null,null,FkLineField);
			while (cursor.moveToNext())
			{
				Connection connection = fromCursor(cursor);
				if (connection != null)
				{
					connections.add(connection);
				}
			}
			
			cursor.close();
        }
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			connections.clear();
		}
		
		return connections;
	}

    @Override
    public List<Connection> getConnectionsByStop(final long ArgStopId) {
        return getConnectionsByStop(ArgStopId, false);
    }

    @Override
    public List<Connection> getConnectionsByStop(final long ArgStopId, boolean argOnlyFavorite) {
        List<Connection> connections = new ArrayList<Connection>();

        try
        {
            PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(m_dbHelper);
            List<PhysicalStop> physicalStops = physicalStopDAO.getAllByStopId(ArgStopId);

            List<Long> physicalStopIds = new ArrayList<Long>();
            for (PhysicalStop physicalStop : physicalStops){
                if (!physicalStopIds.contains(physicalStop.getId())) {
                    physicalStopIds.add(physicalStop.getId());
                }
            }

            String inClause = "";
            final int NumberElements = physicalStopIds.size();
            int i = 0;
            while (i < NumberElements)
            {
                inClause += String.valueOf(physicalStopIds.get(i).longValue());

                i++;
                if (i < NumberElements)
                {
                    inClause += ",";
                }
            }

            String filter = FkPhysicalStopField + " IN (" + inClause + ")";
            if (argOnlyFavorite)
            {
                filter += " AND isFavorite = 1";
            }

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, filter, null ,null,null,FkLineField);

            while (cursor.moveToNext())
            {
                Connection connection = fromCursor(cursor);
                if (connection != null)
                {
                    connections.add(connection);
                }
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            connections.clear();
        }

		/*Collections.sort(connections, new Comparator<Connection>() {
			@Override
			public int compare(Connection c, Connection c2) {

				String name = Normalizer.normalize(c.getLine().getName(), Normalizer.Form.NFD);
				name = name.replaceAll("[^\\p{ASCII}]", "");
				name = name.toLowerCase(Locale.FRENCH);
				String name2 = Normalizer.normalize(c2.getLine().getName(), Normalizer.Form.NFD);
				name2 = name2.replaceAll("[^\\p{ASCII}]", "");
				name2 = name2.toLowerCase(Locale.FRENCH);

				if (name.length() == 1 && Character.isDigit(name.charAt(0)))
				{
					name = "0" + name;
				}

				if (name2.length() == 1 && Character.isDigit(name2.charAt(0)))
				{
					name2 = "0" + name;
				}

				int result = name.compareTo(name2);

				return result;

			}
		});*/

        return connections;
    }

	@Override
	public int removeAllFavorites(List<Connection> ArgConnections) {
		if (ArgConnections == null || ArgConnections.size() == 0)
		{
			return 0;
		}

		int numberUpdated = 0;
		try
		{
			for (Connection conn : ArgConnections)
			{
				conn.setFavorite(false);

				if (update(conn))
				{
					numberUpdated++;
				}
				else
				{
					conn.setFavorite(true);
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
	public boolean update(Connection ArgConnection) {
		if (ArgConnection == null || ArgConnection.getId() < 1)
		{
			return false;
		}

		try
		{
			ContentValues cv = new ContentValues();
			cv.put(IsFavoriteField, ArgConnection.isFavorite());
			cv.put(FkLineField, ArgConnection.getLine().getId());
			cv.put(FkPhysicalStopField, ArgConnection.getPhysicalStop().getId());

			int numberRowsUpdated = m_dbHelper.getSqlDB().update(TableName, cv, IdField + " = ?", new String[]{String.valueOf(ArgConnection.getId())});
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
