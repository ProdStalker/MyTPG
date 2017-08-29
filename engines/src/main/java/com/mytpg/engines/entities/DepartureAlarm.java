/**
 * 
 */
package com.mytpg.engines.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.Entity;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.AlarmSettings;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author StalkerA
 *
 */
public class DepartureAlarm extends Entity {
	private Calendar m_date = DateTools.now();
	private int m_departureCode = -1;
	private Line m_line = new Line();
	private Calendar m_newDate = DateTools.now();
	private Stop m_stop = new Stop();
	private List<Disruption> m_disruptions = new ArrayList<>();
	private int mMinutes = AlarmSettings.MINIMUM_MINUTES_ALARM;

	/**
	 * 
	 */
	public DepartureAlarm() {
		setNewDate(null);
	}

	/**
	 * @param ArgId
	 */
	public DepartureAlarm(long ArgId) {
		super(ArgId);
		setNewDate(null);
	}

    protected DepartureAlarm(Parcel in) {
        super(in);
        m_date = DateTools.now();
        m_date.setTimeInMillis(in.readLong());
        m_departureCode = in.readInt();
		mMinutes = in.readInt();
        m_line = (Line) in.readValue(Line.class.getClassLoader());
		m_newDate = DateTools.now();
		m_newDate.setTimeInMillis(in.readLong());
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

	public Calendar getDate()
	{
		return m_date;
	}

	public int getDepartureCode()
	{
		return m_departureCode;
	}

	public List<Disruption> getDisruptions()
	{
		return m_disruptions;
	}
	
	public Line getLine()
	{
		return m_line;
	}

	public int getMinutes()
	{
		return mMinutes;
	}

	public Calendar getNewDate()
	{
		return m_newDate;
	}
	
	public Stop getStop()
	{
		return m_stop;
	}

	public void setDate(Calendar ArgDate)
	{
		if (ArgDate == null)
		{
			ArgDate = DateTools.now();
			ArgDate.setTimeInMillis(0);
		}
		
		m_date = ArgDate;
	}
	public void setDepartureCode(int ArgDepartureCode)
	{
		m_departureCode = ArgDepartureCode;
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

	public void setMinutes(int argMinutes) {
		if (argMinutes < AlarmSettings.MINIMUM_MINUTES_ALARM)
		{
			argMinutes = AlarmSettings.MINIMUM_MINUTES_ALARM;
		}

		this.mMinutes = argMinutes;
	}

	public void setNewDate(Calendar ArgNewDate)
	{
		if (ArgNewDate == null)
		{
			ArgNewDate = DateTools.now();
			ArgNewDate.setTimeInMillis(0);
		}

		m_newDate = ArgNewDate;
	}
	
	public void setStop(Stop ArgStop)
	{
		if (ArgStop == null)
		{
			ArgStop = new Stop();
		}
		
		m_stop = ArgStop;
	}


	/* (non-Javadoc)
	 * @see com.mytpg.engines.entities.core.Entity#fromJson(org.json.JSONObject)
	 */
	@Override
	public void fromJson(JSONObject ArgJsonObj) {
		// TODO Auto-generated method stub

	}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeLong(m_date.getTimeInMillis());
        dest.writeInt(m_departureCode);
		dest.writeInt(mMinutes);
        dest.writeValue(m_line);
		dest.writeLong(m_newDate.getTimeInMillis());
        dest.writeValue(m_stop);
		if (m_disruptions == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(m_disruptions);
		}
    }

    public static final Parcelable.Creator<DepartureAlarm> CREATOR = new Parcelable.Creator<DepartureAlarm>() {
        @Override
        public DepartureAlarm createFromParcel(Parcel in) {
            return new DepartureAlarm(in);
        }

        @Override
        public DepartureAlarm[] newArray(int size) {
            return new DepartureAlarm[size];
        }
    };
}
