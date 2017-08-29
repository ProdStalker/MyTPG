package com.mytpg.engines.entities.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mytpg.engines.data.dao.ConnectionDAO;
import com.mytpg.engines.data.dao.DepartureAlarmDAO;
import com.mytpg.engines.data.dao.DepartureDAO;
import com.mytpg.engines.data.dao.DirectionDAO;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.MnemoDAO;
import com.mytpg.engines.data.dao.PhysicalStopDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.dao.TicketDAO;
import com.mytpg.engines.data.dao.bustedapp.BustedStopDAO;

/**
 * Created by stalker-mac on 08.11.14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper ms_instance = null;
    public static final int DB_VERSION = 8;
    //private static final String DB_PATH = "";
    private static final String DB_NAME = "mytpg.db";
    private SQLiteDatabase m_sqlDB = null;

    //private static Context ms_context;

    /**
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getInstance(Context context)
    {
        if (ms_instance == null)
        {
           // ms_context = context.getApplicationContext();
            ms_instance = new DatabaseHelper(context.getApplicationContext());

			/*Cursor cur2 = ms_instance.getSqlDB().rawQuery("select name from sqlite_master where type = 'table'",null);
			while (cur2.moveToNext())
			{
				Log.d("TABLE",cur2.getString(cur2.getColumnIndex("name")));
			}*/
        }


        return ms_instance;
    }

    /**
     *
     * @param context
     */
    private DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);

        openDB(SQLiteDatabase.OPEN_READWRITE);

    }

    /**
     *
     * @return
     */
    public SQLiteDatabase getSqlDB()
    {
        if (m_sqlDB == null || !m_sqlDB.isOpen())
        {
            openDB(SQLiteDatabase.OPEN_READWRITE);
        }
        return this.m_sqlDB;
    }

    /**
     *
     */
    @Override
    public synchronized void close()
    {
        if (m_sqlDB != null && m_sqlDB.isOpen())
        {
            m_sqlDB.close();
        }

        super.close();
    }

    /**
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        this.m_sqlDB = db;

        new BustedStopDAO(this).createTable();
        new ConnectionDAO(this).createTable();
        new DepartureAlarmDAO(this).createTable();
        new DepartureDAO(this).createTable();
        new DirectionDAO(this).createTable();
        new LineDAO(this).createTable();
        new MnemoDAO(this).createTable();
        new PhysicalStopDAO(this).createTable();
        new StopDAO(this).createTable();
        new TicketDAO(this).createTable();

    }

    /**
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion)
        {
            //return;
        }

        String query;
        int currentVersion = oldVersion + 1;
        while (currentVersion <= newVersion)
        {
            switch (currentVersion)
            {
                case 4 :

                    db.execSQL("DROP TABLE IF EXISTS " + ConnectionDAO.TableName);
                    db.execSQL("DROP TABLE IF EXISTS " + DepartureAlarmDAO.TableName);
                    db.execSQL("DROP TABLE IF EXISTS " + DepartureDAO.TableName);
                    db.execSQL("DROP TABLE IF EXISTS " + LineDAO.TableName);
                    db.execSQL("DROP TABLE IF EXISTS " + MnemoDAO.TableName);
                    db.execSQL("DROP TABLE IF EXISTS " + PhysicalStopDAO.TableName);
                    db.execSQL("DROP TABLE IF EXISTS " + StopDAO.TableName);
                    db.execSQL("DROP TABLE IF EXISTS " + TicketDAO.TableName);

                    db.execSQL(ConnectionDAO.TABLE_SCRIPT);
                    db.execSQL(DepartureAlarmDAO.TABLE_SCRIPT);
                    db.execSQL(DepartureDAO.TABLE_SCRIPT);
                    db.execSQL(LineDAO.TABLE_SCRIPT);
                    db.execSQL(MnemoDAO.TABLE_SCRIPT);
                    db.execSQL(PhysicalStopDAO.TABLE_SCRIPT);
                    db.execSQL(StopDAO.TABLE_SCRIPT);
                    db.execSQL(TicketDAO.TABLE_SCRIPT);
                    //db.execSQL("ALTER TABLE " + CMLocationDAO.TableName + " ADD COLUMN " + CMLocationDAO.AccuracyField + " DOUBLE NOT NULL DEFAULT 0.0");

                break;
                case 5 :
                    query = String.format("ALTER TABLE %1$s ADD COLUMN %2$s TEXT NOT NULL DEFAULT \"\"", StopDAO.TableName, StopDAO.CFFField);
                    db.execSQL(query);
                    db.execSQL(DirectionDAO.TABLE_SCRIPT);
                break;

                case 6 :
                    query = String.format("ALTER TABLE %1$s ADD COLUMN %2$s TEXT NOT NULL DEFAULT \"\"", DepartureAlarmDAO.TableName, DepartureAlarmDAO.MinutesField);
                    db.execSQL(query);
                break;

                case 7 :
                    query = String.format("ALTER TABLE %1$s ADD COLUMN %2$s INTEGER NOT NULL DEFAULT 0", ConnectionDAO.TableName, ConnectionDAO.IsFavoriteField);
                    db.execSQL(query);
                    query = String.format("ALTER TABLE %1$s ADD COLUMN %2$s INTEGER NOT NULL DEFAULT 0", StopDAO.TableName, StopDAO.IsFavoriteDetailledField);
                    db.execSQL(query);
                break;

                case 8 :
                    db.execSQL(BustedStopDAO.TABLE_SCRIPT);
                break;
            }
            currentVersion++;
        }
        //this.onCreate(db);
    }

    /**
     *
     * @param ArgMode
     */
    public void openDB(final int ArgMode)
    {
        //String dbPath = DB_PATH + DB_NAME;
        if (m_sqlDB == null || !m_sqlDB.isOpen())
        {
            m_sqlDB = this.getWritableDatabase();
        }
    }

}
