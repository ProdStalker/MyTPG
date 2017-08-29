/**
 * 
 */
package com.mytpg.engines.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.EntityWithName;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * @author StalkerA
 *
 */
public class Ticket extends EntityWithName {
	private String m_code = "";
    private Calendar m_date = DateTools.now();
	private String m_description = "";
	private boolean m_isFull = true;
	private double m_price = -1.0;
	
	/**
	 * 
	 */
	public Ticket() {
        setDate(null);
	}

	/**
	 * @param ArgId
	 */
	public Ticket(long ArgId) {
		super(ArgId);
        setDate(null);
	}

	/**
	 * @param ArgName
	 */
	public Ticket(String ArgName) {
		super(ArgName);
        setDate(null);
	}

	/**
	 * @param ArgId
	 * @param ArgName
	 */
	public Ticket(long ArgId, String ArgName) {
		super(ArgId, ArgName);
        setDate(null);
	}



    protected Ticket(Parcel in) {
        super(in);
        m_code = in.readString();
        m_description = in.readString();
        Calendar cal = DateTools.now();
        long millis = in.readLong();
        cal.setTimeInMillis(millis);
        m_date = cal;
        m_isFull = in.readByte() != 0x00;
        m_price = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

	public String getCode()
	{
		return m_code;
	}

    public Calendar getDate()
    {
        return m_date;
    }
	
	public String getDescription()
	{
		return m_description;
	}
	
	public double getPrice()
	{
		return m_price;
	}

	/* (non-Javadoc)
	 * @see com.mytpg.engines.entities.core.Entity#fromJson(org.json.JSONObject)
	 */
	@Override
	public void fromJson(JSONObject ArgJsonObj) {
		// TODO Auto-generated method stub

	}
	
	public boolean isFull()
	{
		return m_isFull;
	}
	
	public void setCode(String ArgCode)
	{
		if (ArgCode == null)
		{
			ArgCode = "";
		}
		
		m_code = ArgCode;
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

	public void setDescription(String ArgDescription)
	{
		if (ArgDescription == null)
		{
			ArgDescription = "";
		}
		
		m_description = ArgDescription;
	}
	
	public void setFull(boolean ArgIsFull)
	{
		m_isFull = ArgIsFull;
	}
	
	public void setPrice(double ArgPrice)
	{
		if (ArgPrice < 0)
		{
			ArgPrice = -1;
		}
		m_price = ArgPrice;
	}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(m_code);
        dest.writeString(m_description);
        dest.writeByte((byte) (m_isFull ? 0x01 : 0x00));
        dest.writeDouble(m_price);
    }

    public static final Parcelable.Creator<Ticket> CREATOR = new Parcelable.Creator<Ticket>() {
        @Override
        public Ticket createFromParcel(Parcel in) {
            return new Ticket(in);
        }

        @Override
        public Ticket[] newArray(int size) {
            return new Ticket[size];
        }
    };
}
