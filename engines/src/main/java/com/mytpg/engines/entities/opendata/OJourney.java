package com.mytpg.engines.entities.opendata;

import android.os.Parcel;

import com.mytpg.engines.entities.core.EntityWithName;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class OJourney extends EntityWithName {
    public final static String JSON_CAPACITY_1ST = "capacity1st";
    public final static String JSON_CAPACITY_2ND = "capacity2nd";
    public final static String JSON_CATEGORY = "category";
    public final static String JSON_CATEGORY_CODE = "categoryCode";
    public final static String JSON_NAME = "name";
    public final static String JSON_NUMBER = "number";
    public final static String JSON_OPERATOR = "operator";
    public final static String JSON_PASS_LIST = "passList";
    public final static String JSON_TO = "to";

    private int mCapacity1st = -1;
    private int mCapacity2nd = -1;
    private String mCategory = "";
    private String mCategoryCode = "";
    private String mNumber = "";
    private String mOperator = "";
    private List<OCheckPoint> mPassList = new ArrayList<>();
    private String mTo = "";

    public OJourney() {
        super();
    }

    public OJourney(long ArgId) {
        super(ArgId);
    }

    public OJourney(String ArgName) {
        super(ArgName);
    }

    public OJourney(long ArgId, String ArgName) {
        super(ArgId, ArgName);
    }

    public OJourney(EntityWithName argEntityWithName) {
        super(argEntityWithName);
    }

    protected OJourney(Parcel in) {
        super(in);

        mCapacity1st = in.readInt();
        mCapacity2nd = in.readInt();
        mCategory = in.readString();
        mCategoryCode = in.readString();
        mNumber = in.readString();
        mOperator = in.readString();
        if (in.readByte() == 0x01) {
            mPassList = new ArrayList<>();
            in.readList(mPassList, OCheckPoint.class.getClassLoader());
        } else {
            mPassList = new ArrayList<>();
        }
        mTo = in.readString();
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null)
        {
            return;
        }

        if (mPassList == null)
        {
            mPassList = new ArrayList<>();
        }

        mCapacity1st = ArgJsonObj.optInt(JSON_CAPACITY_1ST,-1);
        mCapacity2nd = ArgJsonObj.optInt(JSON_CAPACITY_2ND,-1);
        mCategory = ArgJsonObj.optString(JSON_CATEGORY,"");
        mCategoryCode = ArgJsonObj.optString(JSON_CATEGORY_CODE,"");
        setName(ArgJsonObj.optString(JSON_NAME,""));
        mNumber = ArgJsonObj.optString(JSON_NUMBER);
        mOperator = ArgJsonObj.optString(JSON_OPERATOR);

        JSONArray passListJArray = ArgJsonObj.optJSONArray(JSON_PASS_LIST);
        if (passListJArray != null) {
            for (int i = 0; i < passListJArray.length(); i++)
            {
                OCheckPoint oCheckPoint = new OCheckPoint();
                oCheckPoint.fromJson(passListJArray.optJSONObject(i));
                if (oCheckPoint.getId() == -1)
                {
                    oCheckPoint.setId(i);
                }
                mPassList.add(oCheckPoint);
            }
        }

        mTo = ArgJsonObj.optString(JSON_TO);
    }

    public int getCapacity1st() {
        return mCapacity1st;
    }

    public int getCapacity2nd() {
        return mCapacity2nd;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getCategoryCode(){
        return mCategoryCode;
    }

    public String getNumber() {
        return mNumber;
    }

    public String getOperator() {
        return mOperator;
    }

    public List<OCheckPoint> getPassList() {
        return mPassList;
    }

    public String getTo() {
        return mTo;
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

    public void setCategory(String argCategory) {
        if (argCategory == null)
        {
            mCategory = "";
        }

        this.mCategory = argCategory.trim();
    }

    public void setCategoryCode(String argCategoryCode)
    {
        if (argCategoryCode == null)
        {
            argCategoryCode = "";
        }

        this.mCategoryCode = argCategoryCode.trim();
    }

    public void setNumber(String argNumber) {
        if (argNumber == null)
        {
            argNumber = "";
        }

        this.mNumber = argNumber.trim();
    }

    public void setOperator(String argOperator) {
        if (argOperator == null)
        {
            argOperator = "";
        }

        this.mOperator = argOperator.trim();
    }

    public void setPassList(List<OCheckPoint> argPassList) {
        if (argPassList == null)
        {
            argPassList = new ArrayList<>();
        }

        this.mPassList = argPassList;
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

        dest.writeInt(mCapacity1st);
        dest.writeInt(mCapacity2nd);
        dest.writeString(mCategory);
        dest.writeString(mCategoryCode);
        dest.writeString(mNumber);
        dest.writeString(mOperator);
        if (mPassList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mPassList);
        }
        dest.writeString(mTo);

    }

    public static final Creator<OJourney> CREATOR = new Creator<OJourney>() {
        @Override
        public OJourney createFromParcel(Parcel in) {
            return new OJourney(in);
        }

        @Override
        public OJourney[] newArray(int size) {
            return new OJourney[size];
        }
    };
}
