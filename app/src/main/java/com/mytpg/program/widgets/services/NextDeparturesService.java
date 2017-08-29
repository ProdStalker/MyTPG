package com.mytpg.program.widgets.services;


import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViewsService;

import com.android.volley.VolleyError;
import com.mytpg.engines.data.api.DepartureAPI;
import com.mytpg.engines.data.api.LineAPI;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.data.interfaces.listeners.IDepartureAPIListener;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.program.widgets.NextDeparturesAppWidget;
import com.mytpg.program.widgets.configure.NextDeparturesAppWidgetConfigureActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 24.11.16.
 */

public class NextDeparturesService extends Service {
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public static List<Departure> msDepartures = new ArrayList<>();
    private static List<Line> msLines = new ArrayList<>();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Retrieve appwidget id from intent it is needed to update widget later
     * initialize our AQuery class
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
            mAppWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        loadData();
        return super.onStartCommand(intent, flags, startId);
    }

    private void loadData()
    {
        loadLinesFromDB();

        if (msLines.size() == 0)
        {
            loadLines();
        }
        else
        {
            loadNextDepartures();
        }

    }

    private void loadLines()
    {
        LineAPI lineAPI = new LineAPI(this);
        lineAPI.getAll(new IAPIListener<Line>() {
            @Override
            public void onError(VolleyError argVolleyError) {

            }

            @Override
            public void onSuccess(Line argObject) {

            }

            @Override
            public void onSuccess(List<Line> argObjects) {
                msLines = argObjects;
                loadNextDepartures();
            }
        });
    }

    private void loadLinesFromDB() {
        LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(this));
        msLines = lineDAO.getAll();
    }

    private void loadNextDepartures() {
        Line[] lines = new Line[]{};
        DepartureAPI depAPI = new DepartureAPI(this);
        depAPI.getAllByMnemo(NextDeparturesAppWidgetConfigureActivity.loadTitlePref(this,mAppWidgetId),lines, -1, new IDepartureAPIListener(){

            @Override
            public void onError(VolleyError argVolleyError) {

            }

            @Override
            public void onSuccess(Departure argObject) {

            }

            @Override
            public void onSuccess(List<Departure> argObjects) {
                msDepartures = argObjects;
                for (Departure dep : msDepartures)
                {
                    for (Line line : msLines) {
                        if (line.getName().equalsIgnoreCase(dep.getLine().getName())) {
                            dep.getLine().setColor(line.getColor());
                        }
                    }
                }
                updateWidget();
            }

            @Override
            public void onSuccess(List<Departure> argDepartures, Stop argStop) {

            }
        });
    }

    private void updateWidget()
    {
        Intent widgetUpdateIntent = new Intent();
        widgetUpdateIntent.setAction(NextDeparturesAppWidget.DATA_FETCHED);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                mAppWidgetId);
        sendBroadcast(widgetUpdateIntent);

        this.stopSelf();
    }

}
