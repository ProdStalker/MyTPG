package com.mytpg.engines.entities.bustedapp;

import android.os.Parcel;

import com.google.android.gms.maps.model.Marker;
import com.mytpg.engines.entities.core.EntityWithNameAndLocation;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 13.11.14.
 */
public class BustedStop extends EntityWithNameAndLocation {
    public static final String JSON_BACKGROUND_COLOR = "bgCol";
    public static final String JSON_DESTINATION = "dest";
    public static final String JSON_LATITUDE = "lat";
    public static final String JSON_LINE = "line";
    public static final String JSON_LONGITUDE = "lng";
    public static final String JSON_NAME = "name";
    public static final String JSON_TEXT_COLOR = "txtCol";

    private long m_stopId = -1;
    private String m_line = "";
    private String m_destination = "";
    private String m_backgroundColor = "";
    private String m_textColor = "";
    private Marker mMarker = null;

    /**
     *
     */
    public BustedStop() {
        super();

    }

    /**
     * @param ArgId
     */
    public BustedStop(long ArgId) {
        super(ArgId);
    }

    /**
     * @param ArgName
     */
    public BustedStop(String ArgName) {
        super(ArgName);
    }

    /**
     * @param ArgId
     * @param ArgName
     */
    public BustedStop(long ArgId, String ArgName) {
        super(ArgId, ArgName);
    }

    public BustedStop(BustedStop ArgBustedStop){
        super(ArgBustedStop);
        setStopId(ArgBustedStop.getStopId());
        //setMarker(ArgPhysicalStop.getMarker());
    }

    protected BustedStop(Parcel in) {
        super(in);
        m_stopId = in.readLong();
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
        if (!(ArgObj instanceof BustedStop))
            return false;
        BustedStop bustedStop = (BustedStop) ArgObj;
        if (getId() == bustedStop.getId() && getId() != -1)
        {
            return true;
        }
        if (getName().equalsIgnoreCase(bustedStop.getName()))
        {
            return true;
        }
        return super.equals(ArgObj);
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null)
        {
            return;
        }

        if (getLocation() == null)
        {
            setLocation(null);
        }

        setName(ArgJsonObj.optString(JSON_NAME,""));
        setLine(ArgJsonObj.optString(JSON_LINE));
        setDestination(ArgJsonObj.optString(JSON_DESTINATION));
        setTextColor(ArgJsonObj.optString(JSON_TEXT_COLOR));
        setBackgroundColor(ArgJsonObj.optString(JSON_BACKGROUND_COLOR));

        getLocation().setLatitude(ArgJsonObj.optDouble(JSON_LATITUDE, 0.0));
        getLocation().setLongitude(ArgJsonObj.optDouble(JSON_LONGITUDE, 0.0));

    }

    public String getBackgroundColor()
    {
        return m_backgroundColor;
    }

    public String getDestination()
    {
        return m_destination;
    }

    public String getLine()
    {
        return m_line;
    }

    public Marker getMarker()
    {
        return mMarker;
    }

    public long getStopId(){
        return m_stopId;
    }

    public String getTextColor()
    {
        return m_textColor;
    }

    public void setBackgroundColor(String ArgBackgroundColor)
    {
        if (ArgBackgroundColor == null)
        {
            ArgBackgroundColor = "";
        }

        m_backgroundColor = ArgBackgroundColor;
    }

    public void setDestination(String ArgDestination)
    {
        if (ArgDestination == null)
        {
            ArgDestination = "";
        }

        m_destination = ArgDestination;
    }

    public void setLine(String ArgLine)
    {
        if (ArgLine == null)
        {
            ArgLine = "";
        }

        m_line = ArgLine;
    }

    public void setMarker(Marker argMarker)
    {
        mMarker = argMarker;
    }

    public void setStopId(long ArgStopId){
        if (ArgStopId < 1)
        {
            ArgStopId = -1;
        }

        m_stopId = ArgStopId;
    }

    public void setTextColor(String ArgTextColor)
    {
        if (ArgTextColor == null)
        {
            ArgTextColor = "";
        }

        m_textColor = ArgTextColor;
    }

    @Override
    public String toString() {
        String text = super.toString();

        return text;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeLong(m_stopId);
    }

    public static final Creator<BustedStop> CREATOR = new Creator<BustedStop>() {
        @Override
        public BustedStop createFromParcel(Parcel in) {
            return new BustedStop(in);
        }

        @Override
        public BustedStop[] newArray(int size) {
            return new BustedStop[size];
        }
    };
}
