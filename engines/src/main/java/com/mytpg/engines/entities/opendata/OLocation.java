package com.mytpg.engines.entities.opendata;

import android.location.Location;
import android.os.Parcel;

import com.mytpg.engines.entities.core.EntityWithNameAndLocation;
import com.mytpg.engines.settings.LocationSettings;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class OLocation extends EntityWithNameAndLocation {
    public final static String JSON_COORDINATES = "coordinate";
    public final static String JSON_COORDINATES_LATITUDE = "x";
    public final static String JSON_COORDINATES_LONGITUDE = "y";
    public final static String JSON_DISTANCE = "distance";
    public final static String JSON_ID = "id";
    public final static String JSON_NAME = "name";
    public final static String JSON_SCORE = "score";
    public final static String JSON_TYPE = "type";
  
    public enum Type{Address, Poi, Refine, Station}

    private double mDistance = -1.0f;
    private int mScore = -1;
    private Type mType = Type.Refine;

    public OLocation() {
        super();
    }

    public OLocation(long ArgId) {
        super(ArgId);
    }

    public OLocation(String ArgName) {
        super(ArgName);
    }

    public OLocation(long ArgId, String ArgName) {
        super(ArgId, ArgName);
    }

    public OLocation(EntityWithNameAndLocation argEntityWithNameAndLocation) {
        super(argEntityWithNameAndLocation);
    }

    public OLocation(Location argLoc) {
        super(argLoc);
    }

    protected OLocation(Parcel in) {
        super(in);

        mDistance = in.readDouble();
        mScore = in.readInt();
        mType = Type.values()[in.readInt()];
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null)
        {
            return;
        }
        
        if (getLocation() == null)
        {
            setLocation(new Location(LocationSettings.LOCATION_PROVIDER));
        }
        
        setName(ArgJsonObj.optString(JSON_NAME,""));
        mDistance = ArgJsonObj.optDouble(JSON_DISTANCE,-1.0d);
        mScore = ArgJsonObj.optInt(JSON_SCORE,-1);
        setType(ArgJsonObj.optString(JSON_TYPE));

        JSONObject coordinates = ArgJsonObj.optJSONObject(JSON_COORDINATES);
        if (coordinates != null) {
            getLocation().setLatitude(coordinates.optDouble(JSON_COORDINATES_LATITUDE,0.0d));
            getLocation().setLongitude(coordinates.optDouble(JSON_COORDINATES_LONGITUDE,0.0d));
        }
    }

    private void setType(String argType) {
        if (argType == null)
        {
            argType = "";
        }

        if (argType.equalsIgnoreCase("address"))
        {
            mType = Type.Address;
        }
        else if (argType.equalsIgnoreCase("poi"))
        {
            mType = Type.Poi;
        }
        else if (argType.equalsIgnoreCase("station"))
        {
            mType = Type.Station;
        }
        else
        {
            mType = Type.Refine;
        }
    }

    public double getDistance()
    {
        return mDistance;
    }

    public int getScore()
    {
        return mScore;
    }

    public Type getType()
    {
        return mType;
    }

    public void setDistance(double argDistance)
    {
        mDistance = argDistance;
    }

    public void setScore(int argScore)
    {
        if (mScore < 0)
        {
            argScore = -1;
        }

        mScore = argScore;
    }

    public void setType(Type argType)
    {
        mType = argType;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        dest.writeDouble(mDistance);
        dest.writeInt(mScore);
        dest.writeInt(mType.ordinal());
    }

    public static final Creator<OLocation> CREATOR = new Creator<OLocation>() {
        @Override
        public OLocation createFromParcel(Parcel in) {
            return new OLocation(in);
        }

        @Override
        public OLocation[] newArray(int size) {
            return new OLocation[size];
        }
    };
}
