package com.mytpg.program.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.mytpg.engines.data.dao.DirectionDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.directions.Direction;
import com.mytpg.engines.entities.interfaces.ILocationClickAdapter;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.LocationSettings;
import com.mytpg.engines.settings.RequestCodeSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.DirectionsAdapter;
import com.mytpg.program.fragments.core.BaseFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class DirectionsFragment extends BaseFragment {
    private final static String ARG_SAVED_LOC = "savedLoc";
    private final static String ARG_SAVED_FROM_LOC = "savedFromLoc";
    private final static String ARG_SAVED_TO_LOC = "savedToLoc";
    private final static String ARG_SAVED_LAST_POSITION_ASKED = "savedLastPositionAsked";
    private final static String ARG_SAVED_IS_FROM_LAST_ASKED = "savedIsFromLastAsked";
    private final static String ARG_SAVED_FROM_STOP = "savedFromStop";
    private final static String ARG_SAVED_TO_STOP = "savedToStop";
    private final static String ARG_SAVED_DIRECTIONS = "savedDirections";


    private List<Stop> mStops = new ArrayList<>();
    private DirectionsAdapter mDirectionsAdapter = null;
    private RecyclerView mMainRV = null;
    private RecyclerView.LayoutManager mLayManager = null;

    private Location mLoc = null;
    private Location mFromLoc = null;
    private Location mToLoc = null;
    private LocationManager mLocManager = null;
    private int mLastPositionAsked = -1;
    private boolean mIsFromLastAsked = true;
    private Stop mFromStop = null;
    private Stop mToStop = null;

    private DirectionClickListener mDirectionClickListener = new DirectionClickListener();
    private List<Direction> mDirections = new ArrayList<>();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_SAVED_LOC,mLoc);
        outState.putParcelable(ARG_SAVED_FROM_LOC, mFromLoc);
        outState.putParcelable(ARG_SAVED_TO_LOC, mToLoc);
        outState.putInt(ARG_SAVED_LAST_POSITION_ASKED, mLastPositionAsked);
        outState.putBoolean(ARG_SAVED_IS_FROM_LAST_ASKED, mIsFromLastAsked);
        outState.putParcelable(ARG_SAVED_FROM_STOP, mFromStop);
        outState.putParcelable(ARG_SAVED_TO_STOP, mToStop);
        if (mDirectionsAdapter != null) {
            outState.putParcelableArrayList(ARG_SAVED_DIRECTIONS, mDirectionsAdapter.getDirectionsToSave());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLocManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);


        ArrayList<Direction> directions = null;
        if (savedInstanceState != null)
        {
            directions = savedInstanceState.getParcelableArrayList(ARG_SAVED_DIRECTIONS);
            mLoc = savedInstanceState.getParcelable(ARG_SAVED_LOC);
            mFromLoc = savedInstanceState.getParcelable(ARG_SAVED_FROM_LOC);
            mToLoc = savedInstanceState.getParcelable(ARG_SAVED_TO_LOC);
            mLastPositionAsked = savedInstanceState.getInt(ARG_SAVED_LAST_POSITION_ASKED,-1);
            mIsFromLastAsked = savedInstanceState.getBoolean(ARG_SAVED_IS_FROM_LAST_ASKED,true);
            mFromStop = savedInstanceState.getParcelable(ARG_SAVED_FROM_STOP);
            mToStop = savedInstanceState.getParcelable(ARG_SAVED_TO_STOP);
        }

        if (directions == null || directions.isEmpty())
        {
            loadData();
        }
        else
        {
            if (mStops == null)
            {
                mStops = new ArrayList<>();
            }

            if (mDirectionsAdapter == null)
            {
                mDirectionsAdapter = new DirectionsAdapter(getActivity(), directions, mStops);
                mDirectionsAdapter.setOnItemClickListener(mDirectionClickListener);
            }
            else {
                mDirectionsAdapter.setDirections(directions);
            }

            if (mStops.isEmpty())
            {
                loadAutoSuggest(false);
            }

            mMainRV.setAdapter(mDirectionsAdapter);

            //mOSectionsAdapter.setOnItemClickListener(new OSectionsAdapterClickListener());

        }

        updateFabVisibility(true);
        updateFabDrawable(R.drawable.ic_action_add);
    }

    @Override
    public void fabClicked() {
        addDirection();
    }

    private void addDirection()
    {
        Direction direction = new Direction();
        if (mDirectionsAdapter != null) {
            if (mDirectionsAdapter.getStopsAutoSuggestAdapter() == null)
            {
                loadAutoSuggest(true);
                return;
            }
            mDirectionsAdapter.add(0, direction);
            mMainRV.scrollToPosition(0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_directions, container, false);

        mMainRV = (RecyclerView) rootView.findViewById(R.id.mainRV);
        mLayManager = new LinearLayoutManager(getActivity());
        mMainRV.setLayoutManager(mLayManager);

       /* mResearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                research();
            }
        });


        mFromGPSIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFromLastAsked = true;
                if (askLocation())
                {
                    try {
                        loadLocation(true);
                    }
                    catch (SecurityException se)
                    {
                        se.printStackTrace();
                    }
                }
            }
        });

        mToGPSIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFromLastAsked = false;
                if (askLocation())
                {
                    try {
                        loadLocation(false);
                    }
                    catch (SecurityException se)
                    {
                        se.printStackTrace();
                    }
                }
            }
        });

        mDateTimeET.setFocusable(false);

        initializeData();*/

        return rootView;
    }

    private void loadData()
    {
        initializeData();
    }

    private void prepareResearch(int argPosition) {
        if (mDirectionsAdapter == null)
        {
            return;
        }

        String from = mDirectionsAdapter.getItem(argPosition).getFrom();
        String to = mDirectionsAdapter.getItem(argPosition).getTo();

        if (from.isEmpty() || to.isEmpty())
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(getString(R.string.incomplete_fields));
            adb.setMessage(getString(R.string.please_complete_all_fields));
            adb.setPositiveButton(getString(android.R.string.ok), null);
            adb.show();
            return;
        }

        mFromStop = stopByText(from);
        mToStop = stopByText(to);

        if (mFromStop == null)
        {
            String address = searchAddress(from,true);
            mDirectionsAdapter.getItem(argPosition).setFrom(address);
        }
        if (mToStop == null)
        {
            String address = searchAddress(to,true);
            mDirectionsAdapter.getItem(argPosition).setTo(address);
        }

        mDirectionsAdapter.notifyItemChanged(argPosition);

        Direction direction = mDirectionsAdapter.getItem(argPosition);
        research(direction);
    }

    private void research(Direction argDirection) {
        if (argDirection == null || argDirection.getFrom().isEmpty() || argDirection.getTo().isEmpty())
        {
            return;
        }

        Bundle bundle = ShowDirectionsFragment.createBundle(argDirection);
        ShowDirectionsFragment sdf = new ShowDirectionsFragment();
        sdf.setArguments(bundle);
        changeFragment(sdf,true);
    }

    private String searchAddress(String argSearchText, boolean argIsFrom) {
        Geocoder geoCoder = new Geocoder(getActivity());
        String address = argSearchText;
        try {
            List<Address> addresses = geoCoder.getFromLocationName(argSearchText, 5);
            if (addresses.size() > 0) {
                for (Address cAddress : addresses) {
                    String thoroughfare = cAddress.getThoroughfare();
                    if (thoroughfare != null) {

                    }
                }
                Address firstAddress = null;

                int i = 0;
                while (i < addresses.size()) {
                    if (addresses.get(i) != null) {
                        firstAddress = addresses.get(i);
                        break;
                    }

                    i++;
                }

                if (firstAddress != null) {
                    Location loc = new Location(LocationSettings.LOCATION_PROVIDER);

                    if (firstAddress.getMaxAddressLineIndex() != -1) {
                        address = firstAddress.getAddressLine(0);
                    }

                    if (address.isEmpty()) {
                        address = firstAddress.getThoroughfare() + " " + firstAddress.getSubThoroughfare();
                    }
                    loc.setLatitude(firstAddress.getLatitude());
                    loc.setLongitude(firstAddress.getLongitude());

                    if (argIsFrom)
                    {
                        mFromLoc = loc;
                    }
                    else
                    {
                        mToLoc = loc;
                    }

                    Log.d("LOC", loc.toString());
                }
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d("ADDRESS FOUND",address);
        return address;
    }

    private String searchAddressByLocation(Location argLocation, boolean argIsFrom)
    {
        String address = "";
        Geocoder geoCoder = new Geocoder(getActivity());
        try {
            List<Address> addresses = geoCoder.getFromLocation(argLocation.getLatitude(), argLocation.getLongitude(), 5);
            if (addresses.size() > 0) {
                for (Address cAddress : addresses) {
                    String thoroughfare = cAddress.getThoroughfare();
                    if (thoroughfare != null) {

                    }
                }
                Address firstAddress = null;

                int i = 0;
                while (i < addresses.size()) {
                    if (addresses.get(i) != null) {
                        firstAddress = addresses.get(i);
                        break;
                    }

                    i++;
                }

                if (firstAddress != null) {
                    Location loc = new Location(LocationSettings.LOCATION_PROVIDER);

                    if (firstAddress.getMaxAddressLineIndex() != -1) {
                        address = firstAddress.getAddressLine(0);
                    }

                    if (address.isEmpty()) {
                        address = firstAddress.getThoroughfare() + " " + firstAddress.getSubThoroughfare();
                    }
                    loc.setLatitude(firstAddress.getLatitude());
                    loc.setLongitude(firstAddress.getLongitude());

                    if (argIsFrom)
                    {
                        mFromLoc = loc;
                    }
                    else
                    {
                        mToLoc = loc;
                    }

                    Log.d("LOC", loc.toString());
                }
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d("ADDRESS FOUND",address);
        return address;
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_directions);
    }

    @Override
    public boolean needToBeFullViewport() {
        return false;
    }

    private Stop stopByText(String argText)
    {
        if (argText == null || argText.trim().isEmpty())
        {
            return null;
        }

        argText = argText.trim();

        for (Stop stop : mStops)
        {
            if (stop.getName().equalsIgnoreCase(argText) ||
                stop.getCode().equalsIgnoreCase(argText) ||
                stop.getCFF().equalsIgnoreCase(argText))
            {
                return stop;
            }
        }

        return null;
    }

    private void initializeData() {
        loadDirectionsFromDB();
        loadAutoSuggest(false);

        /*updateDateChoose();

        mDateTimeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDate();
            }
        });*/
    }

    private void loadDirectionsFromDB() {
        final Handler handler = new Handler();
        new Runnable(){
            @Override
            public void run(){
                DirectionDAO directionDAO = new DirectionDAO(DatabaseHelper.getInstance(getActivity()));
                mDirections = directionDAO.getAll();

                Calendar now = DateTools.now();

                for (Direction direction : mDirections)
                {
                    direction.getDate().set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
                    direction.getDate().set(Calendar.MONTH, now.get(Calendar.MONTH));
                    direction.getDate().set(Calendar.YEAR, now.get(Calendar.YEAR));
                }
                
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateAdapter();
                    }
                });
            }
            
        }.run();
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    private void loadLocation(int argPosition, boolean argIsFrom){
        try {
            boolean oneActivate = false;
            if (mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocListener(argPosition, argIsFrom));
                oneActivate = true;
            }
            if (mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, new LocListener(argPosition, argIsFrom));
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
                        loadLocation(mLastPositionAsked, mIsFromLastAsked);
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

    private void loadAutoSuggest(final boolean argNeedAddOneDirection) {
       /* if (mFromStopsAutoSuggestAdapter != null)
        {
            mFromACTV.setAdapter(mFromStopsAutoSuggestAdapter);
        }
        if (mToStopsAutoSuggestAdapter != null)
        {
            mToACTV.setAdapter(mToStopsAutoSuggestAdapter);
        }

        if (mFromStopsAutoSuggestAdapter != null && mToStopsAutoSuggestAdapter != null)
        {
            return;
        }*/

        final Handler handler = new Handler();
        new Runnable(){
            @Override
            public void run()
            {
                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                mStops = stopDAO.getAll(true,true,false);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateAdapter();

                        if (argNeedAddOneDirection)
                        {
                            addDirection();
                        }
                    }
                });
            }
        }.run();
    }
    
    private void updateAdapter()
    {
        if (mDirectionsAdapter == null) {
            mDirectionsAdapter = new DirectionsAdapter(getActivity(), mDirections, new ArrayList<>(mStops));
        }
        else
        {
            mDirectionsAdapter.setItems(mDirections);
            mDirectionsAdapter.setStops(new ArrayList<Stop>(mStops));
        }
        mDirectionsAdapter.setOnItemClickListener(mDirectionClickListener);
        mDirectionsAdapter.setOnLocClickListener(mDirectionClickListener);
        mMainRV.setAdapter(mDirectionsAdapter);
    }

    @Override
    public void search(String argSearchText) {

    }

    private class LocListener implements LocationListener
    {
        private boolean mIsFrom = true;
        private int mPosition = -1;

        public LocListener(int argPosition, boolean argIsFrom)
        {
            mPosition = argPosition;
            mIsFrom = argIsFrom;
        }

        @Override
        public void onLocationChanged(Location location) {
            try {
                mLocManager.removeUpdates(this);

                mLoc = location;
                if (mIsFrom)
                {
                    mFromLoc = new Location(location);
                }
                else
                {
                    mToLoc = new Location(location);
                }

                String address = searchAddressByLocation(mLoc, mIsFrom);
                if (mIsFrom) {
                    mDirectionsAdapter.getItem(mPosition).setFrom(address);
                } else {
                    mDirectionsAdapter.getItem(mPosition).setTo(address);
                }
                mDirectionsAdapter.notifyItemChanged(mPosition);
                /*final Handler handler = new Handler();
                new Runnable(){
                    @Override
                    public void run()
                    {
                        StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));

                        List<Stop> tmpStops = stopDAO.getByLocation(mLoc,10);

                        int index = -1;
                        float currentDistance = 9999999f;
                        for (int i = 0; i < tmpStops.size(); i++)
                        {
                            float distance = mLoc.distanceTo(tmpStops.get(i).getPhysicalStops().get(0).getLocation());
                            if (distance < currentDistance)
                            {
                                currentDistance = distance;
                                index = i;
                            }
                        }

                        final String stopName = tmpStops.get(index).getName();


                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mDirectionsAdapter != null) {
                                    if (mIsFrom) {
                                        mDirectionsAdapter.getItem(mPosition).setFrom(stopName);
                                    } else {
                                        mDirectionsAdapter.getItem(mPosition).setTo(stopName);
                                    }
                                    mDirectionsAdapter.notifyItemChanged(mPosition);
                                }
                            }
                        });
                    }
                }.run();*/

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

    private class DirectionClickListener implements AdapterView.OnItemClickListener, ILocationClickAdapter
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Direction direction = mDirectionsAdapter.getItem(position);
            if (direction == null)
            {
                return;
            }

            if (id == R.id.researchIV) {
                prepareResearch(position);
                return;
            }
            
            if (id == R.id.saveIV)
            {
                save(position);
                return;
            }

            if (id == R.id.deleteIV)
            {
                delete(position);
                return;
            }
        }

        @Override
        public void onLocationAsked(int argPosition) {

        }

        @Override
        public void onLocationAsked(int argPosition, boolean argIsFrom) {
            if (askLocation())
            {
                mIsFromLastAsked = argIsFrom;
                mLastPositionAsked = argPosition;
                try {
                    loadLocation(argPosition, argIsFrom);
                }
                catch (SecurityException se)
                {
                    se.printStackTrace();
                }
            }
        }
    }

    private void delete(int argPosition) {
        Direction direction = null;

        if (mDirectionsAdapter != null) {
            direction = mDirectionsAdapter.getItem(argPosition);
        }

        String text = getString(R.string.direction_not_deleted);

        if (direction != null)
        {
            DirectionDAO directionDAO = new DirectionDAO(DatabaseHelper.getInstance(getActivity()));
            if (directionDAO.delete(direction))
            {
                text = getString(R.string.direction_deleted);
                mDirectionsAdapter.remove(argPosition);
            }
        }

        Snackbar.make(getView(), text, Snackbar.LENGTH_SHORT).show();
    }

    private void save(int argPosition) {
        Direction direction = null;

        if (mDirectionsAdapter != null) {
            direction = mDirectionsAdapter.getItem(argPosition);
        }

        String text = getString(R.string.direction_not_saved);

        if (direction != null)
        {
            boolean result;
            DirectionDAO directionDAO = new DirectionDAO(DatabaseHelper.getInstance(getActivity()));
            if (direction.getId() == -1)
            {
                result = directionDAO.create(direction);
            }
            else
            {
                result = directionDAO.update(direction);
            }
            
            if (result)
            {
                text = getString(R.string.direction_saved);
                mDirectionsAdapter.notifyItemChanged(argPosition);
            }
        }

        Snackbar.make(getView(), text, Snackbar.LENGTH_SHORT).show();
    }

}
