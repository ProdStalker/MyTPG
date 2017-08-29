package com.mytpg.engines.entities.opendata;

import android.os.Parcel;

import com.mytpg.engines.entities.core.Entity;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class OPrognosis extends Entity {
    public final static String JSON_ARRIVAL_TIMESTAMP = "arrivalTimestamp";
    public final static String JSON_CAPACITY_1ST = "capacity1st";
    public final static String JSON_CAPACITY_2ND = "capacity2nd";
    public final static String JSON_DEPARTURE_TIMESTAMP = "departureTimestamp";
    public final static String JSON_PLATFORM = "platform";

    private Calendar mArrivalDate = DateTools.zeroMillis();
    private int mCapacity1st = -1;
    private int mCapacity2nd = -1;
    private Calendar mDepartureDate = DateTools.zeroMillis();
    private String mPlatform = "";

    public OPrognosis() {
        super();
    }

    public OPrognosis(long ArgId) {
        super(ArgId);
    }

    public OPrognosis(Entity argEntity) {
        super(argEntity);
    }

    protected OPrognosis(Parcel in) {
        super(in);

        long arrivalMillis = in.readLong();
        mArrivalDate.setTimeInMillis(arrivalMillis);
        mCapacity1st = in.readInt();
        mCapacity2nd = in.readInt();
        long departureMillis = in.readLong();
        mDepartureDate.setTimeInMillis(departureMillis);
        mPlatform = in.readString();
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null) {
            return;
        }

        if (mArrivalDate == null) {
            mArrivalDate = DateTools.zeroMillis();
        }
        if (mDepartureDate == null) {
            mDepartureDate = DateTools.zeroMillis();
        }



        long arrivalTimestamp = ArgJsonObj.optLong(JSON_ARRIVAL_TIMESTAMP,0) * DateTools.SECOND_IN_MILLISECONDS;
        mArrivalDate.setTimeInMillis(arrivalTimestamp);
        mCapacity1st = ArgJsonObj.optInt(JSON_CAPACITY_1ST,-1);
        mCapacity2nd = ArgJsonObj.optInt(JSON_CAPACITY_2ND,-1);
        long departureTimestamp = ArgJsonObj.optLong(JSON_DEPARTURE_TIMESTAMP,0) * DateTools.SECOND_IN_MILLISECONDS;
        mDepartureDate.setTimeInMillis(departureTimestamp);
        mPlatform = ArgJsonObj.optString(JSON_PLATFORM,"");

    }

    public Calendar getArrivalDate()
    {
        return mArrivalDate;
    }

    public int getCapacity1st()
    {
        return mCapacity1st;
    }

    public int getCapacity2nd()
    {
        return mCapacity2nd;
    }

    public Calendar getDepartureDate()
    {
        return mDepartureDate;
    }

    public String getPlatform()
    {
        return mPlatform;
    }

    public void setArrivalDate(Calendar argArrivalDate)
    {
        if (argArrivalDate == null)
        {
            argArrivalDate = DateTools.zeroMillis();
        }

        mArrivalDate = argArrivalDate;
    }

    public void setCapacity1st(int argCapacity1st)
    {
        if (argCapacity1st < 0)
        {
            argCapacity1st = -1;
        }
        mCapacity1st = argCapacity1st;
    }

    public void setCapacity2nd(int argCapacity2nd)
    {
        if (argCapacity2nd < 0)
        {
            argCapacity2nd = -1;
        }

        mCapacity2nd = argCapacity2nd;
    }

    public void setDepartureDate(Calendar argDepartureDate)
    {
        if (argDepartureDate == null)
        {
            mDepartureDate = DateTools.zeroMillis();
        }

        mDepartureDate = argDepartureDate;
    }

    public void setPlatform(String argPlatform)
    {
        if (argPlatform == null)
        {
            argPlatform = "";
        }

        mPlatform = argPlatform.trim();
    }



    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        dest.writeLong(mArrivalDate.getTimeInMillis());
        dest.writeInt(mCapacity1st);
        dest.writeInt(mCapacity2nd);
        dest.writeLong(mDepartureDate.getTimeInMillis());
        dest.writeString(mPlatform);
    }

    public static final Creator<OPrognosis> CREATOR = new Creator<OPrognosis>() {
        @Override
        public OPrognosis createFromParcel(Parcel in) {
            return new OPrognosis(in);
        }

        @Override
        public OPrognosis[] newArray(int size) {
            return new OPrognosis[size];
        }
    };
}
