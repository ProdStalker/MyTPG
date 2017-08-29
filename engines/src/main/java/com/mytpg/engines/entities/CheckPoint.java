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
public class CheckPoint extends Entity {
    public static final String JSON_ARRIVAL_TIME = "arrivalTime";
    public static final String JSON_CHARACTERISTICS = "characteristics";
    public static final String JSON_CODE = "departureCode";
    public static final String JSON_DATE = "timestamp";
    public static final String JSON_DISRUPTIONS = "disruptions";
    public static final String JSON_RELIABILITY = "reliability";
    public static final String JSON_STOP = "stop";
    public final static String JSON_VISIBLE = "visible";

    private int m_arrivalTime = -1;
    private int m_code = -1;
    private Calendar m_date = Calendar.getInstance();
    private List<Disruption> m_disruptions = new ArrayList<Disruption>();
    private boolean m_hasAlarm = false;
    private boolean m_isPRM = false;
    private Line m_line = new Line();
    private Departure.ReliabilityType m_reliabiltyType = Departure.ReliabilityType.NotDefined;
    private Stop m_stop = new Stop();

    private boolean m_isVisible = false;

    /**
     *
     */
    public CheckPoint() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean equals(Object ArgObj) {
        if (this == ArgObj)
            return true;
        if (ArgObj == null)
            return false;
        if (!(ArgObj instanceof CheckPoint))
            return false;
        CheckPoint checkPoint = (CheckPoint)ArgObj;
        if (getId() == checkPoint.getId() && getId() != -1)
        {
            return true;
        }
        if (getCode() == checkPoint.getCode())
        {
            return true;
        }

        return super.equals(ArgObj);
    }

    /**
     * @param ArgId
     */
    public CheckPoint(long ArgId) {
        super(ArgId);
        // TODO Auto-generated constructor stub
    }

    protected CheckPoint(Parcel in) {
        super(in);
        m_arrivalTime = in.readInt();
        m_code = in.readInt();
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
        m_reliabiltyType = (Departure.ReliabilityType) in.readValue(Departure.ReliabilityType.class.getClassLoader());
        m_stop = (Stop) in.readValue(Stop.class.getClassLoader());
        m_isVisible = in.readByte() != 0x00;
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

        setArrivalTime(ArgJsonObj.optInt(JSON_ARRIVAL_TIME,-1));
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

        getStop().fromJson(ArgJsonObj.optJSONObject(JSON_STOP));

        String characteristics = ArgJsonObj.optString(JSON_CHARACTERISTICS);
        setPRM(characteristics.equalsIgnoreCase("PMR"));

        Departure.ReliabilityType rt = Departure.ReliabilityType.NotDefined;
        String reliability = ArgJsonObj.optString(JSON_RELIABILITY);
        if (reliability.equalsIgnoreCase("F"))
        {
            rt = Departure.ReliabilityType.Reliable;
        }
        else if (reliability.equalsIgnoreCase("T"))
        {
            rt = Departure.ReliabilityType.Theorical;
        }

        setReliabiltyType(rt);

        setVisible(ArgJsonObj.optBoolean(JSON_VISIBLE));
    }

    /**
     *
     * @return the m_arrivalTime
     */
    public int getArrivalTime(){
        return m_arrivalTime;
    }

    /**
     * @return the m_code
     */
    public int getCode() {
        return m_code;
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
     * @return the m_line
     */
    public Line getLine() {
        return m_line;
    }

    /**
     * @return the m_reliabiltyType
     */
    public Departure.ReliabilityType getReliabiltyType() {
        return m_reliabiltyType;
    }

    /**
     *
     * @return the m_stop
     */
    public Stop getStop(){
        return m_stop;
    }

    public boolean hasAlarm()
    {
        return m_hasAlarm;
    }

    /**
     * @return the m_isPRM
     */
    public boolean isPRM() {
        return m_isPRM;
    }

    public boolean isVisible()
    {
        return m_isVisible;
    }

    public void setAlarm(boolean ArgHasAlarm)
    {
        m_hasAlarm = ArgHasAlarm;
    }

    public void setArrivalTime(int ArgArrivalTime){
        m_arrivalTime = ArgArrivalTime;
    }

    /**
     * @param ArgCode the m_code to set
     */
    public void setCode(int ArgCode) {
        this.m_code = ArgCode;
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
            ArgDisruptions = new ArrayList<Disruption>();
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
    public void setReliabiltyType(Departure.ReliabilityType ArgReliabiltyType) {
        this.m_reliabiltyType = ArgReliabiltyType;
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

    public void setVisible(final boolean ArgIsVisible)
    {
        m_isVisible = ArgIsVisible;
    }

    @Override
    public String toString() {
        String text = super.toString();

        text += "\n date : " + DateTools.dateToString(getDate());
        text += "\n stop : " + getStop().toString();

        return text;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeInt(m_arrivalTime);
        dest.writeInt(m_code);
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
        dest.writeByte((byte) (m_isVisible ? 0x01 : 0x00));
    }

    public static final Parcelable.Creator<CheckPoint> CREATOR = new Parcelable.Creator<CheckPoint>() {
        @Override
        public CheckPoint createFromParcel(Parcel in) {
            return new CheckPoint(in);
        }

        @Override
        public CheckPoint[] newArray(int size) {
            return new CheckPoint[size];
        }
    };

    public String toShareText(boolean argIsForMessage, int argStopNumber) {
        StringBuilder sb = new StringBuilder();

        String stateText = "\u2713"; // tick

        if (getArrivalTime() > -1 && isVisible()) {
            if (argStopNumber > 0) {
                stateText = String.valueOf(argStopNumber);
            }
        } else {
            stateText = "\u274C"; // cross
        }

        stateText = stateText.trim();

        if (argIsForMessage) {
            sb.append(String.format("%1s \t %2s \t [%3s]", getStop().getName(),
                                                           DateTools.dateToString(getDate(), DateTools.FormatType.OnlyHourWithoutSeconds),
                                                           stateText));
        }
        else {
            sb.append(String.format("[%1s] \t %2s \t %3s", stateText,
                                                            DateTools.dateToString(getDate(), DateTools.FormatType.OnlyHourWithoutSeconds),
                                                            getStop().getName()));
        }

        return sb.toString();
    }
}
