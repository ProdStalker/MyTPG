package com.mytpg.program.adapters.bustedapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.entities.bustedapp.Alert;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private AdapterView.OnItemLongClickListener mItemLongClickListener = null;
    private List<Alert> mAlerts;


    public ArrayList<Alert> getAlertsToSave() {
        ArrayList<Alert> alerts = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Alert alert : mAlerts)
            {
                alerts.add(alert);
            }
        }

        return alerts;
    }


    public List<Alert> getAlerts() {
        return mAlerts;
    }

    public void clear() {
        int count = getItemCount();
        if (count == 0 || mAlerts == null)
        {
            return;
        }

        mAlerts.clear();
        notifyItemRangeRemoved(0,count-1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView mLineTV = null;
        public View mBaseView = null;
        public TextView mDestinationTV = null;
        public TextView mStopNameTV = null;
        public ImageView mArrowIV = null;


        public ViewHolder(View v)
        {
            super(v);
            mLineTV = (TextView)v.findViewById(R.id.lineTV);
            mDestinationTV = (TextView)v.findViewById(R.id.destinationTV);
            mStopNameTV = (TextView)v.findViewById(R.id.stopNameTV);
            mBaseView = v;
            mArrowIV = (ImageView)v.findViewById(R.id.arrowIV);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null)
            {
                mItemClickListener.onItemClick(null, v, getAdapterPosition(), v.getId());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null)
            {
                mItemLongClickListener.onItemLongClick(null, v, getAdapterPosition(), v.getId());
            }
            return true;
        }
    }

    public AlertsAdapter(Context argContext, List<Alert> argAlerts)
    {
        this.mContext = argContext;
        this.mAlerts = argAlerts;
    }

    public void setAlerts(List<Alert> argAlerts)
    {
        this.mAlerts= argAlerts;
    }

    public Alert getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mAlerts.size())
        {
            return new Alert();
        }

        return mAlerts.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mAlerts == null)
        {
            return 0;
        }

        return mAlerts.size();
    }

    @Override
    public void onBindViewHolder(AlertsAdapter.ViewHolder holder, int position) {
        Alert alert = getItem(position);

        holder.mLineTV.setText(alert.getLineName());
        holder.mDestinationTV.setText(alert.getDirection());
        holder.mStopNameTV.setText(alert.getStopName());

        //holder.mDateTV.setText(DateTools.dateToString(alert.getDate(), DateTools.FormatType.OnlyHourWithoutSeconds));
       // holder.mStopNameTV.setText(alert.getStopName());
        holder.mDestinationTV.setText(alert.getDirection());

        Log.d("ALERT", alert.toString());

        if (alert.getLineName().isEmpty())
        {
            holder.mDestinationTV.setText(String.format("(%1s)", mContext.getString(R.string.at_the_stop)));
            holder.mArrowIV.setVisibility(View.GONE);
            holder.mLineTV.setVisibility(View.INVISIBLE);
        }
        else
        {
            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(mContext));
            LineTools.configureTextView(holder.mLineTV, lineDAO.findByName(alert.getLineName()));
            holder.mLineTV.setVisibility(View.VISIBLE);
            holder.mArrowIV.setVisibility(View.VISIBLE);
        }

        Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
        if (Build.VERSION.SDK_INT >= 16) {
            holder.mLineTV.setBackground(circle);
        }
        else
        {
            //noinspection deprecation
            holder.mLineTV.setBackgroundDrawable(circle);
        }

        LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(mContext));
        LineTools.configureTextView(holder.mLineTV, lineDAO.findByName(alert.getLineName()));

    }

    @Override
    public AlertsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener argOnItemLongClickListener)
    {
        this.mItemLongClickListener = argOnItemLongClickListener;
    }
}
