package com.mytpg.engines.data.abstracts.opendata;

import android.content.Context;

import com.mytpg.engines.data.abstracts.core.API;
import com.mytpg.engines.settings.DataSettings;

/**
 * Created by stalker-mac on 23.10.16.
 */

public abstract class OAPI<T> extends API<T> {

    public OAPI(Context argContext) {
        super(argContext);
        this.setBaseUrl(DataSettings.API_OPENDATA_BASE_URL);
    }

}
