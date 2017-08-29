package com.mytpg.program.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mytpg.engines.data.api.bustedapp.AlertAPI;
import com.mytpg.engines.data.dao.bustedapp.BustedStopDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.data.interfaces.listeners.ISearchListener;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.bustedapp.Alert;
import com.mytpg.engines.entities.bustedapp.BustedStop;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.RequestCodeSettings;
import com.mytpg.engines.tools.LocationTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.bustedapp.AlertsAdapter;
import com.mytpg.program.adapters.bustedapp.MapAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public class BustedFragment extends BaseFragment implements OnMapReadyCallback{
    private static final String ARG_SAVED_LOCATION = "savedLocation";
    private static final String ARG_SAVED_ALERTS = "savedAlerts";
    private static final String ARG_WANTS_SEARCH = "wantsSearch";
    private static final String ARG_SAVED_WANTS_SEARCH = "savedWantsSearch";

    private GoogleMap mMap;
    private MapView mMapView;
    private boolean mMapIsReady = false;
    private List<Alert> mAlerts = new ArrayList<>();
    private List<BustedStop> mBustedStops = new ArrayList<>();
    private AlertsAdapter mAlertsAdapter = null;
    private MapAdapter mMapAdapter = null;
    private LocationManager mLocManager = null;
    private Location mLoc = null;
    private LocListener mLocListener = new LocListener();
    private MapAdapterClickListener mMapAdapterClickListener = new MapAdapterClickListener();
    private RecyclerView mMainRV = null;
    private LinearLayoutManager mLayoutManager;

    public BustedFragment()
    {
        super();
    }

    public static Bundle createBundle(String argWantsSearchText)
    {
        Bundle bundle = new Bundle();

        bundle.putString(ARG_WANTS_SEARCH,argWantsSearchText);

        return bundle;
    }

    public static BustedFragment newInstance()
    {
        BustedFragment mf = new BustedFragment();
        return mf;
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_busted);
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


        View rootView = inflater.inflate(R.layout.fragment_busted, container, false);

        TextView poweredByTV = (TextView)rootView.findViewById(R.id.poweredByBustedTV);
        mMapView = (MapView)rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mMainRV = (RecyclerView)rootView.findViewById(R.id.mainRV);
        mMainRV.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());// new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mMainRV.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAlertsAdapter = new AlertsAdapter(getActivity(),null);
        mMainRV.setAdapter(mAlertsAdapter);

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

        String html = "Powered by <a href=\"https://play.google.com/store/apps/details?id=com.busted.app\">Busted App</a>";
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        poweredByTV.setText(result);
        poweredByTV.setMovementMethod(LinkMovementMethod.getInstance());

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

        loadBustedStopsFromDB();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.search, menu);
        inflater.inflate(R.menu.signal, menu);
        inflater.inflate(R.menu.maps, menu);
        /*if (getMainActivity() != null)
        {
            getMainActivity().initializeSearchView();
        }*/
    }

    private void loadAlerts() {
        AlertAPI alertAPI = new AlertAPI(getActivity());
        alertAPI.getAll(new IAPIListener<Alert>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissPD();
            }

            @Override
            public void onSuccess(Alert argObject) {
            }

            @Override
            public void onSuccess(List<Alert> argObjects) {
                mAlerts = argObjects;
                mAlertsAdapter = new AlertsAdapter(getActivity(), mAlerts);
                mAlertsAdapter.setOnItemClickListener(new AlertsAdapterClickListener());
                mMainRV.setAdapter(mAlertsAdapter);
                mMapAdapter = new MapAdapter(getActivity(), mAlerts, new BustedFragment.MapListener());
                mMapAdapter.setOnItemClickListener(mMapAdapterClickListener);
                showAlertsOnMap();

                //mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadBustedStopsFromDB()
    {

        final Handler handler = new Handler();
        Runnable bustedStopsRunnable = new Runnable() {
            @Override
            public void run() {
                BustedStopDAO stopDAO = new BustedStopDAO(DatabaseHelper.getInstance(getActivity()));
                mBustedStops = stopDAO.getAll(true,true,false);

                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        //dismissPD();

                       // mSwipeRefreshLayout.setRefreshing(false);
                        loadAlerts();
                    }
                });
            }
        };
        bustedStopsRunnable.run();

    }

    private void showAlertsOnMap() {

        if (mMapIsReady && !mAlerts.isEmpty())
        {
            mMapAdapter.setMap(mMap);
            mMap.setInfoWindowAdapter(mMapAdapter);
            LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

            mMap.clear();
            for (Alert alert : mAlerts)
            {
                LatLng latLng = LocationTools.locToLatLng(alert.getLocation());
                boundsBuilder.include(latLng);
                String imageIdentifier = "ic_" + alert.getCategory().getImageName();
                int markerImageId = getResources().getIdentifier(imageIdentifier, "drawable", getContext().getPackageName());
                Log.d("MARKER IMAGE ID", String.valueOf(markerImageId) + " FOR " + imageIdentifier);
                Marker marker = null;
                if (markerImageId != 0) {
                    marker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(alert.getCategory().getName())
                            .icon(BitmapDescriptorFactory.fromResource(markerImageId)));
                }
                else
                {
                    marker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(alert.getCategory().getName()));
                }
               // Log.d("NAME", alert.getCategory().getName());
                alert.setMarker(marker);
                //mMapAdapter.addMarker(marker);

            }
            LatLngBounds bounds = boundsBuilder.build();
           // mMap.setLatLngBoundsForCameraTarget(bounds);

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));


        }
        dismissPD();

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

    private class MapAdapterClickListener implements AdapterView.OnItemClickListener, GoogleMap.OnInfoWindowClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Alert alert = mMapAdapter.getItem(position);

        }

        @Override
        public void onInfoWindowClick(Marker marker) {
            if (mMapAdapter == null)
            {
                return;
            }

            Alert alert = mMapAdapter.alertAtLatLng(marker.getPosition());

        }
    }

    private class AlertsAdapterClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Alert alert = mMapAdapter.getItem(position);

            Log.d("ALERT CLICK", alert.toString());
        }
    }
}
