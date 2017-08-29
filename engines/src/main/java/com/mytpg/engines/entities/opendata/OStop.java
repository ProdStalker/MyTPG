package com.mytpg.engines.entities.opendata;

import android.os.Parcel;

import com.mytpg.engines.entities.core.EntityWithName;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class OStop extends EntityWithName {
    public final static String JSON_CATEGORY = "category";
    public final static String JSON_NAME = "name";
    public final static String JSON_NUMBER = "number";
    public final static String JSON_OPERATOR = "operator";
    public final static String JSON_STATION = "station";
    public final static String JSON_TO = "to";

    private String mCategory = "";
    private String mNumber = "";
    private String mOperator = "";
    private OLocation mStation = new OLocation();
    private String mTo = "";

    public OStop() {
        super();
    }

    public OStop(long ArgId) {
        super(ArgId);
    }

    public OStop(String ArgName) {
        super(ArgName);
    }

    public OStop(long ArgId, String ArgName) {
        super(ArgId, ArgName);
    }

    public OStop(EntityWithName argEntityWithName) {
        super(argEntityWithName);
    }

    protected OStop(Parcel in) {
        super(in);

        mCategory = in.readString();
        mNumber = in.readString();
        mOperator = in.readString();
        mStation = (OLocation)in.readValue(OLocation.class.getClassLoader());
        mTo = in.readString();
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null)
        {
            return;
        }

        if (mStation == null)
        {
            mStation = new OLocation();
        }

        mCategory = ArgJsonObj.optString(JSON_CATEGORY,"");
        setName(ArgJsonObj.optString(JSON_NAME,""));
        mNumber = ArgJsonObj.optString(JSON_NUMBER,"");
        mOperator = ArgJsonObj.optString(JSON_OPERATOR,"");
        mStation.fromJson(ArgJsonObj.optJSONObject(JSON_STATION));
        mTo = ArgJsonObj.optString(JSON_TO,"");
    }

    public String getCategory() {
        return mCategory;
    }

    public String getNumber() {
        return mNumber;
    }

    public String getOperator() {
        return mOperator;
    }

    public OLocation getStation() {
        return mStation;
    }

    public String getTo() {
        return mTo;
    }

    public void setCategory(String argCategory)
    {
        if (argCategory == null)
        {
            argCategory = "";
        }

        mCategory = argCategory.trim();
    }

    public void setNumber(String argNumber)
    {
        if (argNumber == null)
        {
            argNumber = "";
        }

        mNumber = argNumber.trim();
    }

    public void setOperator(String argOperator)
    {
        if (argOperator == null)
        {
            argOperator = "";
        }

        mOperator = argOperator;
    }

    public void setStation(OLocation argStation)
    {
        if (argStation == null)
        {
            argStation = new OLocation();
        }

        mStation = argStation;
    }

    public void setTo(String argTo)
    {
        if (argTo == null)
        {
            argTo = "";
        }

        mTo = argTo.trim();
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        dest.writeString(mCategory);
        dest.writeString(mNumber);
        dest.writeString(mOperator);
        dest.writeValue(mStation);
        dest.writeString(mTo);
    }

    public static final Creator<OStop> CREATOR = new Creator<OStop>() {
        @Override
        public OStop createFromParcel(Parcel in) {
            return new OStop(in);
        }

        @Override
        public OStop[] newArray(int size) {
            return new OStop[size];
        }
    };
}
