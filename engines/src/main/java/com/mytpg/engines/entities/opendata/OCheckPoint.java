package com.mytpg.engines.entities.opendata;

import android.os.Parcel;

import com.mytpg.engines.entities.core.Entity;
import com.mytpg.engines.entities.core.EntityWithName;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class OCheckPoint extends Entity {
    public final static String JSON_ARRIVAL_TIMESTAMP = "arrivalTimestamp";
    public final static String JSON_DEPARTURE_TIMESTAMP = "departureTimestamp";
    public final static String JSON_LOCATION = "location";
    public final static String JSON_PLATFORM = "platform";
    public final static String JSON_PROGNOSIS = "prognosis";
    public final static String JSON_STATION = "station";

    private Calendar mArrivalDate = DateTools.zeroMillis();
    private Calendar mDepartureDate = DateTools.zeroMillis();
    private OLocation mLocation = new OLocation();
    private String mPlatform = "";
    private OPrognosis mPrognosis = new OPrognosis();
    private OLocation mStation = new OLocation();

    public OCheckPoint() {
        super();
    }

    public OCheckPoint(long ArgId) {
        super(ArgId);
    }

    public OCheckPoint(long ArgId, String ArgName) {
        super(ArgId);
    }

    public OCheckPoint(EntityWithName argEntity) {
        super(argEntity);
    }

    protected OCheckPoint(Parcel in) {
        super(in);

        long arrivalMillis = in.readLong();
        mArrivalDate.setTimeInMillis(arrivalMillis);
        long departureMillis = in.readLong();
        mDepartureDate.setTimeInMillis(departureMillis);
        mLocation = (OLocation)in.readValue(OLocation.class.getClassLoader());
        mPlatform = in.readString();
        mPrognosis = (OPrognosis) in.readValue(OPrognosis.class.getClassLoader());
        mStation = (OLocation) in.readValue(OLocation.class.getClassLoader());
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
        if (mLocation == null)
        {
            mLocation = new OLocation();
        }
        if (mStation == null) {
            mStation = new OLocation();
        }
        if (mPrognosis == null) {
            mPrognosis = new OPrognosis();
        }

        long arrivalTimestamp = ArgJsonObj.optLong(JSON_ARRIVAL_TIMESTAMP,0) * DateTools.SECOND_IN_MILLISECONDS;
        mArrivalDate.setTimeInMillis(arrivalTimestamp);
        long departureTimestamp = ArgJsonObj.optLong(JSON_DEPARTURE_TIMESTAMP,0) * DateTools.SECOND_IN_MILLISECONDS;
        mDepartureDate.setTimeInMillis(departureTimestamp);
        mLocation.fromJson(ArgJsonObj.optJSONObject(JSON_LOCATION));
        mPlatform = ArgJsonObj.optString(JSON_PLATFORM,"");
        mPrognosis.fromJson(ArgJsonObj.optJSONObject(JSON_PROGNOSIS));
        mStation.fromJson(ArgJsonObj.optJSONObject(JSON_STATION));
    }

    public Calendar getArrivalDate()
    {
        return mArrivalDate;
    }

    public Calendar getDepartureDate()
    {
        return mDepartureDate;
    }

    public OLocation getLocation()
    {
        return mLocation;
    }

    public String getPlatform()
    {
        return mPlatform;
    }

    public OPrognosis getPrognosis()
    {
        return mPrognosis;
    }

    public OLocation getStation()
    {
        return mStation;
    }

    public void setArrivalDate(Calendar argArrivalDate)
    {
        if (argArrivalDate == null)
        {
            argArrivalDate = DateTools.zeroMillis();
        }

        mArrivalDate = argArrivalDate;
    }

    public void setDepartureDate(Calendar argDepartureDate)
    {
        if (argDepartureDate == null)
        {
            mDepartureDate = DateTools.zeroMillis();
        }

        mDepartureDate = argDepartureDate;
    }

    public void setLocation(OLocation argLocation)
    {
        if (argLocation == null)
        {
            argLocation = new OLocation();
        }

        mLocation = argLocation;
    }

    public void setPlatform(String argPlatform)
    {
        if (argPlatform == null)
        {
            argPlatform = "";
        }

        mPlatform = argPlatform.trim();
    }

    public void setPrognosis(OPrognosis argPrognosis)
    {
        if (argPrognosis == null)
        {
            argPrognosis = new OPrognosis();
        }

        mPrognosis = argPrognosis;
    }

    public void setStation(OLocation argStation)
    {
        if (argStation == null)
        {
            argStation = new OLocation();
        }

        mStation = argStation;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        dest.writeLong(mArrivalDate.getTimeInMillis());
        dest.writeLong(mDepartureDate.getTimeInMillis());
        dest.writeValue(mLocation);
        dest.writeString(mPlatform);
        dest.writeValue(mPrognosis);
        dest.writeValue(mStation);
    }

    public static final Creator<OCheckPoint> CREATOR = new Creator<OCheckPoint>() {
        @Override
        public OCheckPoint createFromParcel(Parcel in) {
            return new OCheckPoint(in);
        }

        @Override
        public OCheckPoint[] newArray(int size) {
            return new OCheckPoint[size];
        }
    };
}
