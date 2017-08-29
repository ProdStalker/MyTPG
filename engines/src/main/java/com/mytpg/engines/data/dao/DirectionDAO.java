package com.mytpg.engines.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.directions.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 24.10.16.
 */

public class DirectionDAO extends DAO<Direction> {
    public static final String IdField = "pkDirection";
    public static final String DateField = "date";
    public static final String FromField = "ffrom";
    public static final String IsDepartureField = "isDeparture";
    public static final String ToField = "tto";

    public static final String TableName = "tbldirections";
    public static final String TABLE_SCRIPT = "CREATE TABLE " + TableName +
            "(" + IdField + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DateField + " INTEGER NOT NULL DEFAULT 0, " +
            FromField + " TEXT NOT NULL collate nocase, " +
            ToField + " TEXT NOT NULL collate nocase, " +
            IsDepartureField + " INTEGER NOT NULL DEFAULT 1);";

    /**
     * @param ArgDBHelper
     */
    public DirectionDAO(DatabaseHelper ArgDBHelper) {
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
    public boolean create(Direction ArgDirection) {
        try
        {
            ContentValues cv = new ContentValues();
            cv.put(DateField, ArgDirection.getDate().getTimeInMillis());
            cv.put(FromField, ArgDirection.getFrom());
            cv.put(ToField, ArgDirection.getTo());
            cv.put(IsDepartureField, ArgDirection.isDeparture());

            long id = m_dbHelper.getSqlDB().insert(TableName, null, cv);
            if (id == -1)
            {
                return false;
            }

            ArgDirection.setId(id);
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean create(List<Direction> ArgObjs) {
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
    public boolean delete(Direction ArgDirection) {
        boolean isOk = false;
        if (ArgDirection == null || ArgDirection.getId() < 1)
        {
            return isOk;
        }

        try
        {
            m_dbHelper.getSqlDB().beginTransaction();

            int numberRowsDeleted = m_dbHelper.getSqlDB().delete(TableName, IdField + " = ?", new String[]{String.valueOf(ArgDirection.getId())});
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
    public Direction find(long ArgId, boolean ArgIsComplete) {
        Direction direction = null;

        try
        {
            String[] finalSelect = new String[]{IdField,DateField,FromField,ToField,IsDepartureField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, IdField + " = ? ",new String[]{String.valueOf(ArgId)},null,null,IdField + " LIMIT 1");
            if (cursor.moveToFirst())
            {
                direction = fromCursor(cursor,ArgIsComplete);
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            direction = null;
        }

        return direction;
    }

    @Override
    public Direction findByName(String ArgName) {
        return null;
    }

    @Override
    protected Direction fromCursor(Cursor ArgCursor, boolean ArgIsComplete) {
        if (ArgCursor.getColumnCount() == 0) {
            return null;
        }

        int idIndex = ArgCursor.getColumnIndex(IdField);
        int dateIndex = ArgCursor.getColumnIndex(DateField);
        int fromIndex = ArgCursor.getColumnIndex(FromField);
        int toIndex = ArgCursor.getColumnIndex(ToField);
        int isDepartureIndex = ArgCursor.getColumnIndex(IsDepartureField);

        Direction direction = new Direction();

        if (idIndex != -1) {
            direction.setId(ArgCursor.getLong(idIndex));
        }

        if (dateIndex != -1)
        {
            long millis = ArgCursor.getLong(dateIndex);
            direction.getDate().setTimeInMillis(millis);
        }

        if (fromIndex != -1) {
            direction.setFrom(ArgCursor.getString(fromIndex));
        }

        if (toIndex != -1)
        {
            direction.setTo(ArgCursor.getString(toIndex));
        }

        if (isDepartureIndex != -1)
        {
            direction.setDeparture(ArgCursor.getInt(isDepartureIndex) != 0);
        }
        
        return direction;
    }

    @Override
    public List<Direction> getAll() {
        List<Direction> directions = new ArrayList<>();

        try
        {
            String[] finalSelect = new String[]{IdField,DateField,FromField,ToField,IsDepartureField};

            Cursor cursor = m_dbHelper.getSqlDB().query(TableName, finalSelect, null,null,null,null,IdField);
            while (cursor.moveToNext())
            {
                Direction direction = fromCursor(cursor);
                if (direction != null)
                {
                    directions.add(direction);
                }
            }

            cursor.close();
        }
        catch (SQLException sqlEx)
        {
            sqlEx.printStackTrace();
            directions.clear();
        }

        return directions;
    }

    @Override
    public boolean update(Direction ArgDirection) {
        if (ArgDirection == null || ArgDirection.getId() < 1)
        {
            return false;
        }

        try
        {
            ContentValues cv = new ContentValues();
            cv.put(DateField, ArgDirection.getDate().getTimeInMillis());
            cv.put(FromField, ArgDirection.getFrom());
            cv.put(ToField, ArgDirection.getTo());
            cv.put(IsDepartureField, ArgDirection.isDeparture());

            int numberRowsUpdated = m_dbHelper.getSqlDB().update(TableName, cv, IdField + " = ?", new String[]{String.valueOf(ArgDirection.getId())});
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
