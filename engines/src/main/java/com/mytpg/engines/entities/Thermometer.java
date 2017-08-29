/**
 * 
 */
package com.mytpg.engines.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.Entity;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class Thermometer extends Entity {
	public static final String JSON_CHECKPOINTS = "steps";
	public static final String JSON_DATE = "timestamp";
	public static final String JSON_DISRUPTIONS = "disruptions";
	public static final String JSON_STOP = "stop";
	
	private List<CheckPoint> m_checkPoints = new ArrayList<>();
	private Calendar m_date = Calendar.getInstance();
	private List<Disruption> m_disruptions = new ArrayList<>();
	private Line m_line = new Line();
	private Stop m_stop = new Stop();
	
	/**
	 * 
	 */
	public Thermometer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgId
	 */
	public Thermometer(long ArgId) {
		super(ArgId);
		// TODO Auto-generated constructor stub
	}

    protected Thermometer(Parcel in) {
        super(in);
        if (in.readByte() == 0x01) {
            m_checkPoints = new ArrayList<CheckPoint>();
            in.readList(m_checkPoints, CheckPoint.class.getClassLoader());
        } else {
            m_checkPoints = null;
        }
        m_date = DateTools.now();
        m_date.setTimeInMillis(in.readLong());
        m_line = (Line) in.readValue(Line.class.getClassLoader());
        m_stop = (Stop) in.readValue(Stop.class.getClassLoader());
		if (in.readByte() == 0x01)
		{
			m_disruptions = new ArrayList<>();
			in.readList(m_disruptions, Disruption.class.getClassLoader());
		}
    }

    @Override
    public int describeContents() {
        return 0;
    }


	/* (non-Javadoc)
	 * @see com.otpg.engines.entities.core.Entity#fromJson(org.json.JSONObject)
	 */
	@Override
	public void fromJson(JSONObject ArgJsonObj) {
		if (ArgJsonObj == null)
		{
			return;
		}
		if (m_checkPoints == null)
		{
			m_checkPoints = new ArrayList<>();
		}
		if (m_disruptions == null)
		{
			m_disruptions = new ArrayList<>();
		}
		if (m_line == null)
		{
			m_line = new Line();
		}
		if (m_stop == null)
		{
			m_stop = new Stop();
		}
	
		setDate(DateTools.dateAPIToLocaleDate(ArgJsonObj.optString(JSON_DATE)));
		
		JSONObject lineObject = new JSONObject();
		try {
			lineObject.put(Line.JSON_ARRIVAL_CODE, ArgJsonObj.optString(Line.JSON_ARRIVAL_CODE));
			lineObject.put(Line.JSON_ARRIVAL_NAME, ArgJsonObj.optString(Line.JSON_ARRIVAL_NAME));
			lineObject.put(Line.JSON_LINE_CODE, ArgJsonObj.optString(Line.JSON_LINE_CODE));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getLine().fromJson(lineObject);
	
		getStop().fromJson(ArgJsonObj.optJSONObject(JSON_STOP));
		
		JSONArray checkPointsJSONArray = ArgJsonObj.optJSONArray(JSON_CHECKPOINTS);
		if (checkPointsJSONArray != null)
		{
			int i = 0;
			while (i < checkPointsJSONArray.length())
			{
				CheckPoint checkPoint = new CheckPoint();
				checkPoint.fromJson(checkPointsJSONArray.optJSONObject(i));
				checkPoint.setLine(new Line(getLine()));
				m_checkPoints.add(checkPoint);
				i++;
			}
		}

		JSONArray disruptionsJSONArray = ArgJsonObj.optJSONArray(JSON_DISRUPTIONS);
		if (disruptionsJSONArray != null)
		{
			int i = 0;
			while (i < disruptionsJSONArray.length())
			{
				Disruption disruption = new Disruption();
				disruption.fromJson(disruptionsJSONArray.optJSONObject(i));
				disruption.setLine(new Line(getLine()));
				m_disruptions.add(disruption);
				i++;
			}
		}
		
	}
	
	public List<CheckPoint> getCheckPoints()
	{
		return m_checkPoints;
	}

	public CheckPoint getCheckPointByCode(int argDepartureCode)
	{
		if (m_checkPoints == null || m_checkPoints.size() == 0)
		{
			return null;
		}

		for (CheckPoint checkPoint : m_checkPoints)
		{
			if (checkPoint.getCode() == argDepartureCode)
			{
				return checkPoint;
			}
		}

		return null;
	}

	public CheckPoint getActualBusCheckPoint()
	{
		if (m_checkPoints == null || m_checkPoints.size() == 0)
		{
			return null;
		}

		for (int i = 0; i < m_checkPoints.size(); i++)
		{
			if (m_checkPoints.get(i).getArrivalTime() > -1)
			{
				if (i > 0)
				{
					return m_checkPoints.get(i-1);
				}

				return m_checkPoints.get(0);
			}
		}

		return null;
	}
	
	public Calendar getDate()
	{
		return m_date;
	}

	public List<Disruption> getDisruptions()
	{
		return m_disruptions;
	}

	public Line getLine()
	{
		return m_line;
	}
	
	public Stop getStop()
	{
		return m_stop;
	}
	
	public void setCheckPoints(List<CheckPoint> ArgCheckPoints)
	{
		if (ArgCheckPoints == null)
		{
			ArgCheckPoints = new ArrayList<CheckPoint>();
		}
		
		m_checkPoints = ArgCheckPoints;
	}
	
	public void setDate(Calendar ArgDate)
	{
		if (ArgDate == null)
		{
			ArgDate = Calendar.getInstance();
		}
		
		m_date = ArgDate;
	}

	public void setDisruptions(List<Disruption> argDisruptions)
	{
		if (argDisruptions == null)
		{
			argDisruptions = new ArrayList<>();
		}

		this.m_disruptions = argDisruptions;
	}
	
	public void setLine(Line ArgLine)
	{
		if (ArgLine == null)
		{
			ArgLine = new Line();
		}
		
		m_line = ArgLine;
	}
	
	public void setStop(Stop ArgStop)
	{
		if (ArgStop == null)
		{
			ArgStop = new Stop();
		}
		
		m_stop = ArgStop;
	}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        if (m_checkPoints == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(m_checkPoints);
        }
        dest.writeLong(m_date.getTimeInMillis());
        dest.writeValue(m_line);
        dest.writeValue(m_stop);
		if (m_disruptions == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(m_disruptions);
		}
    }

    public static final Parcelable.Creator<Thermometer> CREATOR = new Parcelable.Creator<Thermometer>() {
        @Override
        public Thermometer createFromParcel(Parcel in) {
            return new Thermometer(in);
        }

        @Override
        public Thermometer[] newArray(int size) {
            return new Thermometer[size];
        }
    };
}
