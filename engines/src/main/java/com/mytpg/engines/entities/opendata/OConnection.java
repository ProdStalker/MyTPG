package com.mytpg.engines.entities.opendata;

import android.os.Parcel;

import com.mytpg.engines.entities.core.Entity;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class OConnection extends Entity {
    public final static String JSON_CAPACITY_1ST = "capacity1st";
    public final static String JSON_CAPACITY_2ND = "capacity2nd";
    public final static String JSON_DURATION = "duration";
    public final static String JSON_FROM = "from";
    public final static String JSON_PRODUCTS = "products";
    public final static String JSON_SECTIONS = "sections";
    public final static String JSON_SERVICE = "service";
    public final static String JSON_TO = "to";
    public final static String JSON_TRANSFERS = "transfers";

    private int mCapacity1st = -1;
    private int mCapacity2nd = -1;
    private String mDuration = "";
    private OCheckPoint mFrom = new OCheckPoint();
    private List<String> mProducts = new ArrayList<>();
    private List<OSection> mSections = new ArrayList<>();
    private OService mService = new OService();
    private OCheckPoint mTo = new OCheckPoint();
    private int mTransfers = 0;
    
    public OConnection() {
        super();
    }

    public OConnection(long ArgId) {
        super(ArgId);
    }

    public OConnection(Entity argEntity) {
        super(argEntity);
    }

    protected OConnection(Parcel in) {
        super(in);
        
        mCapacity1st = in.readInt();
        mCapacity2nd = in.readInt();
        mDuration = in.readString();
        mFrom = (OCheckPoint)in.readValue(OCheckPoint.class.getClassLoader());
        if (in.readByte() == 0x01)
        {
            mProducts = new ArrayList<>();
            in.readList(mProducts,String.class.getClassLoader());
        }
        else
        {
            mProducts = new ArrayList<>();
        }
        if (in.readByte() == 0x01)
        {
            mSections = new ArrayList<>();
            in.readList(mSections, OSection.class.getClassLoader());
        }
        else
        {
            mSections = new ArrayList<>();
        }
        mService = (OService)in.readValue(OService.class.getClassLoader());
        mTo = (OCheckPoint)in.readValue(OCheckPoint.class.getClassLoader());
        mTransfers = in.readInt();
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null)
        {
            return;
        }

        if (mProducts == null)
        {
            mProducts = new ArrayList<>();
        }
        else
        {
            mProducts.clear();
        }
        if (mSections == null)
        {
            mSections = new ArrayList<>();
        }
        else
        {
            mSections.clear();
        }
        if (mFrom == null)
        {
            mFrom = new OCheckPoint();
        }
        if (mTo == null)
        {
            mTo = new OCheckPoint();
        }
        if (mService == null)
        {
            mService = new OService();
        }

        mCapacity1st = ArgJsonObj.optInt(JSON_CAPACITY_1ST,-1);
        mCapacity2nd = ArgJsonObj.optInt(JSON_CAPACITY_2ND,-1);
        mDuration = ArgJsonObj.optString(JSON_DURATION,"");
        mFrom.fromJson(ArgJsonObj.optJSONObject(JSON_FROM));

        JSONArray productsJArray = ArgJsonObj.optJSONArray(JSON_PRODUCTS);
        if (productsJArray != null) {
            for (int i = 0; i < productsJArray.length(); i++) {
                String product = productsJArray.optString(i, "");
                if (!product.isEmpty()) {
                    mProducts.add(product);
                }
            }
        }

        JSONArray sectionsJArray = ArgJsonObj.optJSONArray(JSON_SECTIONS);
        if (sectionsJArray != null) {
            for (int i = 0; i < sectionsJArray.length(); i++) {
                OSection oSection = new OSection();
                oSection.fromJson(sectionsJArray.optJSONObject(i));
                if (oSection.getId() == -1) {
                    oSection.setId(i);
                }
                mSections.add(oSection);
            }
        }

        mService.fromJson(ArgJsonObj.optJSONObject(JSON_SERVICE));
        mTo.fromJson(ArgJsonObj.optJSONObject(JSON_TO));
        mTransfers = ArgJsonObj.optInt(JSON_TRANSFERS,0);
    }

    public int getCapacity1st() {
        return mCapacity1st;
    }

    public int getCapacity2nd() {
        return mCapacity2nd;
    }
    
    public String getDuration()
    {
        return mDuration;
    }

    public long getDurationInMillis()
    {
        long millis = 0;

        if (mDuration == null || mDuration.isEmpty())
        {
            return millis;
        }

        String[] mainDurationArray = mDuration.split("d");
        if (mainDurationArray.length != 2)
        {
            return millis;
        }

        String[] durationArray = mainDurationArray[1].split(":");
        if (durationArray.length != 3)
        {
            return millis;
        }

        int day = Integer.valueOf(mainDurationArray[0]).intValue();
        int hour = Integer.valueOf(durationArray[0]).intValue();
        int minute = Integer.valueOf(durationArray[1]).intValue();
        int second = Integer.valueOf(durationArray[2]).intValue();

        millis += day * DateTools.DAY_IN_MILLISECONDS;
        millis += hour * DateTools.HOUR_IN_MILLISECONDS;
        millis += minute * DateTools.MINUTE_IN_MILLISECONDS;
        millis += second * DateTools.SECOND_IN_MILLISECONDS;

        return millis;
    }

    public long getDurationValue(int argType)
    {
        long duration = 0;

        if (mDuration == null || mDuration.isEmpty())
        {
            return duration;
        }

        String[] mainDurationArray = mDuration.split("d");
        if (mainDurationArray.length != 2)
        {
            return duration;
        }

        String[] durationArray = mainDurationArray[1].split(":");
        if (durationArray.length != 3)
        {
            return duration;
        }

        int day = Integer.valueOf(mainDurationArray[0]).intValue();
        int hour = Integer.valueOf(durationArray[0]).intValue();
        int minute = Integer.valueOf(durationArray[1]).intValue();
        int second = Integer.valueOf(durationArray[2]).intValue();

        switch (argType)
        {
            case Calendar.DAY_OF_MONTH :
                return day;
            case Calendar.HOUR_OF_DAY :
                return hour;
            case Calendar.MINUTE :
                return minute;
            case Calendar.SECOND :
                return second;
        }

        return getDurationInMillis();
    }

    public OCheckPoint getFrom() {
        return mFrom;
    }

    public List<String> getProducts() {
        return mProducts;
    }

    public List<OSection> getSections() {
        return mSections;
    }

    public OService getService() {
        return mService;
    }

    public OCheckPoint getTo() {
        return mTo;
    }

    public int getTransfers()
    {
        return mTransfers;
    }

    public void setCapacity1st(int argCapacity1st) {
        if (argCapacity1st < 0)
        {
            argCapacity1st = -1;
        }

        this.mCapacity1st = argCapacity1st;
    }

    public void setCapacity2nd(int argCapacity2nd) {
        if (argCapacity2nd < 0)
        {
            argCapacity2nd = -1;
        }

        this.mCapacity2nd = argCapacity2nd;
    }

    public void setDuration(String argDuration) {
        if (argDuration == null)
        {
            argDuration = "";
        }

        this.mDuration = argDuration.trim();
    }

    public void setFrom(OCheckPoint argFrom) {
        if (argFrom == null)
        {
            argFrom = new OCheckPoint();
        }

        this.mFrom = argFrom;
    }

    public void setProducts(List<String> argProducts) {
        if (argProducts == null)
        {
            argProducts = new ArrayList<>();
        }

        this.mProducts = argProducts;
    }

    public void setSections(List<OSection> argSections) {
        if (argSections == null)
        {
            argSections = new ArrayList<>();
        }

        this.mSections = argSections;
    }

    public void setService(OService argService) {
        this.mService = argService;
    }

    public void setTo(OCheckPoint argTo) {
        this.mTo = argTo;
    }

    public void setTransfers(int argTransfers)
    {
        if (argTransfers < 0)
        {
            argTransfers = 0;
        }
        mTransfers = argTransfers;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        dest.writeInt(mCapacity1st);
        dest.writeInt(mCapacity2nd);
        dest.writeString(mDuration);
        dest.writeValue(mFrom);
        if (mProducts == null)
        {
            dest.writeByte((byte)0x00);
        }
        else
        {
            dest.writeByte((byte)0x01);
            dest.writeList(mProducts);
        }
        if (mSections == null)
        {
            dest.writeByte((byte)0x00);
        }
        else
        {
            dest.writeByte((byte)0x01);
            dest.writeList(mSections);
        }
        dest.writeValue(mService);
        dest.writeValue(mTo);
        dest.writeInt(mTransfers);
    }

    public static final Creator<OConnection> CREATOR = new Creator<OConnection>() {
        @Override
        public OConnection createFromParcel(Parcel in) {
            return new OConnection(in);
        }

        @Override
        public OConnection[] newArray(int size) {
            return new OConnection[size];
        }
    };

    public String formatDuration() {
        StringBuilder sb = new StringBuilder();

        long day = getDurationValue(Calendar.DAY_OF_MONTH);
        long hour = getDurationValue(Calendar.HOUR_OF_DAY);
        long minute = getDurationValue(Calendar.MINUTE);
       // long seconds = getDurationValue(Calendar.SECOND);

        if (day > 0)
        {
            sb.append(String.format("%1$sd",day));
        }

        if (hour < 10)
        {
            sb.append("0");
        }

        sb.append(String.format("%1$s",hour));

        sb.append(":");

        if (minute < 10)
        {
            sb.append("0");
        }

        sb.append(String.format("%1$s",minute));


        return sb.toString();
    }
}
