package com.mytpg.engines.entities.opendata;

import android.os.Parcel;

import com.mytpg.engines.entities.core.Entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 23.10.16.
 */

public class ODirection extends Entity {
    public final static String JSON_CONNECTIONS = "connections";
    public final static String JSON_FROM = "from";
    public final static String JSON_TO = "to";

    private List<OConnection> mConnections = new ArrayList<>();
    private OLocation mFrom = new OLocation();
    private OLocation mTo = new OLocation();

    public ODirection() {
        super();
    }

    public ODirection(long ArgId) {
        super(ArgId);
    }

    public ODirection(Entity argEntity) {
        super(argEntity);
    }

    protected ODirection(Parcel in) {
        super(in);

        if (in.readByte() == 0x01)
        {
            mConnections = new ArrayList<>();
            in.readList(mConnections, OConnection.class.getClassLoader());
        }
        else
        {
            mConnections = new ArrayList<>();
        }

        mFrom = (OLocation) in.readValue(OLocation.class.getClassLoader());
        mTo = (OLocation) in.readValue(OLocation.class.getClassLoader());
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {
        if (ArgJsonObj == null)
        {
            return;
        }

        if (mConnections == null)
        {
            mConnections = new ArrayList<>();
        }

        if (mFrom == null)
        {
            mFrom = new OLocation();
        }

        if (mTo == null)
        {
            mTo = new OLocation();
        }

        JSONArray connectionsJArray = ArgJsonObj.optJSONArray(JSON_CONNECTIONS);
        if (connectionsJArray != null)
        {
            for (int i = 0; i < connectionsJArray.length(); i++) {
                OConnection oConnection = new OConnection();
                oConnection.fromJson(connectionsJArray.optJSONObject(i));

                if (oConnection.getId() == -1)
                {
                    oConnection.setId(i);
                }

                mConnections.add(oConnection);
            }
        }

        mFrom.fromJson(ArgJsonObj.optJSONObject(JSON_FROM));
        mTo.fromJson(ArgJsonObj.optJSONObject(JSON_TO));
    }

    public List<OConnection> getConnections()
    {
        return mConnections;
    }

    public OLocation getFrom()
    {
        return mFrom;
    }

    public OLocation getTo()
    {
        return mTo;
    }

    public void setConnections(List<OConnection> argConnections)
    {
        if (argConnections == null)
        {
            argConnections = new ArrayList<>();
        }

        mConnections = argConnections;
    }

    public void setFrom(OLocation argFrom)
    {
        if (argFrom == null)
        {
            argFrom = new OLocation();
        }

        mFrom = argFrom;
    }

    public void setTo(OLocation argTo)
    {
        if (argTo == null)
        {
            argTo = new OLocation();
        }

        mTo = argTo;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        if (mConnections == null)
        {
            dest.writeByte((byte) 0x00);
        }
        else
        {
            dest.writeByte((byte) 0x01);
            dest.writeList(mConnections);
        }

        dest.writeValue(mFrom);
        dest.writeValue(mTo);
    }

    public static final Creator<ODirection> CREATOR = new Creator<ODirection>() {
        @Override
        public ODirection createFromParcel(Parcel in) {
            return new ODirection(in);
        }

        @Override
        public ODirection[] newArray(int size) {
            return new ODirection[size];
        }
    };
}
