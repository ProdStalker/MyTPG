/**
 * 
 */
package com.mytpg.engines.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.EntityWithName;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * @author stalker-mac
 *
 */
public class Disruption extends EntityWithName {
	public static final String JSON_CODE = "disruptionCode";
	public static final String JSON_CONSEQUENCE = "consequence";
	public static final String JSON_DATE = "timestamp";
	public static final String JSON_LINE_NAME = "lineCode";
	public static final String JSON_NATURE = "nature";
	public static final String JSON_PLACE = "place";
	public static final String JSON_STOP_NAME = "stopName";
	
	private int m_code = -1;
	private String m_consequences = "";
	private Calendar m_date = DateTools.now();
	private Line m_line = new Line();
	private String m_nature = "";
	private String m_place = "";
	private Stop m_stop = new Stop();
	
	/**
	 * 
	 */
	public Disruption() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgId
	 */
	public Disruption(long ArgId) {
		super(ArgId);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @param ArgName
	 */
	public Disruption(String ArgName)
	{
		super(ArgName);
	}
	
	/**
	 * 
	 * @param ArgId
	 * @param ArgName
	 */
	public Disruption(long ArgId, String ArgName)
	{
		super(ArgId,ArgName);
	}

    protected Disruption(Parcel in){
        super(in);
        m_code = in.readInt();
        m_consequences = in.readString();
        m_date = DateTools.now();
        m_date.setTimeInMillis(in.readLong());
        m_line = (Line) in.readValue(Line.class.getClassLoader());
        m_nature = in.readString();
        m_place = in.readString();
        m_stop = (Stop) in.readValue(Stop.class.getClassLoader());

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
		if (m_line == null)
		{
			m_line = new Line();
		}
		if (m_stop == null)
		{
			m_stop = new Stop();
		}
		
		setCode(ArgJsonObj.optInt(JSON_CODE));
		setConsequences(ArgJsonObj.optString(JSON_CONSEQUENCE));
		
		String timestamp = ArgJsonObj.optString(JSON_DATE,"");
		setDate(DateTools.dateAPIToLocaleDate(timestamp));

		
		getLine().setName(ArgJsonObj.optString(JSON_LINE_NAME));
		setNature(ArgJsonObj.optString(JSON_NATURE));
		setPlace(ArgJsonObj.optString(JSON_PLACE));
		getStop().setName(ArgJsonObj.optString(JSON_STOP_NAME));

	}

	/**
	 * @return the m_code
	 */
	public int getCode() {
		return m_code;
	}

	/**
	 * @return the m_consequences
	 */
	public String getConsequences() {
		return m_consequences;
	}

	/**
	 * @return the m_date
	 */
	public Calendar getDate() {
		return m_date;
	}

	/**
	 * @return the m_line
	 */
	public Line getLine() {
		return m_line;
	}

	/**
	 * @return the m_nature
	 */
	public String getNature() {
		return m_nature;
	}

	/**
	 * @return the m_place
	 */
	public String getPlace() {
		return m_place;
	}

	/**
	 * @return the m_stop
	 */
	public Stop getStop() {
		return m_stop;
	}

	/**
	 * @param ArgCode the m_code to set
	 */
	public void setCode(int ArgCode) {
		this.m_code = ArgCode;
		updateName();
	}

	/**
	 * @param ArgConsequences the m_consequences to set
	 */
	public void setConsequences(String ArgConsequences) {
		this.m_consequences = ArgConsequences;
		updateName();
	}

	/**
	 * @param ArgDate the m_date to set
	 */
	public void setDate(Calendar ArgDate) {
		this.m_date = ArgDate;
		updateName();
	}

	/**
	 * @param ArgLine the m_line to set
	 */
	public void setLine(Line ArgLine) {
		if (ArgLine == null)
		{
			ArgLine = new Line();
		}
		
		this.m_line = ArgLine;
		updateName();
	}

	/**
	 * @param ArgNature the m_nature to set
	 */
	public void setNature(String ArgNature) {
		this.m_nature = ArgNature;
		updateName();
	}

    /**
     *
     * @param ArgName
     */
	@Override
	public void setName(String ArgName)
	{
		updateName();
	}

	/**
	 * @param ArgPlace the m_place to set
	 */
	public void setPlace(String ArgPlace) {
		this.m_place = ArgPlace;
		//updateName();
	}

	/**
	 * @param ArgStop the m_stop to set
	 */
	public void setStop(Stop ArgStop) {
		if (ArgStop == null)
		{
			ArgStop = new Stop();
		}
		
		this.m_stop = ArgStop;
		updateName();
	}
	
	private void updateName()
	{
		StringBuilder sb = new StringBuilder();
		
		//sb.append(String.valueOf(getCode()));
		//sb.append(";");
		sb.append(getConsequences());
		//sb.append(";");
		String dateString;
		if (DateTools.dateAreDifferent(getDate(), Calendar.getInstance(), DateTools.ComparisonType.OnlyDay))
		{
			dateString = DateTools.dateToString(getDate());
		}
		else
		{
			dateString = DateTools.dateToString(getDate(),DateTools.FormatType.OnlyHourWithoutSeconds);
		}
		//dateString = dateString.substring(0,dateString.length()-3);
		
		sb.append(dateString);
		//sb.append(";");
		sb.append(getLine().getName());
		//sb.append(";");
		sb.append(getLine().getArrivalStop().getName());
		//sb.append(";");
		sb.append(getLine().getDepartureStop().getName());
		//sb.append(";");
		sb.append(getNature());
		//sb.append(";");
		sb.append(getPlace());
		//sb.append(";");
		sb.append(getStop().getName());
		//sb.append(";");
		
		super.setName(sb.toString());
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

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeInt(m_code);
        dest.writeString(m_consequences);
        dest.writeLong(m_date.getTimeInMillis());
        dest.writeValue(m_line);
        dest.writeString(m_nature);
        dest.writeString(m_place);
        dest.writeValue(m_stop);
    }

    public static final Parcelable.Creator<Disruption> CREATOR
            = new Parcelable.Creator<Disruption>() {
        public Disruption createFromParcel(Parcel in) {
            return new Disruption(in);
        }

        public Disruption[] newArray(int size) {
            return new Disruption[size];
        }
    };
}
