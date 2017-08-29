package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.interfaces.listeners.ISearchListener;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.LocationSettings;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.engines.tools.LocationTools;
import com.mytpg.engines.tools.SortTools;
import com.mytpg.engines.tools.StopTools;
import com.mytpg.program.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class MapAdapter implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {
    private enum MarkerVisibility {NoChange,Invisible,Visible}

    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<Stop> mStops;
    private List<Stop> mOriginalsStops;
    private String mCurrentSearch = "";
    private GoogleMap mMap;
    private Marker mAddressMarker = null;
    private Location mLastLoc = null;
    private Marker mCurrentActiveMarker = null;
   // private ISearchListener.BoundsListener mSearchBoundsListener = null;
   // private List<Marker> mOriginalMarkers;

    public MapAdapter(Context argContext, List<Stop> argStops, ISearchListener.BoundsListener argSearchBoundsListener)
    {
        this.mContext = argContext;
        this.mOriginalsStops = argStops;
        this.mStops = new ArrayList<>(argStops);
        //this.mSearchBoundsListener = argSearchBoundsListener;
        //this.mOriginalMarkers = new ArrayList<>();

    }
    public void setLastLoc(Location argLoc)
    {
        mLastLoc = argLoc;
        updateDistanceOpenMarkers();
    }

    public LatLngBounds search(String argSearchText) {
        LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

        boolean isSearchByLine = argSearchText.toLowerCase().indexOf("l:") == 0;
        boolean isSearchByAddress = argSearchText.length() > 0 && mStops.isEmpty();

        if (argSearchText.length() < mCurrentSearch.length() || mCurrentSearch.length() == 0 || argSearchText.length() == 0 ||
            isSearchByLine || isSearchByAddress) {
            mStops = new ArrayList<>(mOriginalsStops);
            for (int i = mStops.size() -1; i >= 0; i--)
            {
                changeColorMarker(i, BitmapDescriptorFactory.HUE_RED, MarkerVisibility.Visible);
            }
            /*for (Marker marker : mOriginalMarkers)
            {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker());
            }*/
        }

        //mStops = new ArrayList<>(mOriginalsStops);

        mCurrentSearch = argSearchText;
        argSearchText = argSearchText.toLowerCase();

        if (argSearchText.length() > 0) {
            if (isSearchByLine)
            {
                searchByLine(argSearchText);
            }
            else if (isSearchByAddress)
            {
                return searchByAddress(argSearchText);
                /*if (addressBounds != null)
                {
                    if (mSearchBoundsListener != null)
                    {
                        mSearchBoundsListener.onSuccess(addressBounds);
                    }
                    return;
                }*/
            }
            else {
                searchByName(argSearchText);
               /* if (mStops.size() == 0)
                {
                    search(argSearchText);
                    return;
                }*/
            }
        }

        for (Stop stop : mStops)
        {
            for (PhysicalStop physicalStop : stop.getPhysicalStops())
            {
                boundsBuilder.include(LocationTools.locToLatLng(physicalStop.getLocation()));
            }
        }

        if (mStops.isEmpty())
        {
            /*if (mSearchBoundsListener != null)
            {
                mSearchBoundsListener.onNotFound();
            }*/
            return null;
        }

        /*if (mSearchBoundsListener != null)
        {
            mSearchBoundsListener.onSuccess(boundsBuilder.build());
        }*/

        return boundsBuilder.build();
    }

    private LatLngBounds searchByAddress(String argSearchText)
    {
        LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
        Location loc = null;

        Geocoder geoCoder = new Geocoder(mContext);
        try {
            List<Address> addresses = geoCoder.getFromLocationName(argSearchText, 5);
            if (addresses.size() > 0) {
                for (Address address : addresses) {
                    String thoroughfare = address.getThoroughfare();
                    if (thoroughfare != null) {
                        // Log.d("Address ", thoroughfare);
                        Log.d("Address", address.toString());
                        Log.d("FEATURE", address.getFeatureName());

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
                    if (loc == null) {
                        loc = new Location(LocationSettings.LOCATION_PROVIDER);
                    }

                    String address = "";
                    if (firstAddress.getMaxAddressLineIndex() != -1) {
                        address = firstAddress.getAddressLine(0);
                            /*if (firstAddress.getMaxAddressLineIndex() == 2) {
                                address = firstAddress.getAddressLine(0);
                            } else if (firstAddress.getMaxAddressLineIndex() == 3) {
                                address = firstAddress.getAddressLine(1) + ", " + firstAddress.getAddressLine(0);
                            }*/
                    }
                    Log.d("FOUND", firstAddress.toString());
                    Log.d("FOUND 2", firstAddress.getAddressLine(0));
                    Log.d("ADDRESS", address);

                    if (address.isEmpty()) {
                        address = firstAddress.getThoroughfare() + " " + firstAddress.getSubThoroughfare();
                    }
                    loc.setLatitude(firstAddress.getLatitude());
                    loc.setLongitude(firstAddress.getLongitude());
                    boundsBuilder.include(LocationTools.locToLatLng(loc));
                    if (mAddressMarker == null)
                    {
                        mAddressMarker = getMap().addMarker(new MarkerOptions().title(address)
                                .position(LocationTools.locToLatLng(loc))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    }
                    else
                    {
                        mAddressMarker.hideInfoWindow();
                        mAddressMarker.setPosition(LocationTools.locToLatLng(loc));
                        mAddressMarker.setTitle(address);
                    }

                   return getProximityBoundsFromPosition(loc);

                   /* List<Stop> stops = StopTools.getStopsByDistance(loc, SortTools.FilterDistanceType.Close,4,mStops);
                    for (Stop stop : stops)
                    {
                        for (PhysicalStop physicalStop : stop.getPhysicalStops())
                        {
                            boundsBuilder.include(LocationTools.locToLatLng(physicalStop.getLocation()));
                        }
                    }*/

                }
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public LatLngBounds getProximityBoundsFromPosition(Location argLoc) {
        LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
        List<PhysicalStop> physicalStops = new ArrayList<>();
        for (Stop stop : mOriginalsStops)
        {
            physicalStops.addAll(stop.getPhysicalStops());
        }

        SortTools.sortEntityWithNameAndLocationByDistance(physicalStops, SortTools.FilterDistanceType.Close,argLoc);

        List<PhysicalStop> finalPhysicalStops = new ArrayList<>();
        List<Long> stopIds = new ArrayList<>();
        for (PhysicalStop physicalStop : physicalStops)
        {
            if (!stopIds.contains(physicalStop.getStopId()))
            {
                finalPhysicalStops.add(physicalStop);
                stopIds.add(physicalStop.getStopId());
                boundsBuilder.include(LocationTools.locToLatLng(physicalStop.getLocation()));
                if (finalPhysicalStops.size() == 4)
                {
                    break;
                }
            }
        }

        return boundsBuilder.build();
    }

    private void searchByLine(String argSearchText)
    {
        boolean isTogether = false;
        boolean isAllTogether = false;
        argSearchText = argSearchText.replace("l:","");
        if (argSearchText.contains("-"))
        {
            argSearchText = argSearchText.replaceAll("-","");
            isTogether = true;
        }
        else if (argSearchText.contains("="))
        {
            argSearchText = argSearchText.replaceAll("=","");
            isAllTogether = true;
        }

        String[] searchList = argSearchText.split(",");

        for (int i = mStops.size() - 1; i >= 0; i--) {
            boolean canRemove = true;
            for (Line conn : mStops.get(i).getConnections()) {
                for (String search : searchList) {
                    if (conn.getName().toLowerCase().equalsIgnoreCase(search)) {
                        canRemove = false;
                        changeColorMarker(i, BitmapDescriptorFactory.HUE_BLUE, MarkerVisibility.Visible);
                        break;
                    }
                }
            }

            if (canRemove) {
                changeColorMarker(i, BitmapDescriptorFactory.HUE_RED,MarkerVisibility.Invisible);
                mStops.remove(i);
            }
        }

        for (int i = mStops.size() -1; i >= 0; i--)
        {
            int lineCounter = 0;
            List<String> linesAlreadyFound = new ArrayList<>();
            Stop stop = mStops.get(i);

            for (PhysicalStop physicalStop : stop.getPhysicalStops()) {
                int connCounter = 0;
                boolean moreThanOne = false;
                List<String> connAlreadyFound = new ArrayList<>();
                for (String search : searchList) {
                    for (Line conn : physicalStop.getConnections())
                    {
                        if (conn.getName().equalsIgnoreCase(search))
                        {
                            if (!connAlreadyFound.contains(conn.getName()))
                            {
                                connCounter++;
                                connAlreadyFound.add(conn.getName());
                            }

                            if (!linesAlreadyFound.contains(conn.getName()))
                            {
                                lineCounter++;

                                linesAlreadyFound.add(conn.getName());
                            }
                        }

                        if (connCounter > 1)
                        {
                            moreThanOne = true;
                        }
                    }
                    if (moreThanOne && !isAllTogether)
                    {
                        break;
                    }
                }
                if (!isTogether && !isAllTogether && moreThanOne)
                {
                    physicalStop.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }
                else if (isTogether && moreThanOne)
                {
                    physicalStop.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }
                else if (isAllTogether && connCounter == searchList.length)
                {
                    physicalStop.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }
                /*if ((moreThanOne && !isAllTogether) || (isAllTogether && counter == searchList.length)) {
                    changeColorMarker(i,BitmapDescriptorFactory.HUE_BLUE,MarkerVisibility.Visible);
                    physicalStop.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }
                else if (isTogether)
                {
                    physicalStop.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    physicalStop.getMarker().setVisible(false);
                }
                else if (isAllTogether && counter < searchList.length) {
                    physicalStop.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    physicalStop.getMarker().setVisible(false);
                    //changeColorMarker(i, BitmapDescriptorFactory.HUE_RED,MarkerVisibility.Invisible);
                    //mStops.remove(i);
                    //break;
                } */
            }

            if ((isAllTogether && lineCounter != searchList.length) || isTogether && lineCounter <= 1)
            {
                changeColorMarker(i,BitmapDescriptorFactory.HUE_RED, MarkerVisibility.Invisible);
                mStops.remove(i);
            }
        }
    }
    private void searchByName(String argSearchText)
    {
        for (int i = mStops.size() - 1; i >= 0; i--) {
            if (!mStops.get(i).getName().toLowerCase().contains(argSearchText)) {
                changeColorMarker(i, BitmapDescriptorFactory.HUE_RED);
                mStops.remove(i);
            }
            else
            {
                changeColorMarker(i, BitmapDescriptorFactory.HUE_BLUE);
            }
        }
    }

    public void changeColorMarker(int ArgPosition, float ArgColor) {
        changeColorMarker(ArgPosition, ArgColor, MarkerVisibility.NoChange);
    }

    public void changeColorMarker(int ArgPosition, float ArgColor, MarkerVisibility argMarkerVisibility) {
        if (mStops == null || ArgPosition >= mStops.size())
        {
            return;
        }

        Stop stop = mStops.get(ArgPosition);


        for (int i = 0; i < stop.getPhysicalStops().size(); i++) {
            Marker marker = stop.getPhysicalStops().get(i).getMarker();//markerAtPosition(stop.getPhysicalStops().get(i).getLocation());
            if (marker != null) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(ArgColor));
                switch (argMarkerVisibility)
                {
                    case Invisible :
                        marker.setVisible(false);
                        break;
                    case Visible:
                        marker.setVisible(true);
                        break;
                }

            }
        }
    }

    private Marker markerAtPosition(LatLng argPos)
    {
        Location loc = new Location(LocationSettings.LOCATION_PROVIDER);
        loc.setLatitude(argPos.latitude);
        loc.setLongitude(argPos.longitude);

        return markerAtPosition(loc);
    }

    private Marker markerAtPosition(Location ArgLoc) {
       /* if (mOriginalMarkers == null || mOriginalMarkers.size() == 0 || ArgLoc == null)
        {
            return null;
        }

        for (Marker marker : mOriginalMarkers)
        {
            if (marker.getPosition().longitude == ArgLoc.getLongitude() &&
                    marker.getPosition().latitude == ArgLoc.getLatitude())
            {
                return marker;
            }

        }*/

        for (Stop stop : mStops)
        {
            for (PhysicalStop physicalStop : stop.getPhysicalStops())
            {
                if (physicalStop.getMarker().getPosition().longitude == ArgLoc.getLongitude() &&
                        physicalStop.getMarker().getPosition().latitude == ArgLoc.getLatitude())
                {
                    return physicalStop.getMarker();
                }
            }
        }

        return null;
    }

   /* public List<Marker> getMarkers()
    {
        return this.mOriginalMarkers;
    }*/

    public ArrayList<Stop> getStopsToSave() {
        ArrayList<Stop> stops = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Stop stop : mOriginalsStops)
            {
                stops.add(stop);
            }
        }

        return stops;
    }

    private void updateDistanceOpenMarkers() {
        if (mCurrentActiveMarker != null)
        {
            mCurrentActiveMarker.showInfoWindow();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        if (marker == null)
        {
            return null;
        }

        mCurrentActiveMarker = marker;

        final Stop stop = stopAtLatLng(marker.getPosition());
        PhysicalStop physicalStopResult = physicalStopAtLatLng(marker.getPosition());
        if (stop == null || physicalStopResult == null)
        {
            return null;
        }

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_map, null, false);

        ViewHolder holder = new ViewHolder(v);

        holder.mNameTV.setText(stop.getName());



        holder.m_distinctLines = new ArrayList<>();

        for (Line conn : physicalStopResult.getConnections()) {
            holder.m_distinctLines.add(conn);
        }

        StopTools.sortConnections(holder.m_distinctLines);

        holder.mNameTV.setText(stop.getName());


        holder.mConnectionsLinLay.removeAllViews();


        for (Line line : holder.m_distinctLines)
        {
            View connectionsV = LayoutInflater.from(mContext).inflate(R.layout.item_line_destination_map, holder.mConnectionsLinLay, false);
            connectionsV.setTag(R.id.lineTV,line.getId());
            TextView lineTV = (TextView)connectionsV.findViewById(R.id.lineTV);
            TextView destinationTV = (TextView)connectionsV.findViewById(R.id.destinationTV);

            lineTV.setText(line.getName());
            destinationTV.setText(line.getArrivalStop().getName());

            Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
            if (Build.VERSION.SDK_INT >= 16) {
                lineTV.setBackground(circle);
            }
            else
            {
                //noinspection deprecation
                lineTV.setBackgroundDrawable(circle);
            }
            LineTools.configureTextView(lineTV, line);

           // relativeLayout.addView(connectionsV);
            holder.mConnectionsLinLay.addView(connectionsV);
        }


        if (mLastLoc == null)
        {
            holder.mDistanceTV.setVisibility(View.GONE);
            //distanceTV.setText("Coucou");
        }
        else
        {
            int distance = (int)mLastLoc.distanceTo(physicalStopResult.getLocation());

            holder.mDistanceTV.setText(mContext.getString(R.string.distance,distance));
            holder.mDistanceTV.setVisibility(View.VISIBLE);
        }

        return v;
    }

    public PhysicalStop physicalStopAtLatLng(LatLng argPosition) {
        Stop stop = stopAtLatLng(argPosition);
        if (stop == null)
        {
            return null;
        }

        if (stop.getConnections().size() == 0)
        {
            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(mContext));
            for (PhysicalStop physicalStop : stop.getPhysicalStops()) {
                List<Line> lines = lineDAO.getAllByPhysicalStop(physicalStop.getId());
                physicalStop.setConnections(lines);
            }
        }

        Marker marker = markerAtPosition(argPosition);
        if (marker == null)
        {
            return null;
        }

        PhysicalStop physicalStopResult = null;
        for (PhysicalStop physicalStop : stop.getPhysicalStops())
        {
            if (physicalStop.getLocation().getLatitude() == marker.getPosition().latitude &&
                    physicalStop.getLocation().getLongitude() == marker.getPosition().longitude)
            {
                physicalStopResult = physicalStop;
                break;
            }
        }

        return physicalStopResult;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    public Stop stopAtLatLng(LatLng ArgPos) {
        if (ArgPos == null)
        {
            return null;
        }

        if (getItemCount() == 0)
        {
            return null;
        }

        for (Stop stop : mOriginalsStops)
        {
            for (PhysicalStop physicalStop : stop.getPhysicalStops()) {
                if (physicalStop.getLocation().getLatitude() == ArgPos.latitude &&
                        physicalStop.getLocation().getLongitude() == ArgPos.longitude) {

                    return stop;
                }
            }
        }

        return null;
    }

   /* public void addMarker(Marker argMarker) {
        mOriginalMarkers.add(argMarker);
    }*/

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View m_baseView = null;
        public TextView mNameTV = null;
        public TextView mDistanceTV = null;
        public List<Line> m_distinctLines = null;
        public LinearLayout mConnectionsLinLay = null;
        public View mDividerV = null;


        public ViewHolder(View v)
        {
            super(v);
            m_baseView = v;
            mNameTV = (TextView)v.findViewById(R.id.nameTV);
            mDistanceTV = (TextView) v.findViewById(R.id.distanceTV);
            m_distinctLines = new ArrayList<>();
            mConnectionsLinLay = (LinearLayout)v.findViewById(R.id.connectionsLinLay);
            mDividerV = v.findViewById(R.id.dividerV);
            mDividerV.setVisibility(View.GONE);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null)
            {
                mItemClickListener.onItemClick(null, v, getAdapterPosition(), v.getId());
            }
        }
    }


    public GoogleMap getMap()
    {
        return mMap;
    }
    
    public void setMap(GoogleMap argMap)
    {
        mMap = argMap;
    }

    /*public void setMarkers(List<Marker> argMarkers)
    {
        this.mOriginalMarkers = argMarkers;
        search(mCurrentSearch);
    }*/

    public void setStops(List<Stop> argStops)
    {
        this.mOriginalsStops = argStops;
        search(mCurrentSearch);
    }

    public Stop getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mStops.size())
        {
            return new Stop();
        }

        return mStops.get(argPos);
    }

   /* public Marker getMarker(int argPos)
    {
        if (argPos < 0 || argPos >= mOriginalMarkers.size())
        {
            return null;
        }

        return mOriginalMarkers.get(argPos);
    }*/


    public int getItemCount() {
        if (mStops == null)
        {
            return 0;
        }

        return mStops.size();
    }


    public void onBindViewHolder(MapAdapter.ViewHolder holder, int position) {

    }


    public MapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stop, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }

}
