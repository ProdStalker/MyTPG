package com.mytpg.engines.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.Entity;
import com.mytpg.engines.entities.stops.PhysicalStop;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 11.11.14.
 */
public class Connection extends Entity {
    private boolean m_isFavorite = false;
    private Line m_line = new Line();
    private PhysicalStop m_physicalStop = new PhysicalStop();

    /**
     *
     */
    public Connection() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param ArgId
     */
    public Connection(long ArgId) {
        super(ArgId);
        // TODO Auto-generated constructor stub
    }

    protected Connection(Parcel in) {
        super(in);
        m_isFavorite = in.readByte() != 0x00;
        m_line = (Line) in.readValue(Line.class.getClassLoader());
        m_physicalStop = (PhysicalStop) in.readValue(PhysicalStop.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object ArgObj)
    {
        if (this == ArgObj)
            return true;
        if (ArgObj == null)
            return false;
        if (!(ArgObj instanceof Connection))
            return false;
        Connection conn = (Connection) ArgObj;
        if (getId() == conn.getId() && getId() != -1)
        {
            return true;
        }
        if (getLine().equals(conn.getLine()) && getPhysicalStop().equals(conn.getPhysicalStop()))
        {
            return true;
        }
        return super.equals(ArgObj);
    }

    public Line getLine()
    {
        return m_line;
    }

    public PhysicalStop getPhysicalStop()
    {
        return m_physicalStop;
    }

    public boolean isFavorite()
    {
        return m_isFavorite;
    }

    public void setFavorite(boolean argIsFavorite)
    {
        m_isFavorite = argIsFavorite;
    }

    public void setLine(Line ArgLine)
    {
        if (ArgLine == null)
        {
            ArgLine = new Line();
        }

        m_line = ArgLine;
    }

    public void setPhysicalStop(PhysicalStop ArgPhysicalStop)
    {
        if (ArgPhysicalStop == null)
        {
            ArgPhysicalStop = new PhysicalStop();
        }

        m_physicalStop = ArgPhysicalStop;
    }

    /* (non-Javadoc)
     * @see com.mytpg.engines.entities.core.Entity#fromJson(org.json.JSONObject)
     */
    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        String text = super.toString();

        text += "\n line" + getLine().toString();
        text += "\n physical stop : " + getPhysicalStop().toString();

        return text;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeByte((byte) (m_isFavorite ? 0x01 : 0x00));
        dest.writeValue(m_line);
        dest.writeValue(m_physicalStop);
    }

    public static final Parcelable.Creator<Connection> CREATOR = new Parcelable.Creator<Connection>() {
        @Override
        public Connection createFromParcel(Parcel in) {
            return new Connection(in);
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[size];
        }
    };

}