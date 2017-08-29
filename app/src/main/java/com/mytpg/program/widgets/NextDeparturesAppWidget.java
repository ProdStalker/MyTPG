package com.mytpg.program.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.widgets.configure.NextDeparturesAppWidgetConfigureActivity;
import com.mytpg.program.widgets.services.NextDeparturesService;
import com.mytpg.program.widgets.services.NextDeparturesWidgetService;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link NextDeparturesAppWidgetConfigureActivity NextDeparturesAppWidgetConfigureActivity}
 */
public class NextDeparturesAppWidget extends AppWidgetProvider {
   public static final String DATA_FETCHED = "com.mytpg.program.broadcast.DATA_FETCHED";

   public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = NextDeparturesAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
       // Set up the intent that starts the StackViewService, which will
       // provide the views for this collection.
       Intent intent = new Intent(context, NextDeparturesWidgetService.class);
       // Add the app widget ID to the intent extras.
       intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
       intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
       // Instantiate the RemoteViews object for the app widget layout.
       RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.app_widget_next_departures);
       // Set up the RemoteViews object to use a RemoteViews adapter.
       // This adapter connects
       // to a RemoteViewsService  through the specified intent.
       // This is how you populate the data.
       rv.setRemoteAdapter(R.id.mainLV, intent);

       // The empty view is displayed when the collection has no items.
       // It should be in the same layout used to instantiate the RemoteViews
       // object above.
       rv.setEmptyView(R.id.mainLV, R.id.empty_view);

       Intent templateIntent = new Intent(context, MainActivity.class);
       templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
       PendingIntent templatePendingIntent = PendingIntent.getActivity(
               context, 0, templateIntent, 0);

       rv.setPendingIntentTemplate(R.id.mainLV, templatePendingIntent);

       appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.mainLV);
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            if (!NextDeparturesAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId).equalsIgnoreCase("EXAMPLE")) {
                Intent serviceIntent = new Intent(context, NextDeparturesService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        appWidgetId);
                context.startService(serviceIntent);
            }
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        if (intent.getAction().equals(NextDeparturesAppWidget.DATA_FETCHED)) {
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), NextDeparturesAppWidget.class.getName());
            int[] appWidgetIds = mgr.getAppWidgetIds(thisAppWidget);
            for (int appWidgetId : appWidgetIds)
            {
                updateAppWidget(context, mgr, appWidgetId);

            }
        }

        super.onReceive(context, intent);
    }
}

