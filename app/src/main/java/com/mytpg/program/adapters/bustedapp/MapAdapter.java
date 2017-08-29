package com.mytpg.program.adapters.bustedapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.interfaces.listeners.ISearchListener;
import com.mytpg.engines.entities.bustedapp.Alert;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.settings.LocationSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.engines.tools.LocationTools;
import com.mytpg.engines.tools.SortTools;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class MapAdapter implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {
    private enum MarkerVisibility {NoChange,Invisible,Visible}

    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<Alert> mOriginalsAlerts;
    private List<Alert> mAlerts;
    private GoogleMap mMap;
    private Location mLastLoc = null;
    private Marker mCurrentActiveMarker = null;
   // private ISearchListener.BoundsListener mSearchBoundsListener = null;
   // private List<Marker> mOriginalMarkers;

    public MapAdapter(Context argContext, List<Alert> argAlerts, ISearchListener.BoundsListener argSearchBoundsListener)
    {
        this.mContext = argContext;
        this.mOriginalsAlerts = argAlerts;
        this.mAlerts = new ArrayList<>(argAlerts);
        //this.mSearchBoundsListener = argSearchBoundsListener;
        //this.mOriginalMarkers = new ArrayList<>();

    }
    public void setLastLoc(Location argLoc)
    {
        mLastLoc = argLoc;
        updateDistanceOpenMarkers();
    }

    public LatLngBounds getProximityBoundsFromPosition(Location argLoc) {
        LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

        SortTools.sortEntityWithNameAndLocationByDistance(mAlerts, SortTools.FilterDistanceType.Close,argLoc);

        int counter = 1;
        for (Alert alert : mAlerts)
        {
            boundsBuilder.include(LocationTools.locToLatLng(alert.getLocation()));
            counter++;
            if (counter > 5)
            {
                break;
            }
        }

        return boundsBuilder.build();
    }

    public void changeColorMarker(int ArgPosition, float ArgColor) {
        changeColorMarker(ArgPosition, ArgColor, MarkerVisibility.NoChange);
    }

    public void changeColorMarker(int ArgPosition, float ArgColor, MarkerVisibility argMarkerVisibility) {
        if (mAlerts == null || ArgPosition >= mAlerts.size())
        {
            return;
        }

        Marker marker = mAlerts.get(ArgPosition).getMarker();//markerAtPosition(alert.getPhysicalAlerts().get(i).getLocation());
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

        for (Alert alert : mAlerts)
        {
            if (alert.getMarker().getPosition().longitude == ArgLoc.getLongitude() &&
                alert.getMarker().getPosition().latitude == ArgLoc.getLatitude())
            {
                return alert.getMarker();
            }
        }

        return null;
    }

   /* public List<Marker> getMarkers()
    {
        return this.mOriginalMarkers;
    }*/

    public ArrayList<Alert> getAlertsToSave() {
        ArrayList<Alert> alerts = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Alert alert : mOriginalsAlerts)
            {
                alerts.add(alert);
            }
        }

        return alerts;
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

        final Alert alert = alertAtLatLng(marker.getPosition());
        if (alert == null)
        {
            return null;
        }

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_busted_map, null, false);

        ViewHolder holder = new ViewHolder(v);

        holder.mNameTV.setText(alert.getCategory().getName());
        holder.mLineTV.setText(alert.getLineName());
        Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
        if (Build.VERSION.SDK_INT >= 16) {
            holder.mLineTV.setBackground(circle);
        }
        else
        {
            //noinspection deprecation
            holder.mLineTV.setBackgroundDrawable(circle);
        }

        holder.mDateTV.setText(DateTools.dateToString(alert.getDate(), DateTools.FormatType.OnlyHourWithoutSeconds));
        holder.mStopNameTV.setText(alert.getStopName());
        holder.mDestinationTV.setText(alert.getDirection());

        Log.d("ALERT", alert.toString());

        if (alert.getLineName().isEmpty())
        {
            holder.mDestinationTV.setText(String.format("(%1s)", mContext.getString(R.string.at_the_stop)));
            holder.mArrowIV.setVisibility(View.GONE);
            holder.mLineTV.setVisibility(View.GONE);
        }
        else
        {
            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(mContext));
            LineTools.configureTextView(holder.mLineTV, lineDAO.findByName(alert.getLineName()));
            holder.mLineTV.setVisibility(View.VISIBLE);
            holder.mArrowIV.setVisibility(View.VISIBLE);
        }


        if (mLastLoc == null)
        {
            holder.mDistanceTV.setVisibility(View.GONE);
            //distanceTV.setText("Coucou");
        }
        else
        {
            int distance = (int)mLastLoc.distanceTo(alert.getLocation());

            holder.mDistanceTV.setText(mContext.getString(R.string.distance,distance));
            holder.mDistanceTV.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    public Alert alertAtLatLng(LatLng ArgPos) {
        if (ArgPos == null)
        {
            return null;
        }

        if (getItemCount() == 0)
        {
            return null;
        }

        for (Alert alert : mOriginalsAlerts)
        {
            if (alert.getLocation().getLatitude() == ArgPos.latitude &&
                alert.getLocation().getLongitude() == ArgPos.longitude)
            {

                return alert;
            }
        }

        return null;
    }

   /* public void addMarker(Marker argMarker) {
        mOriginalMarkers.add(argMarker);
    }*/

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View m_baseView = null;
        public TextView mDateTV = null;
        public TextView mNameTV = null;
        public TextView mDistanceTV = null;
        public TextView mLineTV = null;
        public TextView mStopNameTV = null;
        public ImageView mArrowIV = null;
        public TextView mDestinationTV = null;
        public View mDividerV = null;


        public ViewHolder(View v)
        {
            super(v);
            m_baseView = v;
            mDateTV = (TextView)v.findViewById(R.id.dateTV);
            mNameTV = (TextView)v.findViewById(R.id.nameTV);
            mDistanceTV = (TextView) v.findViewById(R.id.distanceTV);
            mLineTV = (TextView) v.findViewById(R.id.lineTV);
            mStopNameTV = (TextView)v.findViewById(R.id.stopNameTV);
            mArrowIV = (ImageView) v.findViewById(R.id.arrowIV);
            mDestinationTV = (TextView) v.findViewById(R.id.destinationTV);
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

    public void setAlerts(List<Alert> argAlerts)
    {
        this.mOriginalsAlerts = argAlerts;
        this.mAlerts = new ArrayList<>(argAlerts);
    }

    public Alert getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mAlerts.size())
        {
            return new Alert();
        }

        return mAlerts.get(argPos);
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
        if (mAlerts == null)
        {
            return 0;
        }

        return mAlerts.size();
    }


    public void onBindViewHolder(MapAdapter.ViewHolder holder, int position) {

    }


    public MapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_busted_map, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }

}
