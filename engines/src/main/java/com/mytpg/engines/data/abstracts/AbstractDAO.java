package com.mytpg.engines.data.abstracts;

import java.util.List;

/**
 * Created by stalker-mac on 08.11.14.
 */
public abstract class AbstractDAO<T> {

    /**
     *
     * @return
     */
    public abstract long count();

    /**
     *
     * @param ArgObj
     * @return
     */
    public abstract boolean create(T ArgObj);

    /**
     *
     * @param ArgObjs
     * @return
     */
    public abstract boolean create(List<T> ArgObjs);

    /**
     *
     * @param ArgObj
     * @return
     */
    public abstract boolean delete(T ArgObj);

    /**
     *
     * @return
     */
    public abstract boolean deleteAll();

    /**
     *
     * @param ArgId
     * @return
     */
    public T find(final long ArgId){
        return find(ArgId,true);
    }

    public abstract T find(final long ArgId, final boolean ArgIsComplete);

    /**
     *
     * @param ArgName
     * @return
     */
    public abstract T findByName(final String ArgName);

    /**
     *
     * @return
     */
    public abstract List<T> getAll();

    /**
     *
     * @param ArgObj
     * @return
     */
    public abstract boolean update(T ArgObj);
}