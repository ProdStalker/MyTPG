/**
 * 
 */
package com.mytpg.engines.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mytpg.engines.data.interfaces.ILineDAO;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.StopTools;
import com.mytpg.engines.tools.TextTools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class LineDAO extends DAO<Line> implements ILineDAO {
	public static final String IdField = "pkLine";
	public static final String NameField = "name";
	public static final String ColorField = "color";
	
	public static final String FkDestinationField = "fkDestination";
	
	public static final String TableName = "tbllines";
    public static final String TABLE_SCRIPT = "CREATE TABLE " + TableName +
            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NameField + " TEXT NOT NULL collate nocase, " +
            ColorField + " INTEGER NOT NULL, " +
            FkDestinationField + " INTEGER NOT NULL DEFAULT -1);";
	
	public LineDAO(DatabaseHelper ArgDBHelper) {
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
	public boolean create(Line ArgLine) {
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(NameField, ArgLine.getName());
			cv.put(ColorField, ArgLine.getColor());
			
			if (ArgLine.getArrivalStop().getId() == -1)
			{
				StopDAO stopDAO = new StopDAO(m_dbHelper);
				Stop stop = stopDAO.search(ArgLine.getArrivalStop().getName());
				if (stop != null)
				{
					ArgLine.getArrivalStop().setId(stop.getId());
				}
			}
			
			cv.put(FkDestinationField, ArgLine.getArrivalStop().getId());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgLine.setId(id);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean create(List<Line> ArgLines) {
		try
		{
			StopDAO stopDAO = new StopDAO(m_dbHelper);
			
			String sql = "INSERT INTO " + TableName + " (" + NameField + ", " +
                    										 ColorField + ", " +
                    										 FkDestinationField + ") " +
                    										 "VALUES (?,?,?)";

			SQLiteDatabase db = m_dbHelper.getWritableDatabase();
			
			db.beginTransaction();
			
			SQLiteStatement stmt = db.compileStatement(sql);
			int i = 0;
			while (i < ArgLines.size())
			{
				Line line = ArgLines.get(i);
				
				if (line.getArrivalStop().getId() == -1)
				{
					Stop stop = stopDAO.search(line.getArrivalStop().getName(),1);
					if (stop != null)
					{
						line.getArrivalStop().setId(stop.getId());
					}
				}
				
				stmt.bindString(1, line.getName());
				stmt.bindLong(2, line.getColor());
				stmt.bindLong(3, line.getArrivalStop().getId());
				
				final long Id = stmt.executeInsert();
				line.setId(Id);
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
	public boolean delete(Line ArgLine) {
		boolean isOk = false;
		if (ArgLine == null || ArgLine.getId() < 1)
		{
			return isOk;
		}
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgLine.getId())});
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
    public Line find(long ArgId, boolean ArgIsComplete) {
        Line line = null;

        try
        {
            String[] finalSelect = new String[]{IdField,NameField,ColorField,FkDestinationField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IdField + " = ? ",new String[]{String.valueOf(ArgId)},null,null,NameField + " LIMIT 1");
            if (cursor.moveToFirst())
            {
                line = fromCursor(cursor,ArgIsComplete);
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            line = null;
        }

        return line;
    }

	public Line findByLine(Line argLine)
	{
		return findByLine(argLine,0);
	}

	public Line findByLine(Line argLine, int argNumberOfTimes)
	{
		Line line = null;

		try
		{
			String formattedName = argLine.getArrivalStop().getName().replaceAll("-"," ");
			String nameWithoutAccent = TextTools.removeAccent(argLine.getArrivalStop().getName());
			String like = "%" + nameWithoutAccent + "%";
			//Log.d("NAME WITHOUT ACCENT", argLine.getArrivalStop().getName() + " <==> " + nameWithoutAccent);

			//String[] finalSelect = new String[]{IdField,NameField,ColorField,FkDestinationField};
			String query = "SELECT tbllines.* " +
					       "FROM tbllines " +
			               "JOIN tblstops ON tbllines.fkDestination = pkStop " +
					       "WHERE (tblstops.name = ? OR tblstops.code = ? OR " +
					             "tblstops.name = ? OR tblstops.code = ? OR " +
								 "tblstops.name = ? OR tblstops.code = ? OR " +
			                     "tblstops.name = ? OR tblstops.code = ? OR " +
					             "tblstops.name LIKE ? OR tblstops.code LIKE ? ) AND " +
					             "tbllines.name = ? " +
					               "LIMIT 1";

			Cursor cursor = m_dbHelper.getSqlDB().rawQuery(query, new String[]{argLine.getArrivalStop().getName(),argLine.getArrivalStop().getCode(),
					                                                           argLine.getArrivalStop().getCode(), argLine.getArrivalStop().getName(),
			                                                                   formattedName, formattedName,
					                                                           nameWithoutAccent,nameWithoutAccent,
					                                                           like, like,
			                                                                   argLine.getName()});

			if (cursor.moveToFirst())
			{
				line = fromCursor(cursor);
			}

			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			line = null;
		}

		if ((line == null || line.getId() == -1))
		{
			Line newLine = new Line(argLine);
			String realName = StopTools.getRealStopName(argLine.getArrivalStop().getName());
			boolean searchAgain = false;
			if (argNumberOfTimes == 0)
			{
				searchAgain = true;
			}
			else if (!realName.equalsIgnoreCase(newLine.getArrivalStop().getName()))
			{
				searchAgain = true;
			}

			if (searchAgain) {
				newLine.getArrivalStop().setName(realName);
				return findByLine(newLine, ++argNumberOfTimes);
			}

		}


		return line;
	}

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Line findByName(String ArgName) {
        Line line = null;

        if (ArgName.isEmpty())
        {
            return line;
        }

        try
        {
            String[] finalSelect = new String[]{IdField,NameField,ColorField,FkDestinationField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, NameField + " = ? ",new String[]{ArgName},null,null,NameField + " LIMIT 1");
            if (cursor.moveToFirst())
            {
                line = fromCursor(cursor);
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            line = null;
        }

        return line;
    }

	@Override
	protected Line fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
        if (ArgCursor.getColumnCount() == 0)
        {
            return null;
        }

        final int idIndex = ArgCursor.getColumnIndex(IdField);
        final int nameIndex = ArgCursor.getColumnIndex(NameField);
        final int colorIndex = ArgCursor.getColumnIndex(ColorField);
        final int fkDestinationIndex = ArgCursor.getColumnIndex(FkDestinationField);

        Line line = new Line();

        if (idIndex != -1)
        {
            line.setId(ArgCursor.getLong(idIndex));
        }

        if (nameIndex != -1)
        {
            line.setName(ArgCursor.getString(nameIndex));
        }

        if (colorIndex != -1)
        {
            line.setColor(ArgCursor.getInt(colorIndex));
        }

        if (fkDestinationIndex != -1)
        {
            StopDAO stopDAO = new StopDAO(m_dbHelper);
			if (ArgIsComplete) {
				line.setArrivalStop(stopDAO.find(ArgCursor.getLong(fkDestinationIndex), false));
			}
			else
			{
				line.getArrivalStop().setId(ArgCursor.getLong(fkDestinationIndex));
			}
        }

        return line;
	}

	@Override
	public List<Line> getAll() {
		List<Line> lines = new ArrayList<Line>();
		
		try
		{
			String[] finalSelect = new String[]{IdField,NameField,ColorField,FkDestinationField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, null,null,null,null,IdField);
			while (cursor.moveToNext())
			{
				Line line = fromCursor(cursor);
				if (line != null)
				{
					lines.add(line);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			lines.clear();
		}
		
		return lines;
	}

	@Override
	public List<Line> getAll(boolean ArgIsDistinct) {
		if (!ArgIsDistinct)
		{
			return getAll();
		}
		
		List<Line> lines = new ArrayList<Line>();
		
		try
		{
			String[] finalSelect = new String[]{IdField,NameField,ColorField,FkDestinationField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, null,null,NameField,null,NameField);
			while (cursor.moveToNext())
			{
				Line line = fromCursor(cursor);
				if (line != null)
				{
					lines.add(line);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			lines.clear();
		}
		
		return lines;
	}

	@Override
	public List<Line> getAllByName(final String ArgName) {
		List<Line> lines = new ArrayList<Line>();
		
		if (ArgName.trim().isEmpty())
		{
			return lines;
		}
		
		try
		{
			String[] finalSelect = new String[]{IdField,NameField,ColorField,FkDestinationField};

			Cursor cursor = m_dbHelper.getSqlDB().query(true, TableName, finalSelect, NameField + " = ?",new String[]{ArgName},FkDestinationField,null,NameField, null);
			while (cursor.moveToNext())
			{
				Line line = fromCursor(cursor);
				if (line != null)
				{
					lines.add(line);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			lines.clear();
		}
		
		return lines;
	}

	public List<Line> getAllByPhysicalStop(final long ArgPhysicalStopId) {
		/*List<Line> lines = new ArrayList<>();


		ConnectionDAO connectionDAO = new ConnectionDAO(m_dbHelper);
		List<Connection> connections = connectionDAO.getConnectionsByPhysicalStop(ArgPhysicalStopId);
		for (Connection conn : connections)
		{
			lines.add(conn.getLine());
		}

		return lines;*/

		List<Line> lines = new ArrayList<>();

		try
		{
			String query = "SELECT tbllines.* FROM tblconnections LEFT JOIN tbllines ON fkLine = pkLine WHERE fkPhysicalStop = ?";

			//String[] finalSelect = new String[]{IdField,NameField,ColorField,FkDestinationField};

			//Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, NameField + " = ?",new String[]{ArgName},null,null,NameField);
			Cursor cursor = m_dbHelper.getSqlDB().rawQuery(query, new String[]{String.valueOf(ArgPhysicalStopId)});
			while (cursor.moveToNext())
			{
				Line line = fromCursor(cursor);
				if (line != null)
				{
					lines.add(line);
				}
			}

			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			lines.clear();
		}

		return lines;
	}

	@Override
	public boolean update(Line ArgObj) {
		// TODO Auto-generated method stub
		return false;
	}

}
