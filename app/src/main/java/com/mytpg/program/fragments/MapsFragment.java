package com.mytpg.program.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mytpg.engines.data.api.LineAPI;
import com.mytpg.engines.data.api.StopAPI;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.data.interfaces.listeners.ISearchListener;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.RequestCodeSettings;
import com.mytpg.engines.tools.LocationTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.MapAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public class MapsFragment extends BaseFragment implements OnMapReadyCallback{
    private static final String ARG_SAVED_LOCATION = "savedLocation";
    private static final String ARG_SAVED_LINES = "savedLines";
    private static final String ARG_SAVED_STOPS = "savedStops";
    private static final String ARG_WANTS_SEARCH = "wantsSearch";
    private static final String ARG_SAVED_WANTS_SEARCH = "savedWantsSearch";

    private GoogleMap mMap;
    private MapView mMapView;
    private boolean mMapIsReady = false;
    private List<Stop> mStops = new ArrayList<>();
    private List<Line> mLines = new ArrayList<>();
    private MapAdapter mMapAdapter = null;
    private LocationManager mLocManager = null;
    private Location mLoc = null;
    private LocListener mLocListener = new LocListener();
    private MapAdapterClickListener mMapAdapterClickListener = new MapAdapterClickListener();

    public MapsFragment()
    {
        super();
    }

    public static Bundle createBundle(String argWantsSearchText)
    {
        Bundle bundle = new Bundle();

        bundle.putString(ARG_WANTS_SEARCH,argWantsSearchText);

        return bundle;
    }

    public static MapsFragment newInstance()
    {
        MapsFragment mf = new MapsFragment();
        return mf;
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_map);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLocManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (savedInstanceState != null)
        {
            mWantsSearchText = savedInstanceState.getString(ARG_SAVED_WANTS_SEARCH);
        }
        else
        {
            Bundle bundle = getArguments();
            if (bundle != null)
            {
                mWantsSearchText = bundle.getString(ARG_WANTS_SEARCH);
            }
        }
        loadData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView)rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        /*GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();

        int result = googleAPI.isGooglePlayServicesAvailable(getActivity());
        if (result != ConnectionResult.SUCCESS)
        {
            if (googleAPI.isUserResolvableError(result))
            {
                googleAPI.getErrorDialog(getActivity(), result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
        }*/
        /*switch (googleAPI.isGooglePlayServicesAvailable(getActivity()))
        {
            case ConnectionResult.SUCCESS :
                mMapView = (MapView)rootView.findViewById(R.id.map);

                break;

            case ConnectionResult.SERVICE_MISSING :
                Toast.makeText(getActivity(), "Service Missing", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED :
                Toast.makeText(getActivity(), "Update required", Toast.LENGTH_SHORT).show();
                break;
            default :
                Toast.makeText(getActivity(), googleAPI.isGooglePlayServicesAvailable(getActivity()), Toast.LENGTH_SHORT).show();
        }*/

        return rootView;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    public void loadData()
    {
        showPD();

        App app = getApp();
        if (app == null)
        {
            return;
        }

        final boolean isFirstLaunchOfDayLines = app.isFirstLaunchOfDay(App.FirstLaunchType.Lines);
        final boolean isFirstLaunchOfDayStops = app.isFirstLaunchOfDay(App.FirstLaunchType.Stops);

        if (isFirstLaunchOfDayStops || isFirstLaunchOfDayLines) {
            loadStops();
        }
        else
        {
            loadStopsFromDB();
        }
    }

    private void loadStopsFromDB()
    {
        new LoadStopsAsyncTask().execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);
        inflater.inflate(R.menu.maps, menu);
        if (getMainActivity() != null)
        {
            getMainActivity().initializeSearchView();
        }
    }

    private void loadStops() {
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
                loadLines();
            }
        });
    }

    private void loadLines() {
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

    private void updateLineColors() {

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
                        mMapAdapter = new MapAdapter(getActivity(), mStops, new MapListener());
                        mMapAdapter.setOnItemClickListener(mMapAdapterClickListener);

                        if (mMapIsReady)
                        {
                            mMapAdapter.setMap(mMap);
                            mMap.setInfoWindowAdapter(mMapAdapter);
                            //mMap.setInfoWindowAdapter();
                        }
                        showStopsOnMap();
                        if (mWantsSearchText == null || mWantsSearchText.isEmpty()) {
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
                        else
                        {
                            search(mWantsSearchText);
                        }
                    }
                });

            }
        }.run();



    }


    private void showStopsOnMap() {
        if (mMapIsReady && !mStops.isEmpty())
        {
            mMapAdapter.setMap(mMap);
            LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

            mMap.clear();
            for (Stop stop : mStops)
            {
                for (PhysicalStop physicalStop : stop.getPhysicalStops()) {
                    LatLng latLng = LocationTools.locToLatLng(physicalStop.getLocation());
                    boundsBuilder.include(latLng);

                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                                                        .title(stop.getName()));

                    physicalStop.setMarker(marker);
                    //mMapAdapter.addMarker(marker);
                }
            }
            LatLngBounds bounds = boundsBuilder.build();
           // mMap.setLatLngBoundsForCameraTarget(bounds);

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));


        }
        dismissPD();

        showTuto();
    }

    private void showTuto() {
        if (!isAdded())
        {
            return;
        }

        App app = getApp();
        if (app != null && app.isFirstMap())
        {
            Tutorial mapTypeTuto = new Tutorial(getString(R.string.map_type), getString(R.string.showcase_click_to_change_map_type), getActivity().findViewById(R.id.action_map_type));
            mTutorialManager.addTutorial(mapTypeTuto);

            mTutorialManager.launchTutorials();

            app.setFirstMap(false);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMapAdapter != null) {
            mMap.setInfoWindowAdapter(mMapAdapter);
        }

        mMap.setOnInfoWindowClickListener(mMapAdapterClickListener);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        int mapType = GoogleMap.MAP_TYPE_HYBRID;
        if (getApp() != null)
        {
            mapType = getApp().getSharedPreferences().getInt(AppSettings.PREF_MAP_TYPE, GoogleMap.MAP_TYPE_HYBRID);
        }
        mMap.setMapType(mapType);

        try
        {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
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
                    return true;
                }
            });
        }
        catch (SecurityException se)
        {
            se.printStackTrace();
        }


        mMapIsReady = true;
    }

    @Override
    public void search(final String argSearchText) {
        if (mMapAdapter == null)
        {
            return;
        }

        Snackbar.make(getView(), getString(R.string.searching), Snackbar.LENGTH_INDEFINITE).show();


        LatLngBounds bounds = mMapAdapter.search(argSearchText);

        if (bounds != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            Snackbar.make(getView(), getString(R.string.address_found), Snackbar.LENGTH_LONG).show();
        }
        else if (mMapAdapter.getItemCount() == 0 && !argSearchText.toLowerCase().contains("l:"))
        {
            final Handler handler = new Handler();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            search(argSearchText);
                        }
                    });
                }
            };
            Timer timer = new Timer();
            timer.schedule(timerTask, 1000);
        }
        else
        {
            Snackbar.make(getView(), getString(R.string.address_not_found), Snackbar.LENGTH_SHORT).show();
        }

        if (argSearchText.isEmpty() && mLoc != null)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LocationTools.locToLatLng(mLoc),18));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_map_type :
                changeMapType();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeMapType() {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(getContext().getResources().getStringArray(R.array.array_map_types), mMap.getMapType() - 1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int finalChoice = which+1;
                                mMap.setMapType(finalChoice);
                                if (getApp() != null)
                                {
                                    getApp().getSharedPreferences().edit().putInt(AppSettings.PREF_MAP_TYPE, finalChoice).commit();
                                }
                                dialog.dismiss();
                            }
                        }

                );
        adb.show();
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
            }
        }
        catch (SecurityException se)
        {
            se.printStackTrace();
        }
    }

    @Override
    public boolean canDynamicSearch()
    {
        return false;
    }

    private class LocListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            try {
                mLoc = location;
                if (mMapAdapter != null)
                {
                    mMapAdapter.setLastLoc(mLoc);
                }

                if (mMap != null && mMapAdapter != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapAdapter.getProximityBoundsFromPosition(location), 100));


                    mLocManager.removeUpdates(this);
                }
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

    private class MapListener implements ISearchListener.BoundsListener{
        @Override
        public void onSuccess(LatLngBounds argLatLngBounds) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(argLatLngBounds, 100));
        }

        @Override
        public void onNotFound() {

        }
    }

    private class LoadStopsAsyncTask extends AsyncTask<Void, Void, List<Stop>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {
            try {
                super.onPostExecute(stops);

                if (stops == null)
                {
                    return;
                }

                mStops = stops;
                mMapAdapter = new MapAdapter(getActivity(), mStops, new MapListener());
                mMapAdapter.setOnItemClickListener(mMapAdapterClickListener);
                new LoadDataDBAsyncTask().execute();
                if (mMapIsReady)
                {
                    mMapAdapter.setMap(mMap);
                    mMap.setInfoWindowAdapter(mMapAdapter);
                }
                showStopsOnMap();
                if (mWantsSearchText == null || mWantsSearchText.isEmpty()) {
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
                } else {
                    search(mWantsSearchText);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        @Override
        protected List<Stop> doInBackground(Void... params) {
            try {
                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                return stopDAO.getAll(true, true, false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class LoadDataDBAsyncTask extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
            for (int i = 0; i < mMapAdapter.getItemCount(); i++)
            {
                if (mMapAdapter.getItemCount() == 0)
                {
                    break;
                }

                if (i >= 0 && i < mMapAdapter.getItemCount()) {
                    if (mMapAdapter.getItem(i).getConnections().size() == 0)
                    {
                        for (PhysicalStop physicalStop : mMapAdapter.getItem(i).getPhysicalStops()) {
                            List<Line> lines = lineDAO.getAllByPhysicalStop(physicalStop.getId());
                            physicalStop.setConnections(lines);
                        }
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

    private class MapAdapterClickListener implements AdapterView.OnItemClickListener, GoogleMap.OnInfoWindowClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Stop stop = mMapAdapter.getItem(position);
            if (id == R.id.viewIV)
            {
                Bundle bundle = NextDeparturesFragment.createBundle(stop.getMnemo().getName());
                NextDeparturesFragment nextDeparturesFragment = NextDeparturesFragment.newInstance();
                nextDeparturesFragment.setArguments(bundle);
                changeFragment(nextDeparturesFragment, true);

                return;
            }
            else if (id == R.id.connectionsLinLay)
            {
                Log.d("LINE", String.valueOf(view.getTag(R.id.lineTV)));
                ArrayList<String> lineIds = new ArrayList<>();
                lineIds.add(String.valueOf(view.getTag(R.id.lineTV)));
                Bundle bundle = NextDeparturesFragment.createBundle(stop.getMnemo().getName(),
                        -1,
                        lineIds);

                NextDeparturesFragment ndf = NextDeparturesFragment.newInstance();
                ndf.setArguments(bundle);
                changeFragment(ndf,true);
            }
        }

        @Override
        public void onInfoWindowClick(Marker marker) {
            if (mMapAdapter == null)
            {
                return;
            }

            Stop stop = mMapAdapter.stopAtLatLng(marker.getPosition());
            PhysicalStop physicalStop = mMapAdapter.physicalStopAtLatLng(marker.getPosition());
            if (physicalStop == null || stop == null)
            {
                return;
            }

            ArrayList<String> lineIds = new ArrayList<>();
            for (Line line : physicalStop.getConnections()) {
                lineIds.add(String.valueOf(line.getId()));
            }

            Bundle bundle = NextDeparturesFragment.createBundle(stop.getMnemo().getName(),
                    -1,
                    lineIds);

            NextDeparturesFragment ndf = NextDeparturesFragment.newInstance();
            ndf.setArguments(bundle);
            changeFragment(ndf,true);
        }
    }
}
