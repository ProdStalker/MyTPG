package com.mytpg.engines.entities.directions;

import android.os.Parcel;

import com.mytpg.engines.entities.core.Entity;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 21.10.16.
 */

public class DirectionEntity extends Entity {
    private String mFrom = "";
    private String mTo = "";

    public DirectionEntity() {
        super();
    }

    public DirectionEntity(long ArgId) {
        super(ArgId);
    }

    public DirectionEntity(DirectionEntity argDirectionEntity) {
        super(argDirectionEntity);

        setFrom(argDirectionEntity.getFrom());
        setTo(argDirectionEntity.getTo());
    }

    protected DirectionEntity(Parcel in) {
        super(in);
        setFrom(in.readString());
        setTo(in.readString());
    }

    public String getFrom() {
        return mFrom;
    }

    public String getTo() {
        return mTo;
    }

    public void setFrom(String argFrom) {
        this.mFrom = argFrom;
    }

    public void setTo(String argTo) {
        this.mTo = argTo;
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {

    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        dest.writeString(getFrom());
        dest.writeString(getTo());
    }

    public static final Creator<DirectionEntity> CREATOR = new Creator<DirectionEntity>() {
        @Override
        public DirectionEntity createFromParcel(Parcel in) {
            return new DirectionEntity(in);
        }

        @Override
        public DirectionEntity[] newArray(int size) {
            return new DirectionEntity[size];
        }
    };
}
