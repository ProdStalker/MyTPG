package com.mytpg.program.widgets.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.mytpg.program.widgets.factories.FavoriteStopsRemoteViewsFactory;

/**
 * Created by stalker-mac on 24.11.16.
 */

public class FavoriteStopsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FavoriteStopsRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
