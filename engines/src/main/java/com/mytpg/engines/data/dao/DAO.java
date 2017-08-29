package com.mytpg.engines.data.dao;

import android.database.Cursor;

import com.mytpg.engines.data.abstracts.AbstractDAO;
import com.mytpg.engines.entities.db.DatabaseHelper;

import java.util.List;


/**
 * Created by stalker-mac on 08.11.14.
 */
public abstract class DAO<T> extends AbstractDAO<T> {
    protected DatabaseHelper m_dbHelper = null;

    /**
     *
     * @param ArgDBHelper
     */
    public DAO(final DatabaseHelper ArgDBHelper)
    {
        this.m_dbHelper = ArgDBHelper;
    }

    /**
     *
     */
    public void closeDB() {
        if (m_dbHelper != null)
        {
            m_dbHelper.close();
        }
    }

    /**
     * @return
     */
    @Override
    public abstract long count();

    /**
     * @param ArgObj
     * @return
     */
    @Override
    public abstract boolean create(T ArgObj);

    /**
     * @param ArgObjs
     * @return
     */
    @Override
    public abstract boolean create(List<T> ArgObjs);

    /**
     *
     */
    public abstract void createTable();

    /**
     * @param ArgObj
     * @return
     */
    @Override
    public abstract boolean delete(T ArgObj);

    /**
     * @return
     */
    @Override
    public abstract boolean deleteAll();

    /**
     * @param ArgId
     * @return
     */
    @Override
    public abstract T find(final long ArgId,final boolean ArgIsComplete);

    /**
     * @param ArgName
     * @return
     */
    @Override
    public abstract T findByName(String ArgName);

    protected T fromCursor(Cursor ArgCursor)
    {
        return fromCursor(ArgCursor,true);
    }

    protected abstract T fromCursor(Cursor ArgCursor, boolean ArgIsComplete);

    /**
     * @return
     */
    @Override
    public abstract List<T> getAll();

    /**
     *
     * @return
     */
    public DatabaseHelper getDB()
    {
        return this.m_dbHelper;
    }

    /**
     * @param ArgObj
     * @return
     */
    @Override
    public abstract boolean update(T ArgObj);
}
