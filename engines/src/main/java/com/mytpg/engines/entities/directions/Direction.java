package com.mytpg.engines.entities.directions;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by stalker-mac on 21.10.16.
 */

public class Direction extends DirectionEntity {
    private Calendar mDate = DateTools.now();
    private boolean mIsDeparture = true;
    private Stop mFromStop = new Stop();
    private Stop mToStop = new Stop();
    
    public Direction() {
        super();
    }

    public Direction(long ArgId) {
        super(ArgId);
    }

    public Direction(Direction argDirection) {
        super(argDirection);

        setDate(argDirection.getDate());
        setDeparture(argDirection.isDeparture());
    }

    protected Direction(Parcel in) {
        super(in);
        Calendar cal = DateTools.now();
        long millis = in.readLong();
        cal.setTimeInMillis(millis);
        setDeparture(in.readByte() != 0x00);
        mFromStop = (Stop)in.readValue(Stop.class.getClassLoader());
        mToStop = (Stop)in.readValue(Stop.class.getClassLoader());
    }

    public Calendar getDate() {
        return mDate;
    }

    public Stop getFromStop()
    {
        return mFromStop;
    }

    public Stop getToStop()
    {
        return mToStop;
    }

    public boolean isDeparture() {
        return mIsDeparture;
    }

    public void setDate(Calendar argDate) {
        this.mDate = argDate;
    }

    public void setDeparture(boolean argIsDeparture) {
        this.mIsDeparture = argIsDeparture;
    }

    public void setFromStop(Stop argStop)
    {
        if (argStop == null)
        {
            argStop = new Stop();
        }

        mFromStop = argStop;
    }

    public void setToStop(Stop argStop)
    {
        if (argStop == null)
        {
            argStop = new Stop();
        }

        mToStop = argStop;
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

        dest.writeLong(getDate().getTimeInMillis());
        dest.writeByte((byte) (isDeparture() ? 0x01 : 0x00));
        dest.writeValue(mFromStop);
        dest.writeValue(mToStop);
    }

    public static final Parcelable.Creator<Direction> CREATOR = new Parcelable.Creator<Direction>() {
        @Override
        public Direction createFromParcel(Parcel in) {
            return new Direction(in);
        }

        @Override
        public Direction[] newArray(int size) {
            return new Direction[size];
        }
    };
}
