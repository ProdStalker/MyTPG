package com.mytpg.engines.entities.core;

/**
 * Created by stalker-mac on 16.08.16.
 */

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 08.11.14.
 */
public abstract class Entity extends Object implements Parcelable {
    private long m_id = -1;

    /**
     *
     */
    public Entity() {
        super();
    }

    /**
     *
     * @param ArgId
     */
    public Entity(long ArgId){
        super();
        setId(ArgId);
    }

    @Override
    public String toString() {
        String text = super.toString();

        text += "\n id:" + String.valueOf(getId());

        return text;
    }

    public Entity(Entity argEntity)
    {
        super();
        setId(argEntity.getId());
    }

    protected Entity(Parcel in) {
        m_id = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public abstract void fromJson(JSONObject ArgJsonObj);

    /**
     *
     * @return
     */
    public long getId(){
        return m_id;
    }

    /**
     *
     * @param ArgId
     */
    public void setId(long ArgId){
        if (ArgId < 0)
        {
            ArgId = -1;
        }

        m_id = ArgId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(m_id);
    }

}
