package com.mytpg.engines.data.abstracts.busted;

import android.content.Context;
import android.provider.Settings;

import com.mytpg.engines.data.abstracts.APIWithKey;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.busted.Crypt;

/**
 * Created by stalker-mac on 23.10.16.
 */

public abstract class BustedAPI<T> extends APIWithKey<T> {
    public BustedAPI(Context argContext)
    {
        super(argContext);
        String androidId = Settings.Secure.getString(argContext.getContentResolver(),"android_id");

        setKey(Crypt.genUrl(androidId,DataSettings.BUSTED_API_PLATFORM, DataSettings.BUSTED_API_VERSION));
        setBaseUrl(DataSettings.BUSTED_API_BASE_URL);
    }

    @Override
    public String generateUrlKey()
    {
        return getKey();
    }

}
