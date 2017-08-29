package com.mytpg.program.widgets.factories;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.mytpg.engines.data.dao.ConnectionDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.fragments.NextDeparturesFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 24.11.16.
 */

public class FavoriteStopsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private List<Stop> mFavoriteStops = new ArrayList<>();

    public FavoriteStopsRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }

    @Override
    public void onCreate() {
        /*StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(mContext));

        mFavoriteStops = stopDAO.getAllFavorites(true,true);*/
    }

    @Override
    public void onDataSetChanged() {
        StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(mContext));

        mFavoriteStops = stopDAO.getAllFavorites(true,false);
    }

    @Override
    public void onDestroy() {
        mFavoriteStops.clear();
    }

    @Override
    public int getCount() {
        return mFavoriteStops.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.app_widget_item_favorite_stops);
        rv.setTextViewText(R.id.nameTV, mFavoriteStops.get(position).getName());

        Bundle extras = new Bundle();
        extras.putString(MainActivity.ARG_FRAGMENT_WANTED, mContext.getString(R.string.menu_next_departures));
        extras.putString(NextDeparturesFragment.ARG_MNEMO, mFavoriteStops.get(position).getMnemo().getName());
        if (mFavoriteStops.get(position).isFavoriteDetailled()) {
            ConnectionDAO connDAO = new ConnectionDAO(DatabaseHelper.getInstance(mContext));
            List<Connection> connections = connDAO.getConnectionsByStop(mFavoriteStops.get(position).getId(), true);
            ArrayList<String> connectionsStringList = new ArrayList<>();
            for (Connection conn : connections)
            {
                connectionsStringList.add(String.valueOf(conn.getLine().getId()));
            }
            extras.putStringArrayList(NextDeparturesFragment.ARG_FILTER, connectionsStringList);
        }
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
