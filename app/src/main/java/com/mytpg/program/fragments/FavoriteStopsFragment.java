package com.mytpg.program.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mytpg.engines.data.dao.ConnectionDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.interfaces.IFavoriteStopsListener;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.graphics.SimpleItemTouchHelperCallback;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.adapters.FavoriteStopsAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.dialogs.LinesDialogFragment;
import com.mytpg.program.fragments.core.BaseFragment;
import com.mytpg.program.tools.WidgetTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class FavoriteStopsFragment extends BaseFragment {
    private final static String ARG_SAVED_FAVORITE_STOPS = "savedFavoriteStops";

    private RecyclerView mRecyclerView;
    //private RecyclerView mRecentStopsRV;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mRecentStopsLM;
    private FavoriteStopsAdapter mFavoriteStopsAdapter;

    private List<Stop> mFavoriteStops = new ArrayList<>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            mFavoriteStops = savedInstanceState.getParcelableArrayList(ARG_SAVED_FAVORITE_STOPS);
        }

        if (mFavoriteStops == null || mFavoriteStops.isEmpty())
        {
            loadData();
        }
        else
        {
            if (mFavoriteStopsAdapter == null)
            {
                mFavoriteStopsAdapter = new FavoriteStopsAdapter(getActivity(),mFavoriteStops);
                mFavoriteStopsAdapter.setOnItemClickListener(new FavoriteStopsAdapterClickListener());
                mFavoriteStopsAdapter.setOnItemLongClickListener(new FavoriteStopsAdapterClickListener());
            }
            else
            {
                mFavoriteStopsAdapter.setStops(mFavoriteStops);
            }

            mRecyclerView.setAdapter(mFavoriteStopsAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mFavoriteStopsAdapter != null)
        {
            outState.putParcelableArrayList(ARG_SAVED_FAVORITE_STOPS, mFavoriteStopsAdapter.getStopsToSave());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite_stops, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.mainRV);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecentStopsLM = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }

    private void loadData() {


        loadFavoriteStops();

    }

    private void loadFavoriteStops() {
        showPD();
        
        final Handler handler = new Handler();
        new Runnable(){

            @Override
            public void run() {
                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                mFavoriteStops = stopDAO.getAllFavorites(true,true);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFavoriteStopsAdapter = new FavoriteStopsAdapter(getActivity(),mFavoriteStops);
                        mFavoriteStopsAdapter.setOnItemClickListener(new FavoriteStopsAdapterClickListener());
                        mFavoriteStopsAdapter.setOnItemLongClickListener(new FavoriteStopsAdapterClickListener());
                        mFavoriteStopsAdapter.setOnFavoriteStopsListener(new IFavoriteStopsListener() {
                            @Override
                            public void onUpdatedAll() {
                                Snackbar.make(getView(),getString(R.string.favorite_stops_updated),Snackbar.LENGTH_LONG).show();
                                WidgetTools.updateWidget(getActivity(), WidgetTools.WidgetType.FavoriteStops);
                            }

                            @Override
                            public void onMoved(int argFrom, int argTo) {

                            }

                            @Override
                            public void onDissmiss(int argPosition) {
                                favoriteClicked(argPosition);
                            }
                        });
                        mRecyclerView.setAdapter(mFavoriteStopsAdapter);
                        showTuto();
                        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mFavoriteStopsAdapter);
                        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                        touchHelper.attachToRecyclerView(mRecyclerView);

                        dismissPD();
                    }
                });
            }
        }.run();

    }

    private void showTuto() {
        if (!isAdded() || mFavoriteStopsAdapter == null || mFavoriteStopsAdapter.getItemCount() == 0)
        {
            return;
        }

        App app = (App)getContext().getApplicationContext();
        if (app != null && app.isFirstFavoriteStops()) {

            mTutorialManager.getTutorials().clear();

            Tutorial howDeleteTuto = new Tutorial(getActivity().getString(R.string.action_delete), getActivity().getString(R.string.showcase_delete_favorite_stop));
            mTutorialManager.addTutorial(howDeleteTuto);

            Tutorial howOrderTuto = new Tutorial(getActivity().getString(R.string.order), getActivity().getString(R.string.showcase_order_favorite_stop));
            mTutorialManager.addTutorial(howOrderTuto);

            mTutorialManager.launchTutorials();

            app.setFirstFavoriteStops(false);
        }
    }

    @Override
    public void search(String argSearchText) {

    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_favorites_stops);
    }

    private class FavoriteStopsAdapterClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            Stop favoriteStop = mFavoriteStopsAdapter.getItem(position);
            if (favoriteStop == null)
            {
                return;
            }

            if (view instanceof TextView)
            {
                TextView tv = (TextView) view;
                if (tv != null && tv.getText().toString().equalsIgnoreCase("..."))
                {
                    if (getMainActivity() != null)
                    {

                        Bundle bundle = LinesDialogFragment.createBundle(favoriteStop.getConnections(),true);
                        getMainActivity().openDialogFragment(MainActivity.DialogFragmentName.Lines,bundle);
                    }
                }
                return;
            }
            else if (id == R.id.favoriteIV)
            {
                favoriteClicked(position);
            }
            else
            {
                Bundle bundle = null;

                NextDeparturesFragment nextDeparturesFragment = NextDeparturesFragment.newInstance();
                if (favoriteStop.isFavoriteDetailled())
                {
                    ConnectionDAO connDAO = new ConnectionDAO(DatabaseHelper.getInstance(getContext()));
                    List<Connection> connections = connDAO.getConnectionsByStop(favoriteStop.getId(), true);
                    bundle = NextDeparturesFragment.createBundle(favoriteStop.getMnemo().getName(), connections);
                }
                else {
                    bundle = NextDeparturesFragment.createBundle(favoriteStop.getMnemo().getName());
                }
                nextDeparturesFragment.setArguments(bundle);

                changeFragment(nextDeparturesFragment,true);
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
            if (mFavoriteStopsAdapter == null) {
                return false;
            }

            Stop stop = mFavoriteStopsAdapter.getItem(position);
            if (stop == null ||stop.getId() < 1) {
                return false;
            }

            return configureFavoriteDetails(stop, position);
        }
    }

    private boolean configureFavoriteDetails(Stop argStop, int argPosition) {
        showConnections(argStop, argPosition);
        return true;
    }

    private void showConnections(final Stop argStop, final int argPosition)
    {
        ConnectionDAO connDAO = new ConnectionDAO(DatabaseHelper.getInstance(getContext()));
        final List<Connection> connections = connDAO.getConnectionsByStop(argStop.getId());
        CharSequence[] choices = new CharSequence[connections.size()];
        boolean[] choicesBool = new boolean[choices.length];
        int i = 0;
        while (i < choicesBool.length)
        {
            Line currentLine = connections.get(i).getLine();
            choices[i] = currentLine.getName() + " : " + currentLine.getArrivalStop().getName();
           /* boolean found = false;
            int k = 0;

            while (k < mConnections.size())
            {
                Line currentConn = mConnections.get(k);
                if (currentConn.getId() == currentLine.getId())
                {
                    found = true;
                    break;
                }
                k++;
            }*/

            choicesBool[i] = connections.get(i).isFavorite();
            i++;
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());

        adb.setTitle(getString(R.string.action_connections));
        adb.setMultiChoiceItems(choices, choicesBool, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                connections.get(which).setFavorite(isChecked);

            }
        } );

        adb.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                saveFavoriteDetails(argStop, argPosition, connections);
            }
        });

        adb.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        adb.show();
    }

    private void saveFavoriteDetails(Stop argStop, int argPosition, List<Connection> argConnections)
    {
        if (argStop == null || argStop.getId() < 1 || mFavoriteStopsAdapter == null ||
                argPosition < 0 || argPosition >= mFavoriteStopsAdapter.getItemCount() ||
                argConnections == null || argConnections.isEmpty())
        {
            return;
        }

        boolean oneFavorite = false;
        ConnectionDAO connDAO = new ConnectionDAO(DatabaseHelper.getInstance(getContext()));
        for (Connection conn : argConnections)
        {
            if (conn.isFavorite())
            {
                oneFavorite = true;
            }
            connDAO.update(conn);
        }

        argStop.setFavorite(oneFavorite);

        changeStopFavorite(argStop, argPosition, true);
    }

    private void changeStopFavorite(Stop argStop, int argPosition, boolean argIsDetailled) {
        boolean changeSuccess;
        argStop.setFavoriteDetailled(argIsDetailled);
        StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
        if (argStop.isFavorite())
        {
            changeSuccess = stopDAO.addFavorite(argStop, argIsDetailled);
        }
        else
        {
            changeSuccess = stopDAO.removeFavorite(argStop);
        }

        String textChange = getString(R.string.favorite_stop_added);
        if (changeSuccess)
        {
            if (!argStop.isFavorite())
            {
                textChange = getString(R.string.favorite_stop_removed);
            }
            mFavoriteStopsAdapter.notifyItemChanged(argPosition);
            WidgetTools.updateWidget(getActivity(), WidgetTools.WidgetType.FavoriteStops);
        }
        else
        {
            if (argStop.isFavorite())
            {
                textChange = getString(R.string.favorite_stop_error_added);
            }
            else {
                textChange = getString(R.string.favorite_stop_error_removed);
            }
            argStop.setFavorite(!argStop.isFavorite());
        }

        Snackbar.make(getView(), textChange, Snackbar.LENGTH_SHORT).show();
    }

    private void favoriteClicked(int argPosition) {
        Stop favoriteStop = mFavoriteStops.get(argPosition);

        StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
        boolean changeSuccess = stopDAO.removeFavorite(favoriteStop);
        String textChange = getString(R.string.favorite_stop_removed);
        if (changeSuccess)
        {
            mFavoriteStopsAdapter.getStops().remove(argPosition);
            mFavoriteStopsAdapter.notifyItemRemoved(argPosition);
            mFavoriteStopsAdapter.updateFavoriteNumbers();

            WidgetTools.updateWidget(getActivity(), WidgetTools.WidgetType.FavoriteStops);
        }
        else
        {
            textChange = getString(R.string.favorite_stop_error_removed);
        }

        Snackbar.make(getView(), textChange, Snackbar.LENGTH_SHORT).show();
    }
}
