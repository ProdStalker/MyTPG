/**
 * 
 */
package com.mytpg.engines.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mytpg.engines.data.interfaces.ITicketDAO;
import com.mytpg.engines.entities.Ticket;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.tools.DateTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author StalkerA
 *
 */
public class TicketDAO extends DAO<Ticket> implements ITicketDAO {
	public final static String IdField = "pkTicket";
	public final static String NameField = "name";
	public final static String DescriptionField = "description";
	public final static String CodeField = "code";
	public final static String PriceField = "price";
	public final static String IsFullField = "isFull";
    public final static String DateField = "date";
	
	public final static String TableName = "tbltickets";
	
	public final static String TABLE_SCRIPT = "CREATE TABLE " + TableName +
									            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
													  NameField + " TEXT NOT NULL collate nocase, " +
													  DescriptionField + " TEXT NOT NULL collate nocase, " +
													  CodeField + " TEXT NOT NULL collate nocase, " +
													  PriceField + " DOUBLE NOT NULL, " +
                                                      IsFullField + " INTEGER NOT NULL DEFAULT 1, " +
									                  DateField + " INTEGER NOT NULL DEFAULT 0);";
	
	private final static String[] BASE_SELECT = new String[]{IdField,NameField,DescriptionField,CodeField,PriceField,IsFullField, DateField};
	
	public TicketDAO(DatabaseHelper ArgDBHelper) {
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
    public long countActive() {
        long number = 0;

        try
        {
            Calendar now = Calendar.getInstance();
            Cursor cursor = m_dbHelper.getSqlDB().rawQuery("SELECT COUNT(" + IdField + ") AS number FROM " + TableName + " WHERE " + DateField + " > " + String.valueOf(now.getTimeInMillis()), null);
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
	public boolean create(Ticket ArgTicket) {
		try
		{
			ContentValues cv = new ContentValues();
			cv.put(NameField, ArgTicket.getName());
			cv.put(DescriptionField, ArgTicket.getDescription());
			cv.put(CodeField, ArgTicket.getCode());
			cv.put(PriceField, ArgTicket.getPrice());
			cv.put(IsFullField, ArgTicket.isFull());
            cv.put(DateField, ArgTicket.getDate().getTimeInMillis());
			
			long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
			if (id == -1)
			{
				return false;
			}
			
			ArgTicket.setId(id);
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean create(List<Ticket> ArgTickets) {
		if (ArgTickets == null || ArgTickets.size() == 0)
		{
			return false;
		}
		
		try
		{
			String sql = "INSERT INTO " + TableName + " (" + NameField + ", " +
															 DescriptionField + ", " + 
															 CodeField + ", " + 
															 PriceField + ", " +
                                                             IsFullField + ", " +
                                                             DateField + ") " +
                    										 "VALUES (?,?,?,?,?,?)";

			SQLiteDatabase db = m_dbHelper.getWritableDatabase();
			
			db.beginTransaction();
			
			final int NumberElements = ArgTickets.size();
			
			SQLiteStatement stmt = db.compileStatement(sql);
			int i = 0;
			while (i < NumberElements)
			{
				Ticket ticket = ArgTickets.get(i);
				
				stmt.bindString(1, ticket.getName());
				stmt.bindString(2, ticket.getDescription());
				stmt.bindString(3, ticket.getCode());
				stmt.bindDouble(4, ticket.getPrice());
				
				long isFullNumber = 0;
				if (ticket.isFull())
				{
					isFullNumber = 1;
				}
				stmt.bindLong(5, isFullNumber);
                stmt.bindLong(6, ticket.getDate().getTimeInMillis());
				
				final long Id = stmt.executeInsert();
				ticket.setId(Id);
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
	public boolean delete(Ticket ArgTicket) {
		// TODO Auto-generated method stub
		return false;
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
    public Ticket find(long ArgId, boolean ArgIsComplete) {
        return null;
    }

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Ticket findByName(String ArgName) {
        return null;
    }

	@Override
	protected Ticket fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
		if (ArgCursor.getCount() == 0)
		{
			return null;
		}
		
		Ticket ticket = new Ticket();
		
		int idIndex = ArgCursor.getColumnIndex(IdField);
		int nameIndex = ArgCursor.getColumnIndex(NameField);
		int descriptionIndex = ArgCursor.getColumnIndex(DescriptionField);
		int codeIndex = ArgCursor.getColumnIndex(CodeField);
		int priceIndex = ArgCursor.getColumnIndex(PriceField);
		int isFullIndex = ArgCursor.getColumnIndex(IsFullField);
        int dateIndex = ArgCursor.getColumnIndex(DateField);
		
		if (idIndex != -1)
		{
			ticket.setId(ArgCursor.getLong(idIndex));
		}
		
		if (nameIndex != -1)
		{
			ticket.setName(ArgCursor.getString(nameIndex));
		}
		
		if (descriptionIndex != -1)
		{
			ticket.setDescription(ArgCursor.getString(descriptionIndex));
		}
		
		if (codeIndex != -1)
		{
			ticket.setCode(ArgCursor.getString(codeIndex));
		}
		
		if (priceIndex != -1)
		{
			ticket.setPrice(ArgCursor.getDouble(priceIndex));
		}
		
		if (isFullIndex != -1)
		{
			boolean isFull = true;
			int isFullNumber = ArgCursor.getInt(isFullIndex);
			if (isFullNumber == 0)
			{
				isFull = false;
			}
			
			ticket.setFull(isFull);
		}

        if (dateIndex != -1)
        {
            Calendar cal = DateTools.now();
            cal.setTimeInMillis(ArgCursor.getLong(dateIndex));
            ticket.setDate(cal);
        }
		
		return ticket;
	}

	@Override
	public List<Ticket> getAll() {
		List<Ticket> tickets = new ArrayList<Ticket>();
		
		try
		{
			Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, null, null, null, null, IdField);
			while (cursor.moveToNext())
			{
				Ticket ticket = fromCursor(cursor);
				if (ticket != null)
				{
					tickets.add(ticket);
				}
			}
		}
		catch (SQLException sqlEx)
		{
			sqlEx.printStackTrace();
			tickets.clear();
		}
		
		return tickets;
	}



    @Override
    public List<Ticket> getAll(boolean ArgIsFull) {
        List<Ticket> tickets = new ArrayList<Ticket>();

        try
        {
            int isFullNumber = 0;
            if (ArgIsFull)
            {
                isFullNumber = 1;
            }
            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, BASE_SELECT, IsFullField + " = ?", new String[]{String.valueOf(isFullNumber)}, null, null, IdField);
            while (cursor.moveToNext())
            {
                Ticket ticket = fromCursor(cursor);
                if (ticket != null)
                {
                    tickets.add(ticket);
                }
            }
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            tickets.clear();
        }

        return tickets;
    }

	@Override
	public boolean update(Ticket ArgTicket) {
        if (ArgTicket == null || ArgTicket.getId() < 1)
        {
            return false;
        }

        try
        {
            ContentValues cv = new ContentValues();
            cv.put(NameField, ArgTicket.getName());
            cv.put(DescriptionField, ArgTicket.getDescription());
            cv.put(CodeField, ArgTicket.getCode());
            cv.put(PriceField, ArgTicket.getPrice());
            cv.put(IsFullField, ArgTicket.isFull());
            cv.put(DateField, ArgTicket.getDate().getTimeInMillis());

            int numberRowsUpdated = m_dbHelper.getSqlDB().update(TableName, cv, IdField + " = ?", new String[]{String.valueOf(ArgTicket.getId())});
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
