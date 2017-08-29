package com.mytpg.engines.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.Entity;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 11.11.14.
 */
public class Departure extends Entity {
    public static final String JSON_CHARACTERISTICS = "characteristics";
    public static final String JSON_CONNECTION_WAITING_TIME = "connectionWaitingTime";
    public static final String JSON_CODE = "departureCode";
    public static final String JSON_DATE = "timestamp";
    public static final String JSON_DISRUPTIONS = "disruptions";
    public static final String JSON_LINE = "line";
    public static final String JSON_RELIABILITY = "reliability";
    public static final String JSON_WAITING_TIME = "waitingTime";
    public static final String JSON_WAITING_TIME_MILLIS = "waitingTimeMillis";

    public String toShareText(boolean argIsForMessage) {
        StringBuilder sb = new StringBuilder();

        if (argIsForMessage)
        {
            sb.append(String.format("\t %1s -> %2s : ", getLine().getName(), getLine().getArrivalStop().getName()));

            sb.append(String.format("\t %1s ", DateTools.dateToString(getDate(), DateTools.FormatType.OnlyHourWithoutSeconds)));

            if (isPRM()) {
                sb.append("[\u267F] ");
            }

        }
        else {
            if (isPRM()) {
                sb.append("[\u267F] ");
            }

            sb.append(String.format("%1s : ", DateTools.dateToString(getDate(), DateTools.FormatType.OnlyHourWithoutSeconds)));

            sb.append(String.format("\t %1s -> %2s", getLine().getName(), getLine().getArrivalStop().getName()));
        }

        return sb.toString();
    }

    public enum ReliabilityType {NotDefined, Reliable, Theorical}

    private int m_code = -1;
    private long m_connectionWaitingTime = -1;
    private Calendar m_date = Calendar.getInstance();
    private List<Disruption> m_disruptions = new ArrayList<Disruption>();
    private boolean m_hasAlarm = false;
    private boolean m_isPRM = false;
    private Line m_line = new Line();
    private ReliabilityType m_reliabiltyType = ReliabilityType.NotDefined;
    private Stop m_stop = new Stop();
    private String m_waitingTime = "";
    private long m_waitingTimeMillis = -1;

    /**
     *
     */
    public Departure() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param ArgId
     */
    public Departure(long ArgId) {
        super(ArgId);
        // TODO Auto-generated constructor stub
    }

    public Departure(Departure ArgDeparture)
    {
        m_code = ArgDeparture.getCode();
        m_date = ArgDeparture.getDate();
        m_disruptions = new ArrayList<Disruption>(ArgDeparture.getDisruptions());
        m_isPRM = ArgDeparture.isPRM();
        m_line = new Line(ArgDeparture.getLine());
        m_reliabiltyType = ArgDeparture.getReliabiltyType();
        m_stop = new Stop(ArgDeparture.getStop());
        m_waitingTime = ArgDeparture.getWaitingTime();
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
        if (m_disruptions == null)
        {
            m_disruptions = new ArrayList<Disruption>();
        }
        if (m_line == null)
        {
            m_line = new Line();
        }
        if (m_stop == null)
        {
            m_stop = new Stop();
        }

		/*
		 * "characteristics": "PMR",
				"departureCode": 79564,
				"disruptions": [
					{
						"consequence": "Retard de 10 minutes entre Gardiol et Servette.",
						"disruptionCode": 10430,
						"nature": "Suite panne",
						"place": "",
						"stopName": "",
						"timestamp": "2014-07-31T17:06:00+0200"
					}
				],
				"line": {
					"destinationCode": "GARDIOL",
					"destinationName": "Gardiol",
					"lineCode": "3"
				},
				"reliability": "F",
				"timestamp": "2014-07-31T18:39:38+0200",
				"waitingTime": "0",
				"waitingTimeMillis": -29000
		 */

        setCode(ArgJsonObj.optInt(JSON_CODE,-1));
        setDate(DateTools.dateAPIToLocaleDate(ArgJsonObj.optString(JSON_DATE)));

        JSONArray disruptionsJSONArray = ArgJsonObj.optJSONArray(JSON_DISRUPTIONS);
        if (disruptionsJSONArray != null)
        {
            int i = 0;
            while (i < disruptionsJSONArray.length())
            {
                Disruption disruption = new Disruption();
                disruption.fromJson(disruptionsJSONArray.optJSONObject(i));
                m_disruptions.add(disruption);
                i++;
            }
        }
        getLine().fromJson(ArgJsonObj.optJSONObject(JSON_LINE));

        setConnectionWaitingTime(ArgJsonObj.optLong(JSON_CONNECTION_WAITING_TIME,-1));

        String characteristics = ArgJsonObj.optString(JSON_CHARACTERISTICS);
        setPRM(characteristics.equalsIgnoreCase("PMR"));

        ReliabilityType rt = ReliabilityType.NotDefined;
        String reliability = ArgJsonObj.optString(JSON_RELIABILITY);
        if (reliability.equalsIgnoreCase("F"))
        {
            rt = ReliabilityType.Reliable;
        }
        else if (reliability.equalsIgnoreCase("T"))
        {
            rt = ReliabilityType.Theorical;
        }

        setReliabiltyType(rt);
        setWaitingTime(ArgJsonObj.optString(JSON_WAITING_TIME,""));
        setWaitingTimeMillis(ArgJsonObj.optLong(JSON_WAITING_TIME_MILLIS,-1));

    }

    /**
     * @return the m_code
     */
    public int getCode() {
        return m_code;
    }

    /**
     *
     * @return
     */
    public long getConnectionWaitingTime(){
        return m_connectionWaitingTime;
    }

    /**
     * @return the m_date
     */
    public Calendar getDate() {
        return m_date;
    }

    /**
     * @return the m_disruption
     */
    public List<Disruption> getDisruptions() {
        return m_disruptions;
    }

    /**
     * @return the m_isPRM
     */
    public boolean isPRM() {
        return m_isPRM;
    }

    /**
     * @return the m_line
     */
    public Line getLine() {
        return m_line;
    }

    /**
     * @return the m_reliabiltyType
     */
    public ReliabilityType getReliabiltyType() {
        return m_reliabiltyType;
    }

    /**
     *
     * @return the m_stop
     */
    public Stop getStop(){
        return m_stop;
    }

    /**
     *
     * @return the m_waitingTime
     */
    public String getWaitingTime(){
        return m_waitingTime;
    }

    public long getWaitingTimeMillis()
    {
        return m_waitingTimeMillis;
    }

    public boolean hasAlarm()
    {
        return m_hasAlarm;
    }

    public void setAlarm(boolean ArgHasAlarm)
    {
        m_hasAlarm = ArgHasAlarm;
    }

    /**
     * @param ArgCode the m_code to set
     */
    public void setCode(int ArgCode) {
        this.m_code = ArgCode;
    }

    public void setConnectionWaitingTime(long ArgConnectionWaitingTime)
    {
        m_connectionWaitingTime = ArgConnectionWaitingTime;
    }

    /**
     * @param ArgDate the m_date to set
     */
    public void setDate(Calendar ArgDate) {
        this.m_date = ArgDate;
    }

    /**
     * @param ArgDisruptions the m_disruption to set
     */
    public void setDisruption(List<Disruption> ArgDisruptions) {
        if (ArgDisruptions == null)
        {
            ArgDisruptions = new ArrayList<>();
        }

        this.m_disruptions = ArgDisruptions;
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
    }

    /**
     * @param ArgIsPRM the m_isPRM to set
     */
    public void setPRM(boolean ArgIsPRM) {
        this.m_isPRM = ArgIsPRM;
    }

    /**
     * @param ArgReliabiltyType the m_reliabiltyType to set
     */
    public void setReliabiltyType(ReliabilityType ArgReliabiltyType) {
        this.m_reliabiltyType = ArgReliabiltyType;
    }

    protected Departure(Parcel in) {
        super(in);
        m_code = in.readInt();
        m_connectionWaitingTime = in.readLong();
        m_date = DateTools.now();
        m_date.setTimeInMillis(in.readLong());
        if (in.readByte() == 0x01) {
            m_disruptions = new ArrayList<Disruption>();
            in.readList(m_disruptions, Disruption.class.getClassLoader());
        } else {
            m_disruptions = new ArrayList<Disruption>();
        }
        m_hasAlarm = in.readByte() != 0x00;
        m_isPRM = in.readByte() != 0x00;
        m_line = (Line) in.readValue(Line.class.getClassLoader());
        m_reliabiltyType = (ReliabilityType) in.readValue(ReliabilityType.class.getClassLoader());
        m_stop = (Stop) in.readValue(Stop.class.getClassLoader());
        m_waitingTime = in.readString();
        m_waitingTimeMillis = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     *
     * @param ArgStop the m_stop to set
     */
    public void setStop(Stop ArgStop)
    {
        if (ArgStop == null)
        {
            ArgStop = new Stop();
        }

        m_stop = ArgStop;
    }

    public void setWaitingTime(String ArgWaitingTime){
        if (ArgWaitingTime.equalsIgnoreCase("&gt;1h"))
        {
            ArgWaitingTime = ">1h";
        }
        m_waitingTime = ArgWaitingTime;
    }

    public void setWaitingTimeMillis(long ArgWaitingTimeMillis){
        m_waitingTimeMillis = ArgWaitingTimeMillis;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeInt(m_code);
        dest.writeLong(m_connectionWaitingTime);
        dest.writeLong(m_date.getTimeInMillis());
        if (m_disruptions == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(m_disruptions);
        }
        dest.writeByte((byte) (m_hasAlarm ? 0x01 : 0x00));
        dest.writeByte((byte) (m_isPRM ? 0x01 : 0x00));
        dest.writeValue(m_line);
        dest.writeValue(m_reliabiltyType);
        dest.writeValue(m_stop);
        dest.writeString(m_waitingTime);
        dest.writeLong(m_waitingTimeMillis);
    }

    public static final Parcelable.Creator<Departure> CREATOR = new Parcelable.Creator<Departure>() {
        @Override
        public Departure createFromParcel(Parcel in) {
            return new Departure(in);
        }

        @Override
        public Departure[] newArray(int size) {
            return new Departure[size];
        }
    };
}
