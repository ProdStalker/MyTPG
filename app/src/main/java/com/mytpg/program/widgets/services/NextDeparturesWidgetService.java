package com.mytpg.program.widgets.services;


import android.content.Intent;
import android.widget.RemoteViewsService;

import com.mytpg.program.widgets.factories.NextDeparturesRemoteViewsFactory;

/**
 * Created by stalker-mac on 24.11.16.
 */

public class NextDeparturesWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NextDeparturesRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}
