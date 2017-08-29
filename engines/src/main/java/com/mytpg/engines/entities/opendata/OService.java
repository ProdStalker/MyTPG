package com.mytpg.engines.entities.opendata;

import android.os.Parcel;

import com.mytpg.engines.entities.core.Entity;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class OService extends Entity {
    public final static String JSON_IRREGULAR = "irregular";
    public final static String JSON_REGULAR = "regular";

    private String mIrregular = "";
    private String mRegular = "";

    public OService() {
        super();
    }

    public OService(long ArgId) {
        super(ArgId);
    }

    public OService(Entity argEntity) {
        super(argEntity);
    }

    protected OService(Parcel in) {
        super(in);

        mIrregular = in.readString();
        mRegular = in.readString();
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null)
        {
            return;
        }

        mIrregular = ArgJsonObj.optString(JSON_IRREGULAR,"");
        mRegular = ArgJsonObj.optString(JSON_REGULAR,"");
    }

    public String getIrregular() {
        return mIrregular;
    }

    public String getRegular() {
        return mRegular;
    }

    public void setIrregular(String argIrregular) {
        if (argIrregular == null)
        {
            argIrregular = "";
        }

        this.mIrregular = argIrregular.trim();
    }

    public void setRegular(String argRegular) {
        if (argRegular == null)
        {
            argRegular = "";
        }

        this.mRegular = argRegular.trim();
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        dest.writeString(mIrregular);
        dest.writeString(mRegular);
    }

    public static final Creator<OService> CREATOR = new Creator<OService>() {
        @Override
        public OService createFromParcel(Parcel in) {
            return new OService(in);
        }

        @Override
        public OService[] newArray(int size) {
            return new OService[size];
        }
    };
}
