package com.mytpg.engines.entities.bustedapp;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;
import com.mytpg.engines.entities.core.EntityWithNameAndLocation;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by stalker-mac on 18.07.17.
 */

public class Alert extends EntityWithNameAndLocation{
   /* date": "2017-07-18 19:55:09",
            "isInVehicle": "0",
            "stopName": "Fontenette",
            "vehicleLine": "",
            "direction": "",
            "latitude": "46.184889590011",
            "longitude": "6.145634501117",
            "hideLocation": "0",
            "comment": "",
            "categoryName": "Contrôleurs",
            "categoryTag": "controleurs",
            "groupName": "Contrôleurs",
            "groupImage": "policeman",
            "alertTitle": "Fontenette",
            "url": "" */
    public static final String JSON_COMMENT = "comment";
    public static final String JSON_DATE = "date";
    public static final String JSON_DIRECTION = "direction";
    public static final String JSON_HIDE_LOCATION = "hideLocation";
    public static final String JSON_IN_VEHICLE = "isInVehicle";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LINE_NAME = "vehicleLine";
    public static final String JSON_LONGITUDE = "longitude";
    public static final String JSON_STOP_NAME = "stopName";
    public static final String JSON_TITLE = "alertTitle";
    public static final String JSON_URL = "url";

    private AlertCategory mCategory = new AlertCategory();
    private String mComment = "";
    private Calendar mDate = DateTools.zeroMillis();
    private String mDirection = "";
    private boolean mHideLocation = false;
    private boolean mIsInVehicle = false;
    private String mLineName = "";
    private Marker mMarker = null;
    private String mStopName = "";
    private String mUrl = "";

    public Alert() {
        super();
    }

    public Alert(long ArgId) {
        super(ArgId);
    }

    public Alert(String ArgName) {
        super(ArgName);
    }

    public Alert(long ArgId, String ArgName) {
        super(ArgId, ArgName);
    }

    public Alert(Location argLoc) {
        super(argLoc);
    }

    public Alert(EntityWithNameAndLocation argEntityWithNameAndLocation) {
        super(argEntityWithNameAndLocation);
    }

    protected Alert(Parcel in) {
        super(in);
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {

        if (ArgJsonObj == null)
        {
            return;
        }
        if (getLocation() == null)
        {
            setLocation(null);
        }

        setName(ArgJsonObj.optString(JSON_TITLE, ""));

        String dateString = ArgJsonObj.optString(JSON_DATE, "");
        if (!dateString.isEmpty()) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                getDate().setTime(sdf.parse(dateString));// all done
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        setIsInVehicle(ArgJsonObj.optBoolean(JSON_IN_VEHICLE, false));
        setStopName(ArgJsonObj.optString(JSON_STOP_NAME, ""));
        setLineName(ArgJsonObj.optString(JSON_LINE_NAME, ""));
        setDirection(ArgJsonObj.optString(JSON_DIRECTION, ""));
        getLocation().setLatitude(ArgJsonObj.optDouble(JSON_LATITUDE, 0.0));
        getLocation().setLongitude(ArgJsonObj.optDouble(JSON_LONGITUDE, 0.0));
        setHideLocation(ArgJsonObj.optBoolean(JSON_HIDE_LOCATION, false));
        setComment(ArgJsonObj.optString(JSON_COMMENT, ""));
        setUrl(ArgJsonObj.optString(JSON_URL, ""));
        getCategory().fromJson(ArgJsonObj);
         
    }

    @Override
    public String toString() {
        String text = super.toString();

        text += "\n line:" + getLineName();
        text += "\n destination:" + getDirection();

        return text;
    }

    public AlertCategory getCategory() {
        return mCategory;
    }

    public String getComment() {
        return mComment;
    }

    public Calendar getDate() {
        return mDate;
    }

    public String getDirection() {
        return mDirection;
    }

    public boolean isHideLocation() {
        return mHideLocation;
    }

    public boolean isInVehicle() {
        return mIsInVehicle;
    }

    public String getLineName() {
        return mLineName;
    }

    public Marker getMarker()
    {
        return mMarker;
    }

    public String getStopName() {
        return mStopName;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setCategory(AlertCategory argCategory) {
        this.mCategory = argCategory;
    }

    public void setComment(String argComment) {
        this.mComment = argComment;
    }

    public void setDate(Calendar argDate) {
        this.mDate = argDate;
    }

    public void setDirection(String argDirection) {
        this.mDirection = argDirection;
    }

    public void setHideLocation(boolean argHideLocation) {
        this.mHideLocation = argHideLocation;
    }

    public void setIsInVehicle(boolean argIsInVehicle) {
        this.mIsInVehicle = argIsInVehicle;
    }

    public void setLineName(String argLineName) {
        this.mLineName = argLineName;
    }

    public void setMarker(Marker argMarker)
    {
        mMarker = argMarker;
    }

    public void setStopName(String argStopName) {
        this.mStopName = argStopName;
    }
    public void setUrl(String argUrl) {
        this.mUrl = argUrl;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<Alert> CREATOR = new Parcelable.Creator<Alert>() {
        @Override
        public Alert createFromParcel(Parcel in) {
            return new Alert(in);
        }

        @Override
        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };
}
