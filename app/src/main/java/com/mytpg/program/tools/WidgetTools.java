package com.mytpg.program.tools;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

import com.mytpg.program.R;
import com.mytpg.program.widgets.FavoriteStopsAppWidget;

/**
 * Created by stalker-mac on 22.12.16.
 */

public abstract class WidgetTools {
    public enum WidgetType {FavoriteStops;}

    public static void updateWidget(Context argContext, WidgetType argWidgetType)
    {
        Intent intent = null;
        int widgetId = -1;
        switch (argWidgetType)
        {
            case FavoriteStops:
                intent = new Intent(argContext,FavoriteStopsAppWidget.class);
                widgetId = R.xml.app_widget_info_favorite_stops;
            break;
        }

        if (intent == null)
        {
            return;
        }

        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
        int[] ids = {widgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        argContext.sendBroadcast(intent);
    }
}

