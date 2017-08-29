package com.mytpg.engines.data.abstracts.core;

import android.content.Context;

import com.mytpg.engines.settings.DataSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public abstract class API<T> {
    private String mBaseUrl = DataSettings.API_BASE_URL;
    public Context mContext = null;

    public API(Context mContext)
    {
        this.mContext = mContext;
    }

    public String getBaseUrl()
    {
        return this.mBaseUrl;
    }

    public Context getContext()
    {
        return this.mContext;
    }

    protected abstract T getObjectFromJSON(JSONObject argJSONOBject);

    protected abstract List<T> getObjectsFromJson(JSONArray argJSONArray);

    public void setBaseUrl(String argBaseUrl)
    {
        this.mBaseUrl = argBaseUrl;
    }

    public void setContext(Context argContext)
    {
        this.mContext = argContext;
    }


}
