/**
 * 
 */
package com.mytpg.engines.entities.stops;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.core.EntityWithName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class Stop extends EntityWithName {
	public final static String JSON_CONNECTIONS = "connections";
	public final static String JSON_DISTANCE = "distance";
	public final static String JSON_MNEMO = "stopCode";
	public final static String JSON_NAME = "stopName";
    public final static String JSON_PHYSICAL_STOPS = "physicalStops";
	
	private String m_code = "";
    private long m_favoriteNumber = 9999;
	private boolean m_isFavorite = false;
	private boolean m_isFavoriteDetailled = false;
	private boolean m_isVisible = true;
	private String mCFF = "";
	private Mnemo m_mnemo = new Mnemo();
    private List<PhysicalStop> m_physicalStops = new ArrayList<PhysicalStop>();
	
	/**
	 * 
	 */
	public Stop() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgId
	 */
	public Stop(long ArgId) {
		super(ArgId);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgName
	 */
	public Stop(String ArgName) {
		super(ArgName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgId
	 * @param ArgName
	 */
	public Stop(long ArgId, String ArgName) {
		super(ArgId, ArgName);
		// TODO Auto-generated constructor stub
	}
	
	public Stop(Stop ArgStop) {
		setId(ArgStop.getId());
		setName(ArgStop.getName());
        m_physicalStops = new ArrayList<PhysicalStop>(ArgStop.getPhysicalStops());
		m_isFavorite = ArgStop.isFavorite();
		m_isFavoriteDetailled = ArgStop.isFavoriteDetailled();
		setCFF(ArgStop.getCFF());
		m_mnemo = new Mnemo(ArgStop.getMnemo());
        m_favoriteNumber = ArgStop.getFavoriteNumber();
	}

    protected Stop(Parcel in) {
        super(in);
        m_code = in.readString();
        m_favoriteNumber = in.readLong();
        m_isFavorite = in.readByte() != 0x00;
		m_isFavoriteDetailled = in.readByte() != 0x00;
        m_isVisible = in.readByte() != 0x00;
		mCFF = in.readString();
        m_mnemo = (Mnemo) in.readValue(Mnemo.class.getClassLoader());
        if (in.readByte() == 0x01) {
            m_physicalStops = new ArrayList<PhysicalStop>();
            in.readList(m_physicalStops, PhysicalStop.class.getClassLoader());
        } else {
            m_physicalStops = new ArrayList<PhysicalStop>();
        }

    }

	@Override
    public boolean equals(Object ArgObj)
    {
        if (this == ArgObj)
            return true;
        if (ArgObj == null)
            return false;
        if (!(ArgObj instanceof Stop))
            return false;
        Stop stop = (Stop) ArgObj;
        if (getId() == stop.getId() && getId() != -1)
        {
        	return true;
        }
		return getName().equalsIgnoreCase(stop.getName()) ||
				getMnemo().getName().equalsIgnoreCase(stop.getMnemo().getName());
	}
	
	@Override
	public void fromJson(JSONObject ArgJsonObj) {
		if (ArgJsonObj == null)
		{
			return;
		}
		
		if (m_physicalStops == null)
		{
			m_physicalStops = new ArrayList<PhysicalStop>();
		}
		else
		{
			m_physicalStops.clear();
		}
		
		if (m_mnemo == null)
		{
			m_mnemo = new Mnemo();
		}
		
		try {
			
			String name = ArgJsonObj.optString(JSON_NAME);
			getMnemo().setName(ArgJsonObj.optString(JSON_MNEMO));
			if (getMnemo().getName().equalsIgnoreCase("QUOA"))
			{
				name = "Les Quoattes";
			}
			
			setName(name);
			JSONArray physicalStopsJsonArray = ArgJsonObj.optJSONArray(JSON_PHYSICAL_STOPS);

			JSONArray connectionsJsonArray;
			
			if (physicalStopsJsonArray != null)
			{
				List<JSONObject> connectionsJsonObj = new ArrayList<JSONObject>();
				int i = 0;
				while (i < physicalStopsJsonArray.length())
				{
                    JSONObject physicalJsonObj = physicalStopsJsonArray.optJSONObject(i);
                    if (physicalJsonObj != null){
                        PhysicalStop physicalStop = new PhysicalStop();
                        physicalStop.fromJson(physicalJsonObj);
                        m_physicalStops.add(physicalStop);
                    }

					/*JSONArray connJsonArray = physicalStopsJsonArray.optJSONObject(i).optJSONArray(JSON_CONNECTIONS);
					if (connJsonArray != null)
					{
						int j = 0;
						while (j < connJsonArray.length())
						{
							connectionsJsonObj.add(connJsonArray.getJSONObject(j));
							j++;
						}
					}*/
					i++;
				}
				

			}
			else
			{
				connectionsJsonArray = ArgJsonObj.optJSONArray(JSON_CONNECTIONS);
                m_physicalStops.clear();
                if (connectionsJsonArray != null)
                {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put(JSON_CONNECTIONS,connectionsJsonArray);
                    PhysicalStop physicalStop = new PhysicalStop();
                    physicalStop.fromJson(jsonObj);
                    m_physicalStops.add(physicalStop);
                   /* int i = 0;
                    while (i < connectionsJsonArray.length())
                    {
                        JSONObject conn = connectionsJsonArray.optJSONObject(i);
                        if (conn != null){
                            Line line = new Line();
                            line.fromJson(conn);

                            m.add(line);
                        }

                        i++;
                    }*/
                }
			}


			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	public String getCFF()
	{
		return mCFF;
	}

	public String getCode()
	{
		return m_code;
	}

    public List<Line> getConnections(){
        List<Line> connections = new ArrayList<>();

        for (PhysicalStop physicalStop : m_physicalStops){
			/*for (Line physicalConn : physicalStop.getConnections())
			{
				boolean found = false;
				for (Line lineConn : connections) {
					if (lineConn.getName().equals(physicalConn)) {
						found = true;
						break;
					}
				}
				if (!found) {
					connections.add(physicalConn);
				}
			}*/
			connections.addAll(physicalStop.getConnections());
        }

        return connections;
    }

    public long getFavoriteNumber(){
        return m_favoriteNumber;
    }
	
	public Mnemo getMnemo()
	{
		return m_mnemo;
	}

    public List<PhysicalStop> getPhysicalStops(){
        return m_physicalStops;
    }
	
	public boolean isFavorite()
	{
		return m_isFavorite;
	}

	public boolean isFavoriteDetailled()
	{
		return m_isFavoriteDetailled;
	}
	
	public boolean isVisible()
	{
		return m_isVisible;
	}

	public void setCFF(String argCFF)
	{
		mCFF = argCFF;
	}

	public void setCode(String ArgCode)
	{
		m_code = ArgCode;
	}
	
	public void setFavorite(boolean ArgIsFavorite)
	{
		m_isFavorite = ArgIsFavorite;
	}

	public void setFavoriteDetailled(boolean ArgIsFavoriteDetailled)
	{
		m_isFavoriteDetailled = ArgIsFavoriteDetailled;
	}
	
    public void setFavoriteNumber(long ArgFavoriteNumber)
    {
        if (ArgFavoriteNumber < 0)
        {
            ArgFavoriteNumber = 0;
        }

        m_favoriteNumber = ArgFavoriteNumber;
    }
	
	public void setMnemo(Mnemo ArgMnemo)
	{
		if (ArgMnemo == null)
		{
			ArgMnemo = new Mnemo();
		}
		
		m_mnemo = ArgMnemo;
	}
	
	/*@Override
	public void setName(String ArgName)
	{
		super.setName(ArgName);

		if (ArgName.equalsIgnoreCase("Bus Scolaire"))
		{
			setVisible(false);
		}
	}*/

    public void setPhysicalStops(List<PhysicalStop> ArgPhysicalStops){
        if (ArgPhysicalStops == null)
        {
            ArgPhysicalStops = new ArrayList<PhysicalStop>();
        }

        m_physicalStops = ArgPhysicalStops;
    }
	
	public void setVisible(boolean ArgIsVisible)
	{
		m_isVisible = ArgIsVisible;
	}

	@Override
	public String toString() {
		String text = super.toString();

		text += "\n code = " + getCode();
		text += "\n mnemo = " + getMnemo().toString();

		return text;
	}

	/**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(m_code);
        dest.writeLong(m_favoriteNumber);
        dest.writeByte((byte) (m_isFavorite ? 0x01 : 0x00));
		dest.writeByte((byte) (m_isFavoriteDetailled ? 0x01 : 0x00));
        dest.writeByte((byte) (m_isVisible ? 0x01 : 0x00));
		dest.writeString(mCFF);
        dest.writeValue(m_mnemo);
        if (m_physicalStops == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(m_physicalStops);
        }
    }

    public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };
}
