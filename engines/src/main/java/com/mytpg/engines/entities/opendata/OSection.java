package com.mytpg.engines.entities.opendata;

import android.os.Parcel;

import com.mytpg.engines.entities.core.Entity;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class OSection extends Entity {
    public final static String JSON_ARRIVAL = "arrival";
    public final static String JSON_DEPARTURE = "departure";
    public final static String JSON_JOURNEY = "journey";
    public final static String JSON_WALK = "walk";
    
    private OCheckPoint mArrival = new OCheckPoint();
    private OCheckPoint mDeparture = new OCheckPoint();
    private OJourney mJourney = new OJourney();
    private double mWalk = -1.0d;
    
    public OSection() {
        super();
    }

    public OSection(long ArgId) {
        super(ArgId);
    }

    public OSection(Entity argEntity) {
        super(argEntity);
    }

    protected OSection(Parcel in) {
        super(in);

        mArrival = (OCheckPoint) in.readValue(OCheckPoint.class.getClassLoader());
        mDeparture = (OCheckPoint) in.readValue(OCheckPoint.class.getClassLoader());
        mJourney = (OJourney) in.readValue(OJourney.class.getClassLoader());
        mWalk = in.readFloat();
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null)
        {
            return;
        }

        if (mArrival == null)
        {
            mArrival = new OCheckPoint();
        }
        if (mDeparture == null)
        {
            mDeparture = new OCheckPoint();
        }
        if (mJourney == null)
        {
            mJourney = new OJourney();
        }
        
        mArrival.fromJson(ArgJsonObj.optJSONObject(JSON_ARRIVAL));
        mDeparture.fromJson(ArgJsonObj.optJSONObject(JSON_DEPARTURE));
        mJourney.fromJson(ArgJsonObj.optJSONObject(JSON_JOURNEY));
        mWalk = ArgJsonObj.optDouble(JSON_WALK,-1.0d);
    }

    public OCheckPoint getArrival() {
        return mArrival;
    }

    public OCheckPoint getDeparture() {
        return mDeparture;
    }

    public OJourney getJourney() {
        return mJourney;
    }

    public double getWalk() {
        return mWalk;
    }

    public void setArrival(OCheckPoint argArrival) {
        if (argArrival == null)
        {
            argArrival = new OCheckPoint();
        }

        this.mArrival = argArrival;
    }

    public void setDeparture(OCheckPoint argDeparture) {
        if (argDeparture == null)
        {
            argDeparture = new OCheckPoint();
        }

        this.mDeparture = argDeparture;
    }

    public void setJourney(OJourney argJourney) {
        if (argJourney == null)
        {
            argJourney = new OJourney();
        }

        this.mJourney = argJourney;
    }

    public void setWalk(double argWalk) {
        if (argWalk < 0)
        {
            argWalk = -1.0f;
        }

        this.mWalk = argWalk;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

    }

    public static final Creator<OSection> CREATOR = new Creator<OSection>() {
        @Override
        public OSection createFromParcel(Parcel in) {
            return new OSection(in);
        }

        @Override
        public OSection[] newArray(int size) {
            return new OSection[size];
        }
    };
}
