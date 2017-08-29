/**
 * 
 */
package com.mytpg.engines.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.Entity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author stalker-mac
 *
 */
public class HourDeparture extends Entity {
	String m_hourOfDay = "";
	List<Departure> m_departures = new ArrayList<Departure>();
	
	/**
	 * 
	 */
	public HourDeparture() {
	}

	/**
	 * @param ArgId
	 */
	public HourDeparture(long ArgId) {
		super(ArgId);
	}

    protected HourDeparture(Parcel in) {
        super(in);
        m_hourOfDay = in.readString();
        if (in.readByte() == 0x01) {
            m_departures = new ArrayList<Departure>();
            in.readList(m_departures, Departure.class.getClassLoader());
        } else {
            m_departures = new ArrayList<Departure>();
        }
    }

	public List<Departure> getDepartures()
	{
		return m_departures;
	}
	
	public String getHourOfDay()
	{
		return m_hourOfDay;
	}
	
	public void setDeparture(List<Departure> ArgDepartures)
	{
		if (ArgDepartures == null)
		{
			ArgDepartures = new ArrayList<Departure>();
		}
		
		m_departures = ArgDepartures;
	}
	
	public void setHourOfDay(String ArgHourOfDay)
	{
		m_hourOfDay = ArgHourOfDay;
	}

	/* (non-Javadoc)
	 * @see com.mytpg.engines.entities.core.Entity#fromJson(org.json.JSONObject)
	 */
	@Override
	public void fromJson(JSONObject ArgJsonObj) {
		// TODO Auto-generated method stub

	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(m_hourOfDay);
        if (m_departures == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(m_departures);
        }
    }

    public static final Parcelable.Creator<HourDeparture> CREATOR = new Parcelable.Creator<HourDeparture>() {
        @Override
        public HourDeparture createFromParcel(Parcel in) {
            return new HourDeparture(in);
        }

        @Override
        public HourDeparture[] newArray(int size) {
            return new HourDeparture[size];
        }
    };

	public String toShareText(boolean argIsForMessage) {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%1$s : \t",getHourOfDay()));

		for (Departure dep : getDepartures()) {
			final int Minutes = dep.getDate().get(Calendar.MINUTE);
			String minutesText = String.valueOf(Minutes);
			if (minutesText.length() == 1) {
				minutesText = "0" + minutesText;
			}

			sb.append(String.format(" %1$s", minutesText));
		}

		return sb.toString();
	}
}
