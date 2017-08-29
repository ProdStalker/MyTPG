package com.mytpg.engines.entities.stops;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.core.EntityWithNameAndLocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 13.11.14.
 */
public class PhysicalStop extends EntityWithNameAndLocation {
    public final static String JSON_CONNECTIONS = "connections";
    public final static String JSON_COORDINATES = "coordinates";
    public final static String JSON_LATITUDE = "latitude";
    public final static String JSON_LONGITUDE = "longitude";
    public final static String JSON_PHYSICAL_STOP_CODE = "physicalStopCode";

    private List<Line> m_connections = new ArrayList<Line>();
    private long m_stopId = -1;
    private Marker mMarker = null;

    /**
     *
     */
    public PhysicalStop() {
        super();

    }

    /**
     * @param ArgId
     */
    public PhysicalStop(long ArgId) {
        super(ArgId);
    }

    /**
     * @param ArgName
     */
    public PhysicalStop(String ArgName) {
        super(ArgName);
    }

    /**
     * @param ArgId
     * @param ArgName
     */
    public PhysicalStop(long ArgId, String ArgName) {
        super(ArgId, ArgName);
    }

    public PhysicalStop(PhysicalStop ArgPhysicalStop){
        super(ArgPhysicalStop);
        m_connections = new ArrayList<Line>(ArgPhysicalStop.getConnections());
        setStopId(ArgPhysicalStop.getStopId());
        //setMarker(ArgPhysicalStop.getMarker());
    }

    protected PhysicalStop(Parcel in) {
        super(in);
        if (in.readByte() == 0x01) {
            m_connections = new ArrayList<Line>();
            in.readList(m_connections, Line.class.getClassLoader());
        } else {
            m_connections = new ArrayList<Line>();
        }
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
        if (!(ArgObj instanceof PhysicalStop))
            return false;
        PhysicalStop physicalStop = (PhysicalStop) ArgObj;
        if (getId() == physicalStop.getId() && getId() != -1)
        {
            return true;
        }
        if (getName().equalsIgnoreCase(physicalStop.getName()))
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

        if (m_connections == null){
            m_connections = new ArrayList<Line>();
        }
        else{
            m_connections.clear();
        }

        if (getLocation() == null)
        {
            setLocation(null);
        }

        String code = ArgJsonObj.optString(JSON_PHYSICAL_STOP_CODE,"");
        setName(code);
        JSONObject coordinatesJsonObj = ArgJsonObj.optJSONObject(JSON_COORDINATES);
        if (coordinatesJsonObj != null) {
            getLocation().setLatitude(coordinatesJsonObj.optDouble(JSON_LATITUDE, 0.0));
            getLocation().setLongitude(coordinatesJsonObj.optDouble(JSON_LONGITUDE, 0.0));
        }

        JSONArray connectionsJsonArray = ArgJsonObj.optJSONArray(JSON_CONNECTIONS);
        if (connectionsJsonArray != null)
        {
            int i = 0;
            while (i < connectionsJsonArray.length())
            {
                JSONObject conn = connectionsJsonArray.optJSONObject(i);
                if (conn != null){
                    Line line = new Line();
                    line.fromJson(conn);

                    m_connections.add(line);
                }

                i++;
            }
        }
    }

    public List<Line> getConnections()
    {
        return m_connections;
    }

    public Marker getMarker()
    {
        return mMarker;
    }

    public long getStopId(){
        return m_stopId;
    }

    public void setConnections(List<Line> ArgConnections)
    {
        if (ArgConnections == null)
        {
            ArgConnections = new ArrayList<Line>();
        }

        m_connections = ArgConnections;
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

    @Override
    public String toString() {
        String text = super.toString();

        return text;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        if (m_connections == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(m_connections);
        }
        dest.writeLong(m_stopId);
    }

    public static final Parcelable.Creator<PhysicalStop> CREATOR = new Parcelable.Creator<PhysicalStop>() {
        @Override
        public PhysicalStop createFromParcel(Parcel in) {
            return new PhysicalStop(in);
        }

        @Override
        public PhysicalStop[] newArray(int size) {
            return new PhysicalStop[size];
        }
    };
}
