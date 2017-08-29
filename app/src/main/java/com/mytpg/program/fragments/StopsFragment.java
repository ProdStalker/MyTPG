package com.mytpg.program.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mytpg.engines.data.api.LineAPI;
import com.mytpg.engines.data.api.StopAPI;
import com.mytpg.engines.data.api.bustedapp.BustedStopAPI;
import com.mytpg.engines.data.dao.ConnectionDAO;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.PhysicalStopDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.dao.bustedapp.BustedStopDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.bustedapp.BustedStop;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.SortTools;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.adapters.StopsAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.dialogs.LinesDialogFragment;
import com.mytpg.program.fragments.core.BaseFragment;
import com.mytpg.program.tools.WidgetTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class StopsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_SAVED_STOPS = "savedStops";

    private RecyclerView mRecyclerView;
    //private RecyclerView mRecentStopsRV;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mRecentStopsLM;
    private StopsAdapter mStopsAdapter;
    // private StopsAdapter mRecentStopsAdapter;

    private List<Line> mLines = new ArrayList<>();
    private List<Stop> mStops = new ArrayList<>();
    private List<BustedStop> mBustedStops = new ArrayList<>();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mStopsAdapter == null)
        {
            return;
        }

        ArrayList<Stop> stops = mStopsAdapter.getStopsToSave();

        if (!stops.isEmpty()) {
            outState.putParcelableArrayList(ARG_SAVED_STOPS, stops);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ArrayList<Stop> stops = null;
        if (savedInstanceState != null)
        {
            stops = savedInstanceState.getParcelableArrayList(ARG_SAVED_STOPS);
        }

        if (stops == null || stops.isEmpty())
        {
            loadData();
        }
        else
        {
            if (mStopsAdapter == null)
            {
                mStopsAdapter = new StopsAdapter(getActivity(), stops);
            }
            else {
                mStopsAdapter.setStops(stops);
            }
            mRecyclerView.setAdapter(mStopsAdapter);

            /*ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mStopsAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRecyclerView);*/

            mStopsAdapter.setOnItemClickListener(new StopsAdapterClickListener());
            mStopsAdapter.setOnItemLongClickListener(new StopsAdapterClickListener());

        }
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stops, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);
        // mRecentStopsRV = (RecyclerView)rootView.findViewById(R.id.recentStopsRV);

        mRecyclerView.setHasFixedSize(true);
        // mRecentStopsRV.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecentStopsLM = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        /* mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * Callback method to be invoked when the RecyclerView has been scrolled. This will be
             * called after the scroll has completed.
             *
             * @param recyclerView The RecyclerView which scrolled.
             * @param dx           The amount of horizontal scroll.
             * @param dy           The amount of vertical scroll.
             */
           /* @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowVerticalPosition = (mRecyclerView == null || mRecyclerView.getChildCount() == 0) ?
                        0 : mRecyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });*/
        // mRecentStopsRV.setLayoutManager(mRecentStopsLM);

        //loadData();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);
        if (getMainActivity() != null)
        {
            getMainActivity().initializeSearchView();
        }
    }

    private void loadData()
    {
        //showPD();

        App app = getApp();
        if (app == null)
        {
            return;
        }

        final boolean isFirstLaunchOfDayLines = app.isFirstLaunchOfDay(App.FirstLaunchType.Lines);
        final boolean isFirstLaunchOfDayStops = app.isFirstLaunchOfDay(App.FirstLaunchType.Stops);


        mSwipeRefreshLayout.setRefreshing(false);

        if (isFirstLaunchOfDayStops || isFirstLaunchOfDayLines) {
            dismissPD();

            loadStops();
        }
        else {
            //mSwipeRefreshLayout.setRefreshing(true);
            loadStopsFromDB();
        }

    }



    public void loadLines()
    {
        changePDMessageAndStep(2,getString(R.string.getting_lines_data_online));

        LineAPI lineAPI = new LineAPI(getActivity());
        lineAPI.getAll(new IAPIListener<Line>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissPD();
            }

            @Override
            public void onSuccess(Line argObject) {
            }

            @Override
            public void onSuccess(List<Line> argObjects) {
                mLines = argObjects;
                loadBustedStops();
            }
        });
    }

    private void loadBustedStops() {
        changePDMessageAndStep(3,getString(R.string.getting_busted_stops_data_online));

        BustedStopAPI bustedStopAPI = new BustedStopAPI(getActivity());
        bustedStopAPI.getAll(new IAPIListener<BustedStop>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissPD();
            }

            @Override
            public void onSuccess(BustedStop argObject) {
            }

            @Override
            public void onSuccess(List<BustedStop> argObjects) {
                mBustedStops = argObjects;
                updateLineColors();
            }
        });
    }

    private void loadStopsFromDB()
    {
        final Handler handler = new Handler();
        Runnable stopsRunnable = new Runnable() {
            @Override
            public void run() {
                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                mStops = stopDAO.getAll(true,true,false);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mStopsAdapter = new StopsAdapter(getActivity(),mStops);
                        mRecyclerView.setAdapter(mStopsAdapter);
                        mStopsAdapter.setOnItemClickListener(new StopsAdapterClickListener());
                        mStopsAdapter.setOnItemLongClickListener(new StopsAdapterClickListener());

                        //dismissPD();

                        mSwipeRefreshLayout.setRefreshing(false);

                        new LoadDataDBAsyncTask().execute();

                    }
                });
            }
        };
        stopsRunnable.run();

    }

    private void changePDMessageAndStep(int argStep, String argMessage)
    {
        if (mPD == null)
        {
            mPD = new ProgressDialog(getActivity());
            mPD.setIndeterminate(false);
            mPD.setCancelable(false);
           // mPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mPD.setMax(8);
            mPD.setTitle(getString(R.string.loading));
            mPD.show();
        }

        mPD.setTitle(getString(R.string.loading_steps,argStep,mPD.getMax()));
        mPD.setProgress(argStep);
        mPD.setMessage(argMessage);
    }

    private void loadStops()
    {
        changePDMessageAndStep(1,getString(R.string.getting_data_stops_online));
        StopAPI stopAPI = new StopAPI(getActivity());

        stopAPI.getAll(true, new IAPIListener<Stop>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissPD();
            }

            @Override
            public void onSuccess(Stop argObject) {

            }

            @Override
            public void onSuccess(List<Stop> argObjects) {
                mStops = argObjects;
                SortTools.sortEntityWithName(mStops, SortTools.FilterType.AZ);
                loadLines();
            }
        });
    }

    @Override
    public void search(String argSearchText) {
        if (mStopsAdapter != null)
        {
            mStopsAdapter.search(argSearchText);
        }
    }

    private void updateLineColors()
    {

      /*  for (Line line : mLines)
        {
            for (int i = 0; i < mStopsAdapter.getItemCount(); i++) {
                Stop stop = mStopsAdapter.getItem(i);
                for (int j = 0; j < stop .getConnections().size(); j++) {
                    if (stop.getConnections().get(j).getName().equalsIgnoreCase(line.getName()))
                    {
                        stop.getConnections().get(j).setColor(line.getColor());
                        mStopsAdapter.notifyItemChanged(i);
                    }
                }

            }
        }*/
        new LoadDataAsyncTask().execute();




    }

    public void updateToolbarMenu()
    {
        super.updateToolbarMenu();

        if (getMainActivity() != null)
        {
            getMainActivity().changeToolbarMenu(R.id.search,true);
        }
    }

    @Override
    public void onRefresh() {
        if (getApp() != null)
        {
            getApp().setFirstLaunchOfDay(App.FirstLaunchType.Lines,true);
            getApp().setFirstLaunchOfDay(App.FirstLaunchType.Stops,true);
        }

        loadData();
    }

    private void saveFavoriteDetails(Stop argStop, int argPosition, List<Connection> argConnections)
    {
        if (argStop == null || argStop.getId() < 1 || mStopsAdapter == null ||
                argPosition < 0 || argPosition >= mStopsAdapter.getItemCount() ||
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

    private class LoadDataDBAsyncTask extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (mStopsAdapter != null && values[0] >= 0 && values[0] < mStopsAdapter.getItemCount())
            {
                mStopsAdapter.notifyItemChanged(values[0]);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
            for (int i = 0; i < mStopsAdapter.getItemCount(); i++)
            {
                if (mStopsAdapter.getItemCount() == 0)
                {
                    break;
                }

                if (i >= 0 && i < mStopsAdapter.getItemCount()) {
                    if (mStopsAdapter.getItem(i).getConnections().size() == 0)
                    {
                        for (PhysicalStop physicalStop : mStopsAdapter.getItem(i).getPhysicalStops()) {
                            List<Line> lines = lineDAO.getAllByPhysicalStop(physicalStop.getId());
                            physicalStop.setConnections(lines);
                        }

                        publishProgress(i);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    private class LoadDataAsyncTask extends AsyncTask<Void, String, Void>
    {
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            int step = Integer.valueOf(values[0]).intValue();
            String message = values[1];

            changePDMessageAndStep(step,message);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mStopsAdapter = new StopsAdapter(getActivity(), mStops);
            mRecyclerView.setAdapter(mStopsAdapter);
                        /*ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mStopsAdapter);
                        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                        touchHelper.attachToRecyclerView(mRecyclerView);*/
            mStopsAdapter.setOnItemClickListener(new StopsAdapterClickListener());
            mStopsAdapter.setOnItemLongClickListener(new StopsAdapterClickListener());



                       /* List<Stop> recentStops = new ArrayList<Stop>();

                        Random rn = new Random();
                        for (int i = 0; i < 5; i++)
                        {
                            int randomNumber = rn.nextInt(mStops.size());
                            recentStops.add(new Stop(mStops.get(randomNumber)));
                        }
                        mRecentStopsAdapter = new StopsAdapter(getActivity(), recentStops);
                        mRecentStopsRV.setAdapter(mRecentStopsAdapter); */

            //dismissPD();
            dismissPD();

            App app = getApp();

            if (app != null) {
                if (mStops.size() > 0) {
                    app.setFirstLaunchOfDay(App.FirstLaunchType.Lines, false);
                    app.setFirstLaunchOfDay(App.FirstLaunchType.Stops, false);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                publishProgress("4", getString(R.string.configure_stops_connections));

                for (Line line : mLines) {
                    for (int i = 0; i < mStops.size(); i++) {
                        Stop stop = mStops.get(i);
                        for (int j = 0; j < stop.getConnections().size(); j++) {
                            if (stop.getConnections().get(j).getName().equalsIgnoreCase(line.getName())) {
                                stop.getConnections().get(j).setColor(line.getColor());
                            }
                        }
                    }
                }

                List<Line> lineConnections = new ArrayList<>();
                for (Stop stop : mStops) {
                    for (Line lineConn : stop.getConnections()) {
                        if (!lineConnections.contains(lineConn)) {
                           /* if (lineConn.getName().equalsIgnoreCase("L") || lineConn.getName().equalsIgnoreCase("U"))
                            {
                                Log.d("LIGNE", lineConn.getName() + " => " + lineConn.getArrivalStop().getName());
                            }*/
                            lineConnections.add(lineConn);
                        }
                    }
                }

                publishProgress("5", getString(R.string.stops_saving_offline));

                ConnectionDAO connDAO = new ConnectionDAO(DatabaseHelper.getInstance(getActivity()));
                List<Connection> oldConnections = connDAO.getAllFavorites();


                PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(DatabaseHelper.getInstance(getActivity()));
                physicalStopDAO.deleteAll();

                List<Stop> stopsAsset = new ArrayList<>();
                try {
                    StringBuilder buf = new StringBuilder();
                    InputStream json = getActivity().getAssets().open("sbb-tpg.json");
                    BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
                    String str;
                    while ((str = in.readLine()) != null) {
                        buf.append(str);
                    }

                    in.close();

                    JSONObject jsonObject = new JSONObject(buf.toString());
                    JSONArray stopsJSONArray = jsonObject.optJSONArray("stops");

                    for (int i = 0; i < stopsJSONArray.length(); i++)
                    {
                        JSONObject stopJSON = stopsJSONArray.optJSONObject(i);
                        Stop stopFromAsset = new Stop();
                        stopFromAsset.setName(stopJSON.optString("tpg",""));
                        stopFromAsset.setCFF(stopJSON.optString("sbb",""));
                        stopsAsset.add(stopFromAsset);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }



                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                List<Stop> favorites = stopDAO.getAllFavorites(false, false);

                for (Stop stop : mStops) {
                    for (Stop favorite : favorites) {
                        if (stop.getMnemo().getName().equalsIgnoreCase(favorite.getMnemo().getName())) {
                            stop.setFavorite(favorite.isFavorite());
                            stop.setFavoriteDetailled(favorite.isFavoriteDetailled());
                            stop.setFavoriteNumber(favorite.getFavoriteNumber());
                        }
                    }
                    for (Stop stopAsset : stopsAsset)
                    {
                        if (stop.getName().equalsIgnoreCase(stopAsset.getName()))
                        {
                            stop.setCFF(stopAsset.getCFF());
                        }
                    }
                }
                stopDAO.deleteAll();
                stopDAO.create(mStops);

                publishProgress("6", getString(R.string.lines_saving_offline));


                LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
                lineDAO.deleteAll();
                lineDAO.create(lineConnections);


                publishProgress("7", getString(R.string.connections_saving_offline));



                List<Connection> realConnections = new ArrayList<>();

                int i = 0;
                while (i < lineConnections.size()) {
                    final Line TmpConn = lineConnections.get(i);

                    for (Stop stop : mStops) {
                        for (PhysicalStop physicalStop : stop.getPhysicalStops()) {
                            for (Line stopLine : physicalStop.getConnections()) {
                                if (stopLine.equals(TmpConn)) {
                                    Connection newConn = new Connection();
                                    newConn.setPhysicalStop(physicalStop);
                                    newConn.setLine(TmpConn);

                                    if (newConn.getLine().getName().equalsIgnoreCase("10"))
                                    {
                                        Log.d("dds","2332");
                                    }
                                    for (Connection oldConnection : oldConnections)
                                    {
                                        if (oldConnection.equals(newConn))
                                        {
                                            newConn.setFavorite(oldConnection.isFavorite());
                                          //  Log.d("CONN", "SAME CONN");
                                        }
                                        else
                                        {
                                           // Log.d("OLD CONN", oldConnection.toString());
                                           // Log.d("NEW CONN", newConn.toString());
                                        }
                                    }

                                    realConnections.add(newConn);
                                    break;
                                }
                            }
                        }
                    }
                    i++;
                }


                connDAO.deleteAll();
                connDAO.create(realConnections);

                publishProgress("8", getString(R.string.busted_stops_saving_offline));
                BustedStopDAO bustedStopDAO = new BustedStopDAO(DatabaseHelper.getInstance(getContext()));
                bustedStopDAO.deleteAll();
                bustedStopDAO.create(mBustedStops);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_stops);
    }

    private class StopsAdapterClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Stop stop = mStopsAdapter.getItem(position);
            if (stop == null)
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
                        Bundle bundle = LinesDialogFragment.createBundle(stop.getConnections(),true);
                        getMainActivity().openDialogFragment(MainActivity.DialogFragmentName.Lines,bundle);
                    }
                }
                return;
            }
            else if (id == R.id.favoriteIV)
            {
                stop.setFavorite(!stop.isFavorite());
                changeStopFavorite(stop, position, false);
            }
            else
            {
                NextDeparturesFragment nextDeparturesFragment = NextDeparturesFragment.newInstance();
                Bundle bundle = NextDeparturesFragment.createBundle(stop.getMnemo().getName());
                nextDeparturesFragment.setArguments(bundle);

                changeFragment(nextDeparturesFragment,true);
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
            if (mStopsAdapter == null) {
                return false;
            }

            Stop stop = mStopsAdapter.getItem(position);
            if (stop == null ||stop.getId() < 1) {
                return false;
            }

            return configureFavoriteDetails(stop, position);
        }
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
            mStopsAdapter.notifyItemChanged(argPosition);
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

    private boolean configureFavoriteDetails(Stop argStop, int argPosition) {
        showConnections(argStop, argPosition);
        return true;
    }


}
