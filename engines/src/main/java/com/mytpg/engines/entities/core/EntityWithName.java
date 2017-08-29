package com.mytpg.engines.entities.core;

import android.os.Parcel;

/**
 * Created by stalker-mac on 08.11.14.
 */
public abstract class EntityWithName extends Entity {
    private String m_name = "";

    /**
     *
     */
    public EntityWithName() {

        super();
    }

    /**
     *
     * @param ArgId
     */
    public EntityWithName(long ArgId) {

        super(ArgId);
    }

    /**
     *
     * @param ArgName
     */
    public EntityWithName(String ArgName){
        super();
        setName(ArgName);
    }

    /**
     *
     * @param ArgId
     * @param ArgName
     */
    public EntityWithName(long ArgId, String ArgName){
        super(ArgId);
        setName(ArgName);
    }

    @Override
    public String toString() {
        String text = super.toString();

        text += "\n name:" + getName();

        return text;
    }

    public EntityWithName(EntityWithName argEntityWithName)
    {
        super(argEntityWithName);
        setName(argEntityWithName.getName());
    }

    protected EntityWithName(Parcel in) {
        super(in);
        m_name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     *
     * @param ArgName
     */
    public void setName(String ArgName) {
        if (ArgName == null || ArgName.equalsIgnoreCase("null"))
        {
            ArgName = "";
        }
        else
        {
            ArgName = ArgName.trim();
        }
        this.m_name = ArgName;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(m_name);
    }


}
