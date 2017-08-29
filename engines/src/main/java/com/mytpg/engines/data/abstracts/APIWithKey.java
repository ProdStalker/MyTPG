package com.mytpg.engines.data.abstracts;

import android.content.Context;

import com.mytpg.engines.data.abstracts.core.API;
import com.mytpg.engines.settings.DataSettings;

/**
 * Created by stalker-mac on 23.10.16.
 */

public abstract class APIWithKey<T> extends API<T> {
    private String mKey = "";

    public APIWithKey(Context argContext)
    {
        super(argContext);
        this.mKey = DataSettings.API_KEY;
    }

    public String generateUrlKey()
    {
        return "key="+getKey();
    }

    public String getKey()
    {
        return this.mKey;
    }

    public void setKey(String argKey)
    {
        if (argKey == null)
        {
            argKey = "";
        }

        this.mKey = argKey.trim();
    }
}
