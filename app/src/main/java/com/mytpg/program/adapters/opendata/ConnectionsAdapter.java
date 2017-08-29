package com.mytpg.program.adapters.opendata;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.directions.Direction;
import com.mytpg.engines.entities.opendata.OConnection;
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
public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<OConnection> mOConnections;
    private Direction mDirection = new Direction();
    private StopDAO mStopDAO = null;
    private List<Stop> mStopsAlreadyFound = new ArrayList<>();


    public ArrayList<OConnection> getOConnectionsToSave() {
        ArrayList<OConnection> oConnections = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (OConnection oConnection : mOConnections)
            {
                oConnections.add(oConnection);
            }
        }

        return oConnections;
    }

    public void setItems(List<OConnection> argConnections) {
        mOConnections = argConnections;
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position < 0 || position >= getItemCount())
        {
            return;
        }

        mOConnections.remove(position);
        notifyItemRemoved(position);
    }

    public void setDirection(Direction argDirection) {
        mDirection = argDirection;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View mBaseView = null;
        public TextView mDurationTV = null;
        public TextView mFromTV = null;
        public TextView mToTV = null;
        public TextView mTransfersTV = null;
        public TextView mFromDateTV = null;
        public TextView mFromTransportTV = null;
        public TextView mToDateTV = null;
        public TextView mToTransportTV = null;


        public ViewHolder(View v)
        {
            super(v);
            mBaseView = v;

            mDurationTV = (TextView)v.findViewById(R.id.durationTV);
            mFromTV = (TextView)v.findViewById(R.id.fromTV);
            mToTV = (TextView)v.findViewById(R.id.toTV);
            mTransfersTV = (TextView)v.findViewById(R.id.transfersTV);
            mFromDateTV = (TextView)v.findViewById(R.id.fromDateTV);
            mFromTransportTV = (TextView)v.findViewById(R.id.fromTransportTV);
            mToDateTV = (TextView)v.findViewById(R.id.toDateTV);
            mToTransportTV = (TextView)v.findViewById(R.id.toTransportTV);

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

    public ConnectionsAdapter(Context argContext, List<OConnection> argOConnections, Direction argDirection)
    {
        this.mStopDAO = new StopDAO(DatabaseHelper.getInstance(argContext));
        this.mContext = argContext;
        this.mOConnections = argOConnections;
        this.mDirection = argDirection;
    }

    public void setOConnections(List<OConnection> argOConnections)
    {
        this.mOConnections= argOConnections;
    }

    public OConnection getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mOConnections.size())
        {
            return new OConnection();
        }

        return mOConnections.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mOConnections == null)
        {
            return 0;
        }

        return mOConnections.size();
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
    public void onBindViewHolder(ConnectionsAdapter.ViewHolder holder, int position) {
        OConnection oConnection = getItem(position);

        String from = oConnection.getFrom().getStation().getName();
        String to = oConnection.getTo().getStation().getName();

        if (from.equalsIgnoreCase(mDirection.getFromStop().getCFF()))
        {
            from = mDirection.getFromStop().getName();
        }
        else
        {
            Stop fromStop = getStopForCFFName(from);
            if (fromStop != null)
            {
                from = fromStop.getName();
            }
        }
        if (to.equalsIgnoreCase(mDirection.getToStop().getCFF()))
        {
            to = mDirection.getToStop().getName();
        }
        else
        {
            Stop toStop = getStopForCFFName(to);
            if (toStop != null)
            {
                to = toStop.getName();
            }
        }

        holder.mDurationTV.setText(oConnection.formatDuration());
        holder.mFromTV.setText(from);
        holder.mToTV.setText(to);
        holder.mTransfersTV.setText(String.valueOf(oConnection.getTransfers()));

        Calendar now = DateTools.now();

        int departureIndex = 0;
        int arrivalIndex = oConnection.getSections().size()-1;
        if (oConnection.getSections().get(departureIndex).getJourney().getName().isEmpty())
        {
            departureIndex++;
        }
        if (oConnection.getSections().get(arrivalIndex).getJourney().getName().isEmpty())
        {
            arrivalIndex--;
        }

        Calendar departureDate = oConnection.getSections().get(departureIndex).getDeparture().getDepartureDate();
        Calendar arrivalDate = oConnection.getSections().get(arrivalIndex).getArrival().getArrivalDate();

        holder.mFromDateTV.setText(DateTools.dateAreDifferent(now,departureDate, DateTools.ComparisonType.OnlyDay) ? DateTools.dateToString(departureDate, DateTools.FormatType.WithoutSeconds) : DateTools.dateToString(departureDate, DateTools.FormatType.OnlyHourWithoutSeconds));
        holder.mToDateTV.setText(DateTools.dateAreDifferent(now,arrivalDate, DateTools.ComparisonType.OnlyDay) ? DateTools.dateToString(arrivalDate, DateTools.FormatType.WithoutSeconds) : DateTools.dateToString(arrivalDate, DateTools.FormatType.OnlyHourWithoutSeconds));

        OSection fromSection = oConnection.getSections().get(departureIndex);
        OSection toSection = oConnection.getSections().get(arrivalIndex);

        holder.mFromTransportTV.setText(String.format(", %1$s %2$s", LineTools.fromCFFCategory(fromSection.getJourney().getCategory()), fromSection.getJourney().getNumber()));
        holder.mToTransportTV.setText(String.format(", %1$s %2$s", LineTools.fromCFFCategory(toSection.getJourney().getCategory()), toSection.getJourney().getNumber()));
    }

    @Override
    public ConnectionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_connection, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
