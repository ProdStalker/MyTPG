/**
 * 
 */
package com.mytpg.engines.entities;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.EntityWithName;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.entities.vehicles.core.Vehicle;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class Line extends EntityWithName {
	public static final String JSON_ARRIVAL_CODE = "destinationCode";
	public static final String JSON_ARRIVAL_NAME = "destinationName";
	public static final String JSON_COLOR = "hexa";
	public static final String JSON_LINE_CODE = "lineCode";
	public static final String JSON_NAME = "lineCode";
	
	private Stop m_arrivalStop = new Stop();
	private List<CheckPoint> m_checkPoints = new ArrayList<CheckPoint>();
	private int m_color = Color.parseColor("#FFFFFF");
	private Stop m_departureStop = new Stop();
	private Vehicle m_vehicle = new Vehicle();
	
	/**
	 * 
	 */
	public Line() {
	}

	/**
	 * @param ArgId
	 */
	public Line(long ArgId) {
		super(ArgId);
	}

	/**
	 * @param ArgName
	 */
	public Line(String ArgName) {
		super(ArgName);
	}

	/**
	 * @param ArgId
	 * @param ArgName
	 */
	public Line(long ArgId, String ArgName) {
		super(ArgId, ArgName);
	}
	
	public Line(Line ArgLine) {
		setArrivalStop(new Stop(ArgLine.getArrivalStop()));
		setCheckPoints(new ArrayList<CheckPoint>(ArgLine.getCheckPoints()));
		setColor(ArgLine.getColor());
		setDepartureStop(new Stop(ArgLine.getDepartureStop()));
		setId(ArgLine.getId());
		setName(ArgLine.getName());
		setVehicle(new Vehicle(ArgLine.getVehicle()));
	}

    protected Line(Parcel in) {
        super(in);
        m_arrivalStop = (Stop) in.readValue(Stop.class.getClassLoader());
        if (in.readByte() == 0x01) {
            m_checkPoints = new ArrayList<CheckPoint>();
            in.readList(m_checkPoints, CheckPoint.class.getClassLoader());
        } else {
            m_checkPoints = new ArrayList<CheckPoint>();
        }
        m_color = in.readInt();
        m_departureStop = (Stop) in.readValue(Stop.class.getClassLoader());
        m_vehicle = (Vehicle) in.readValue(Vehicle.class.getClassLoader());
    }
	
	@Override
    public boolean equals(Object ArgObj)
    {
        if (this == ArgObj)
            return true;
        if (ArgObj == null)
            return false;
        if (!(ArgObj instanceof Line))
            return false;
        Line line = (Line) ArgObj;
        if (getId() == line.getId() && getId() != -1)
        {
        	return true;
        }
        if (getName().equalsIgnoreCase(line.getName()) && getArrivalStop().equals(line.getArrivalStop()))
    	{
        	return true;
    	}
        return super.equals(ArgObj);
    }

	public Stop getArrivalStop()
	{
		return m_arrivalStop;
	}
	
	public List<CheckPoint> getCheckPoints()
	{
		return m_checkPoints;
	}
	
	public int getColor()
	{
		return m_color;
	}
	
	public Stop getDepartureStop()
	{
		return m_departureStop;
	}
	
	public Vehicle getVehicle()
	{
		return m_vehicle;
	}
	
	public void setArrivalStop(Stop ArgStop)
	{
		if (ArgStop == null)
		{
			ArgStop = new Stop();
		}
		
		m_arrivalStop = ArgStop;
	}
	
	public void setCheckPoints(List<CheckPoint> ArgCheckPoints)
	{
		if (ArgCheckPoints == null)
		{
			ArgCheckPoints = new ArrayList<CheckPoint>();
		}
		m_checkPoints = ArgCheckPoints;
	}
	
	public void setColor(int ArgColor)
	{
		m_color = ArgColor;
	}
	
	public void setColor(String ArgColorString)
	{
		try
		{
			if (ArgColorString.isEmpty())
			{
				ArgColorString = "#FFFFFF";
			}
			else
			{
				ArgColorString = "#" + ArgColorString;
			}
			setColor(Color.parseColor(ArgColorString));
		}
		catch (IllegalArgumentException iae)
		{
			iae.printStackTrace();
			setColor(Color.parseColor("#FFFFFF"));
		}
	}
	
	public void setDepartureStop(Stop ArgStop)
	{
		if (ArgStop == null)
		{
			ArgStop = new Stop();
		}
		
		m_departureStop = ArgStop;
	}
	
	public void setVehicle(Vehicle ArgVehicle)
	{
		m_vehicle = ArgVehicle;
	}

	@Override
	public void fromJson(JSONObject ArgJsonObj) {
		if (ArgJsonObj == null)
		{
			return;
		}
		if (m_arrivalStop == null)
		{
			m_arrivalStop = new Stop();
		}
		if (m_checkPoints == null)
		{
			m_checkPoints = new ArrayList<CheckPoint>();
		}
		if (m_departureStop == null)
		{
			m_departureStop = new Stop();
		}
		if (m_vehicle == null)
		{
			m_vehicle = new Vehicle();
		}
		

		setName(ArgJsonObj.optString(JSON_NAME));
		if (getName().isEmpty())
		{
			setName(ArgJsonObj.optString(JSON_LINE_CODE));
		}

		getArrivalStop().setName(ArgJsonObj.optString(JSON_ARRIVAL_NAME));
		getArrivalStop().setCode(ArgJsonObj.optString(JSON_ARRIVAL_CODE));
		getArrivalStop().getMnemo().setName(ArgJsonObj.optString(JSON_ARRIVAL_CODE));
		
		setColor(ArgJsonObj.optString(JSON_COLOR));
	}

	@Override
	public String toString() {
		String text = super.toString();

		text += "\n" + String.valueOf(getColor());
		text += "\n departure : " + getDepartureStop().toString();
		text += "\n arrival : " + getArrivalStop().toString();

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
        dest.writeValue(m_arrivalStop);
        if (m_checkPoints == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(m_checkPoints);
        }
        dest.writeInt(m_color);
        dest.writeValue(m_departureStop);
        dest.writeValue(m_vehicle);
    }

    public static final Parcelable.Creator<Line> CREATOR = new Parcelable.Creator<Line>() {
        @Override
        public Line createFromParcel(Parcel in) {
            return new Line(in);
        }

        @Override
        public Line[] newArray(int size) {
            return new Line[size];
        }
    };
}
