package com.mytpg.engines.entities.bustedapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.EntityWithName;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 18.07.17.
 */

public class AlertCategory extends EntityWithName {
    public static final String JSON_GROUP_NAME = "groupName";
    public static final String JSON_IMAGE_NAME = "groupImage";
    public static final String JSON_NAME = "categoryName";
    public static final String JSON_TAG = "categoryTag";

    private String mImageName = "";
    private String mGroupName = "";
    private String mTag = "";

    public AlertCategory() {
        super();
    }

    public AlertCategory(long ArgId) {
        super(ArgId);
    }

    public AlertCategory(String ArgName) {
        super(ArgName);
    }

    public AlertCategory(long ArgId, String ArgName) {
        super(ArgId, ArgName);
    }

    public AlertCategory(EntityWithName argEntityWithName) {
        super(argEntityWithName);
    }

    protected AlertCategory(Parcel in) {
        super(in);
    }

    public String getGroupName() {
        return mGroupName;
    }

    public String getImageName() {
        return mImageName;
    }

    public String getTag() {
        return mTag;
    }

    public void setGroupName(String argGroupName) {
        this.mGroupName = argGroupName;
    }

    public void setImageName(String argImageName) {
        this.mImageName = argImageName;
    }

    public void setTag(String argTag)
    {
        this.mTag = argTag;
    }

    @Override
    public String toString() {
        String text = super.toString();

        text += "\n group:" + getGroupName();
        text += "\n image name:" + getImageName();

        return text;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null) {
            return;
        }

        setName(ArgJsonObj.optString(JSON_NAME, ""));
        setImageName(ArgJsonObj.optString(JSON_IMAGE_NAME, ""));
        setGroupName(ArgJsonObj.optString(JSON_GROUP_NAME, ""));
        setTag(ArgJsonObj.optString(JSON_TAG, ""));
    }

    public static final Parcelable.Creator<AlertCategory> CREATOR = new Parcelable.Creator<AlertCategory>() {
        @Override
        public AlertCategory createFromParcel(Parcel in) {
            return new AlertCategory(in);
        }

        @Override
        public AlertCategory[] newArray(int size) {
            return new AlertCategory[size];
        }
    };
}
