package com.mytpg.program.adapters.opendata;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.opendata.OSection;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<OSection> mOSections;
    private StopDAO mStopDAO = null;
    private List<Stop> mStopsAlreadyFound = new ArrayList<>();


    public ArrayList<OSection> getOSectionsToSave() {
        ArrayList<OSection> oSections = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (OSection oSection : mOSections)
            {
                oSections.add(oSection);
            }
        }

        return oSections;
    }

    public void setItems(List<OSection> argConnections) {
        mOSections = argConnections;
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position < 0 || position >= getItemCount())
        {
            return;
        }

        mOSections.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View mBaseView = null;
        public TextView mDurationTV = null;
        public TextView mFromTV = null;
        public TextView mToTV = null;
        public TextView mFromDateTV = null;
        public TextView mTransortTV = null;
        public TextView mToDateTV = null;
        public ImageView mChevronIV = null;
        public RelativeLayout mInfosRelLay = null;
        public TextView mFromPlatformTV = null;
        public TextView mToPlatformTV = null;


        public ViewHolder(View v)
        {
            super(v);
            mBaseView = v;

            mInfosRelLay = (RelativeLayout)v.findViewById(R.id.infosRelLay);
            mDurationTV = (TextView)v.findViewById(R.id.durationTV);
            mFromTV = (TextView)v.findViewById(R.id.fromTV);
            mToTV = (TextView)v.findViewById(R.id.toTV);
            mFromDateTV = (TextView)v.findViewById(R.id.fromDateTV);
            mTransortTV = (TextView)v.findViewById(R.id.transportTV);
            mToDateTV = (TextView)v.findViewById(R.id.toDateTV);
            mChevronIV = (ImageView)v.findViewById(R.id.chevronIV);
            mFromPlatformTV = (TextView)v.findViewById(R.id.fromPlatformTV);
            mToPlatformTV = (TextView)v.findViewById(R.id.toPlatformTV); 

            mInfosRelLay.setOnClickListener(this);
            mChevronIV.setOnClickListener(this);
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

    public SectionsAdapter(Context argContext, List<OSection> argOSections)
    {
        this.mStopDAO = new StopDAO(DatabaseHelper.getInstance(argContext));
        this.mContext = argContext;
        this.mOSections = argOSections;
    }

    public void setOSections(List<OSection> argOSections)
    {
        this.mOSections= argOSections;
    }

    public OSection getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mOSections.size())
        {
            return new OSection();
        }

        return mOSections.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mOSections == null)
        {
            return 0;
        }

        return mOSections.size();
    }

    private Stop getStopForCFFName(String argCFFName)
    {
        for (Stop cStop : mStopsAlreadyFound)
        {
            if (cStop.getCFF().equalsIgnoreCase(argCFFName))
            {
                return cStop;
            }
        }

        Stop stop = mStopDAO.findByCFFName(argCFFName,false);
        if (stop != null) {
            mStopsAlreadyFound.add(stop);
        }

        return stop;
    }

    @Override
    public void onBindViewHolder(SectionsAdapter.ViewHolder holder, int position) {
        OSection oSection = getItem(position);

        String from = oSection.getDeparture().getStation().getName();
        String to = oSection.getArrival().getStation().getName();

        if (from.isEmpty())
        {
            from = oSection.getDeparture().getLocation().getName();
        }
        if (to.isEmpty())
        {
            to = oSection.getArrival().getLocation().getName();
        }

        Stop fromStop = getStopForCFFName(from);
        if (fromStop != null)
        {
            from = fromStop.getName();
        }

        Stop toStop = getStopForCFFName(to);
        if (toStop != null)
        {
            to = toStop.getName();
        }

        //holder.mDurationTV.setText(oSection.formatDuration());
        holder.mFromTV.setText(from);
        holder.mToTV.setText(to);

        Calendar departureDate = oSection.getDeparture().getDepartureDate();
        Calendar arrivalDate = oSection.getArrival().getArrivalDate();

        holder.mFromDateTV.setText(DateTools.dateToString(departureDate, DateTools.FormatType.OnlyHourWithoutSeconds));
        holder.mToDateTV.setText(DateTools.dateToString(arrivalDate,DateTools.FormatType.OnlyHourWithoutSeconds));

        String transport;
        if (oSection.getJourney().getName().isEmpty())
        {
            long difference = oSection.getDeparture().getDepartureDate().getTimeInMillis() - oSection.getArrival().getArrivalDate().getTimeInMillis();
            if (difference < 0)
            {
                difference *= -1;
            }
            int distance = (int) oSection.getDeparture().getLocation().getLocation().distanceTo(oSection.getArrival().getLocation().getLocation());
            transport = mContext.getString(R.string.journey_walk, distance);
            holder.mDurationTV.setVisibility(View.VISIBLE);
            holder.mDurationTV.setText(mContext.getString(R.string.journey_duration, (int)difference/DateTools.MINUTE_IN_MILLISECONDS));
        }
        else
        {
            holder.mDurationTV.setVisibility(View.INVISIBLE);
            holder.mDurationTV.setText(DateTools.dateToString(departureDate, DateTools.FormatType.OnlyHourWithoutSeconds));
            Stop destinationStop = getStopForCFFName(oSection.getJourney().getTo());
            String destination = destinationStop == null ? oSection.getJourney().getTo() : destinationStop.getName();
            transport = mContext.getString(R.string.journey_transport, LineTools.fromCFFCategory(oSection.getJourney().getCategory()), oSection.getJourney().getNumber(), destination);
        }
        holder.mTransortTV.setText(transport);
        
        holder.mFromPlatformTV.setText(mContext.getString(R.string.platform_name,oSection.getDeparture().getPlatform()));
        holder.mToPlatformTV.setText(mContext.getString(R.string.platform_name,oSection.getArrival().getPlatform()));
        
        if (oSection.getDeparture().getPlatform().isEmpty())
        {
            holder.mFromPlatformTV.setVisibility(View.GONE);
        }
        else
        {
            holder.mFromPlatformTV.setVisibility(View.VISIBLE);
        }
        if (oSection.getArrival().getPlatform().isEmpty())
        {
            holder.mToPlatformTV.setVisibility(View.GONE);
        }
        else
        {
            holder.mToPlatformTV.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public SectionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
