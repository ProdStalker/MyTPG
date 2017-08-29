package com.mytpg.program.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mytpg.engines.data.api.LineAPI;
import com.mytpg.engines.data.api.StopAPI;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.RequestCodeSettings;
import com.mytpg.engines.tools.StopTools;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.adapters.ProximityAdapter;
import com.mytpg.program.dialogs.LinesDialogFragment;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class ProximityFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener{
    private final static String ARG_SAVED_STOPS = "savedStops";
    private final static String ARG_SAVED_LINES = "savedLines";
    private final static String ARG_SAVED_LOC = "savedLoc";

    private RecyclerView mRecyclerView;
    //private RecyclerView mRecentStopsRV;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mRecentStopsLM;
    private ProximityAdapter mProximityAdapter;
    // private ProximityAdapter mRecentProximityAdapter;

    private List<Line> mLines = new ArrayList<>();
    private List<Stop> mStops = new ArrayList<>();
    private Location mLoc = null;
    private LocationManager mLocManager = null;
    private LocationListener mLocListener = new LocListener();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            mStops = savedInstanceState.getParcelableArrayList(ARG_SAVED_STOPS);
            mLines = savedInstanceState.getParcelableArrayList(ARG_SAVED_LINES);
            mLoc = savedInstanceState.getParcelable(ARG_SAVED_LOC);
        }

        if (mStops == null || mStops.isEmpty())
        {
            loadData();
        }
        else
        {
            if (mProximityAdapter == null)
            {
                mProximityAdapter = new ProximityAdapter(getActivity(),mLoc,mStops);
                mProximityAdapter.setOnItemClickListener(new ProximityAdapterClickAdapter());
            }
            else
            {
                mProximityAdapter.setLoc(mLoc);
                mProximityAdapter.setStops(mStops);
            }

            mRecyclerView.setAdapter(mProximityAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mProximityAdapter != null) {
            outState.putParcelableArrayList(ARG_SAVED_STOPS,mProximityAdapter.getStopsToSave());
        }

        ArrayList<Line> lines = new ArrayList<>();
        for (Line line : mLines)
        {
            lines.add(line);
        }

        outState.putParcelableArrayList(ARG_SAVED_LINES, lines);

        outState.putParcelable(ARG_SAVED_LOC, mLoc);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_proximity, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                                                    R.color.colorAccent,
                                                    R.color.colorPrimaryDark);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.mainRV);
        // mRecentStopsRV = (RecyclerView)rootView.findViewById(R.id.recentStopsRV);

        mRecyclerView.setHasFixedSize(true);
        // mRecentStopsRV.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecentStopsLM = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mLocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // mRecentStopsRV.setLayoutManager(mRecentStopsLM);


        return rootView;
    }

    private void loadData() {
        if (askLocation())
        {
            try {
                loadLocation();
            }
            catch (SecurityException se)
            {
                se.printStackTrace();
            }
        }

    }

    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    private void loadLocation(){
        try {
            boolean oneActivate = false;
            if (mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
                oneActivate = true;
            }
            if (mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, mLocListener);
                oneActivate = true;
            }

            if (!oneActivate)
            {
                Toast.makeText(getActivity(), getString(R.string.please_activate_location), Toast.LENGTH_LONG).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
        catch (SecurityException se)
        {
            se.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case RequestCodeSettings.REQ_PERMISSION_FINE_LOCATION :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    try {
                        loadLocation();
                    }
                    catch (SecurityException se)
                    {
                        se.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(getActivity(),getString(R.string.unable_loading),Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    public void loadLines()
    {
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
                updateLineColors();
            }
        });
    }


    private void loadStops() {
        showPD();
        StopAPI stopAPI = new StopAPI(getActivity());
        stopAPI.getAllProximity(mLoc, new IAPIListener<Stop>() {
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

                loadLines();
            }
        });

    }

    @Override
    public void search(String argSearchText) {
    }

    private void updateLineColors()
    {

      /*  for (Line line : mLines)
        {
            for (int i = 0; i < mProximityAdapter.getItemCount(); i++) {
                Stop stop = mProximityAdapter.getItem(i);
                for (int j = 0; j < stop .getConnections().size(); j++) {
                    if (stop.getConnections().get(j).getName().equalsIgnoreCase(line.getName()))
                    {
                        stop.getConnections().get(j).setColor(line.getColor());
                        mProximityAdapter.notifyItemChanged(i);
                    }
                }

            }
        }*/

       final Handler handler = new Handler();
        new Runnable() {

            @Override
            public void run() {
                for (Line line : mLines)
                {
                    for (int i = 0; i < mStops.size(); i++) {
                        Stop stop = mStops.get(i);
                        for (int j = 0; j < stop.getConnections().size(); j++) {
                            if (stop.getConnections().get(j).getName().equalsIgnoreCase(line.getName()))
                            {
                                stop.getConnections().get(j).setColor(line.getColor());
                            }
                        }
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProximityAdapter = new ProximityAdapter(getActivity(), mLoc, mStops);
                        mRecyclerView.setAdapter(mProximityAdapter);
                        mProximityAdapter.setOnItemClickListener(new ProximityAdapterClickAdapter());

                       /* List<Stop> recentStops = new ArrayList<Stop>();

                        Random rn = new Random();
                        for (int i = 0; i < 5; i++)
                        {
                            int randomNumber = rn.nextInt(mStops.size());
                            recentStops.add(new Stop(mStops.get(randomNumber)));
                        }
                        mRecentProximityAdapter = new ProximityAdapter(getActivity(), recentStops);
                        mRecentStopsRV.setAdapter(mRecentProximityAdapter); */

                        dismissPD();
                    }
                });

            }
        }.run();

        //mProximityAdapter


    }

    @Override
    public void onRefresh() {
        if (askLocation())
        {
            try {
                loadLocation();
            }
            catch (SecurityException se)
            {
                se.printStackTrace();
            }
        }
    }

    private class LocListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location location) {
            try {
                mLocManager.removeUpdates(this);

                mLoc = location;
                loadStopsFromDB();
            }
            catch (SecurityException se)
            {
                se.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private void loadStopsFromDB() {

        mSwipeRefreshLayout.setRefreshing(true);
        final Handler handler = new Handler();
        Runnable stopsRunnable = new Runnable() {
            @Override
            public void run() {
                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                mStops = stopDAO.getByLocation(mLoc,10);
                mStops = StopTools.sortStopsByDistance(mStops,mLoc);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProximityAdapter = new ProximityAdapter(getActivity(),mLoc, mStops);
                        mRecyclerView.setAdapter(mProximityAdapter);
                        mProximityAdapter.setOnItemClickListener(new ProximityAdapterClickAdapter());

                        dismissPD();

                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        };
        stopsRunnable.run();
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_proximity);
    }

    private class ProximityAdapterClickAdapter implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Stop stop = mProximityAdapter.getItem(position);
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
                boolean changeSuccess;
                stop.setFavorite(!stop.isFavorite());
                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                if (stop.isFavorite())
                {
                    changeSuccess = stopDAO.addFavorite(stop, false);
                }
                else
                {
                    changeSuccess = stopDAO.removeFavorite(stop);
                }

                String textChange = getString(R.string.favorite_stop_added);
                if (changeSuccess)
                {
                    if (!stop.isFavorite())
                    {
                        textChange = getString(R.string.favorite_stop_removed);
                    }
                    mProximityAdapter.notifyItemChanged(position);

                }
                else
                {
                    if (stop.isFavorite())
                    {
                        textChange = getString(R.string.favorite_stop_error_added);
                    }
                    else {
                        textChange = getString(R.string.favorite_stop_error_removed);
                    }
                    stop.setFavorite(!stop.isFavorite());
                }

                Snackbar.make(getView(), textChange, Snackbar.LENGTH_SHORT).show();
            }
            else
            {
                NextDeparturesFragment nextDeparturesFragment = NextDeparturesFragment.newInstance();
                Bundle bundle = NextDeparturesFragment.createBundle(stop.getMnemo().getName());
                nextDeparturesFragment.setArguments(bundle);

                changeFragment(nextDeparturesFragment,true);
            }
        }
    }
}
