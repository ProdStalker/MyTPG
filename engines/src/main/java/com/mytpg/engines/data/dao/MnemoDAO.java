/**
 * 
 */
package com.mytpg.engines.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.mytpg.engines.data.interfaces.IMnemoDAO;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Mnemo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class MnemoDAO extends DAO<Mnemo> implements IMnemoDAO {
	public static final String IdField = "pkMnemo";
	public static final String NameField = "name";
	
	public static final String TableName = "tblmnemos";
    public static final String TABLE_SCRIPT = "CREATE TABLE " + TableName +
            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NameField + " TEXT NOT NULL collate nocase);";
	
	public MnemoDAO(DatabaseHelper ArgDBHelper) {
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
	public boolean create(Mnemo ArgMnemo) {
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(NameField, ArgMnemo.getName());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgMnemo.setId(id);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean create(List<Mnemo> ArgObj) {
		// TODO Auto-generated method stub
		return false;
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
	public boolean delete(Mnemo ArgMnemo) {
		boolean isOk = false;
		if (ArgMnemo == null || ArgMnemo.getId() < 1)
		{
			return isOk;
		}
		
		try
		{
			m_dbHelper.getSqlDB().beginTransaction();
			
			int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgMnemo.getId())});
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
    public Mnemo find(long ArgId, boolean ArgIsComplete) {
        Mnemo mnemo = null;

        if (ArgId < 1)
        {
            return null;
        }

        try
        {
            String[] finalSelect = new String[]{IdField,NameField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IdField + " = ? ",new String[]{String.valueOf(ArgId)},null,null,NameField + " LIMIT 1");
            if (cursor.moveToFirst())
            {
                mnemo = fromCursor(cursor);
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            mnemo = null;
        }

        return mnemo;
    }

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Mnemo findByName(String ArgName) {
        return find(ArgName);
    }

    public Mnemo find(String ArgName)
	{
		Mnemo mnemo = null;
        if (ArgName.isEmpty())
        {
            return mnemo;
        }
		
		try
		{
			String[] finalSelect = new String[]{IdField,NameField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, NameField + " = ? ",new String[]{ArgName},null,null,NameField + " LIMIT 1");
			if (cursor.moveToFirst())
			{
				mnemo = fromCursor(cursor);
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			mnemo = null;
		}
		
		return mnemo;
	}

	@Override
	protected Mnemo fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
        if (ArgCursor.getColumnCount() == 0)
        {
            return null;
        }

        final int idIndex = ArgCursor.getColumnIndex(IdField);
        final int nameIndex = ArgCursor.getColumnIndex(NameField);

        Mnemo mnemo = new Mnemo();

        if (idIndex != -1)
        {
            mnemo.setId(ArgCursor.getLong(idIndex));
        }

        if (nameIndex != -1)
        {
            mnemo.setName(ArgCursor.getString(nameIndex));
        }

        return mnemo;
	}

	@Override
	public List<Mnemo> getAll() {
		List<Mnemo> mnemos = new ArrayList<Mnemo>();
		
		try
		{
			String[] finalSelect = new String[]{IdField,NameField};
			
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, null,null,null,null,NameField);
			while (cursor.moveToNext())
			{
				Mnemo mnemo = fromCursor(cursor);
				if (mnemo != null)
				{
					mnemos.add(mnemo);
				}
			}
			
			cursor.close();
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			mnemos.clear();
		}
		
		return mnemos;
	}

	@Override
	public boolean update(Mnemo ArgObj) {
		// TODO Auto-generated method stub
		return false;
	}

}
