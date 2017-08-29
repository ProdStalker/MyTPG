package com.mytpg.program.widgets.factories;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;


import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.settings.AlarmSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.fragments.ThermometerFragment;
import com.mytpg.program.tools.AlarmTools;
import com.mytpg.program.widgets.services.NextDeparturesService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 24.11.16.
 */

public class NextDeparturesRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private List<Departure> mDepartures = new ArrayList<>();

    public NextDeparturesRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        mDepartures = new ArrayList<>(NextDeparturesService.msDepartures);
    }

    @Override
    public void onDestroy() {
        mDepartures.clear();
    }

    @Override
    public int getCount() {
        return mDepartures.size();
    }

    public int lastIndexWaiting() {
        int index = 0;

        for (int i = 0; i < mDepartures.size(); i++)
        {
            if (mDepartures.get(i).getConnectionWaitingTime() >= -1)
            {
                break;
            }
            index++;
        }

        return index;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final Departure departure = mDepartures.get(position);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.app_widget_item_next_departures);
        rv.setTextViewText(R.id.lineTV, departure.getLine().getName());
        rv.setTextViewText(R.id.destinationTV, departure.getLine().getArrivalStop().getName());

        if (departure.getLine().getArrivalStop().getName().equalsIgnoreCase("Hôpital Trois-Chêne"))
        {
            rv.setTextViewText(R.id.destinationTV, "Hôpital 3-Chêne");
        }

       /* holder.mLineTV.setText(departure.getLine().getName());*/
        LineTools.configureRemoteTextView(mContext, R.id.lineTV, rv, departure.getLine());

        // int alarmVisibility = View.VISIBLE;
        /*int icon = R.drawable.ic_alarm_add;

        if (departure.hasAlarm())
        {
            icon = R.drawable.ic_alarm;
        }
        else if (!AlarmTools.canAddAlarm(departure, AlarmSettings.MINIMUM_MINUTES_ALARM)) {
           icon = R.drawable.ic_alarm_unable;//mContext.getDrawable(R.drawable.ic_alarm_unable);

        }*/
        //rv.setImageViewResource(R.id.alarmIV, icon);

        int visibility = View.VISIBLE;
        if (departure.getDisruptions().isEmpty())
        {
            visibility = View.GONE;
        }
        rv.setViewVisibility(R.id.warningIV, visibility);

		/*boolean isApi = false;
		App app = (App)m_context.getApplicationContext();
		if (app.getAbsDAOFact().getType() == AbstractDAOFactory.FactoryType.API)
		{
			isApi = true;
		}*/

        String timeBeforeDeparture = String.valueOf(departure.getWaitingTime());
        if (!timeBeforeDeparture.equalsIgnoreCase(">1h") &&
                !timeBeforeDeparture.equalsIgnoreCase("no more"))
        {
            timeBeforeDeparture += "'";
        }

       // RelativeLayout.LayoutParams handicapLP = (RelativeLayout.LayoutParams).mHandicapIV.getLayoutParams();

        if (departure.getWaitingTime().equalsIgnoreCase("0"))
        {
           // handicapLP.addRule(RelativeLayout.LEFT_OF,R.id.nowIV);
           // handicapLP.setMargins(0, 0, 0, 0);

            rv.setViewVisibility(R.id.nowIV, View.VISIBLE);
            rv.setViewVisibility(R.id.timeTV, View.INVISIBLE);
        }
        else
        {
            //handicapLP.addRule(RelativeLayout.LEFT_OF,R.id.timeTV);
           // handicapLP.setMargins(0, 0, 5, 0);
            rv.setViewVisibility(R.id.nowIV, View.INVISIBLE);
            rv.setViewVisibility(R.id.timeTV, View.VISIBLE);
        }

        rv.setTextViewText(R.id.timeTV, timeBeforeDeparture);
       // holder.mTimeTV.setTag(KEY_HOUR_VIEW_TYPE, DateTools.dateToString(departure.getDate(), DateTools.FormatType.OnlyHourWithoutSeconds));

        if (position == lastIndexWaiting() && !timeBeforeDeparture.equalsIgnoreCase("0'") && position != 0)
        {

            rv.setTextViewText(R.id.departureTV, mContext.getString(R.string.estimate_arrival,timeBeforeDeparture, DateTools.dateToString(departure.getDate(), DateTools.FormatType.OnlyHourWithoutSeconds)));
            rv.setViewVisibility(R.id.infosDepartureTV,View.VISIBLE);
        }
        else
        {
            rv.setViewVisibility(R.id.infosDepartureTV, View.GONE);
        }

        if (departure.isPRM())
        {
            rv.setViewVisibility(R.id.handicapIV,View.VISIBLE);
        }
        else
        {
            rv.setViewVisibility(R.id.handicapIV,View.GONE);
        }





        Bundle extras = new Bundle();
        extras.putString(MainActivity.ARG_FRAGMENT_WANTED, mContext.getString(R.string.menu_thermometer));
        extras.putInt(ThermometerFragment.ARG_DEPARTURE_CODE, departure.getCode());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.mainRelLay, fillInIntent);
        // Return the remote views object.
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
