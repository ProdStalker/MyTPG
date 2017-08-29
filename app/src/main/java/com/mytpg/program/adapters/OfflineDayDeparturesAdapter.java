package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class OfflineDayDeparturesAdapter extends RecyclerView.Adapter<OfflineDayDeparturesAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private AdapterView.OnItemLongClickListener mItemLongClickListener = null;
    private List<Departure> mDepartures;

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();


    public ArrayList<Departure> getDeparturesToSave() {
        ArrayList<Departure> departures = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Departure departure : mDepartures)
            {
                departures.add(departure);
            }
        }

        return departures;
    }

    public void toggleSelection(int pos) {
        if (mSelectedItems.get(pos, false)) {
            mSelectedItems.delete(pos);
        }
        else {
            mSelectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }

    public void remove(int argPosition) {
        if (argPosition < 0 || argPosition >= getItemCount() || getItemCount() == 0)
        {
            return;
        }

        mDepartures.remove(argPosition);
        notifyItemRemoved(argPosition);
    }

    public List<Departure> getDepartures() {
        return mDepartures;
    }

    public void clear() {
        int count = getItemCount();
        if (count == 0 || mDepartures == null)
        {
            return;
        }

        mDepartures.clear();
        notifyItemRangeRemoved(0,count-1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView mLineTV = null;
        public View mBaseView = null;
        public TextView mDestinationTV = null;
        public TextView mStopTV = null;


        public ViewHolder(View v)
        {
            super(v);
            mLineTV = (TextView)v.findViewById(R.id.lineTV);
            mDestinationTV = (TextView)v.findViewById(R.id.destinationTV);
            mStopTV = (TextView)v.findViewById(R.id.stopTV);
            mBaseView = v;

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

    public OfflineDayDeparturesAdapter(Context argContext, List<Departure> argDepartures)
    {
        this.mContext = argContext;
        this.mDepartures = argDepartures;
    }

    public void setDepartures(List<Departure> argDepartures)
    {
        this.mDepartures= argDepartures;
    }

    public Departure getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mDepartures.size())
        {
            return new Departure();
        }

        return mDepartures.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mDepartures == null)
        {
            return 0;
        }

        return mDepartures.size();
    }

    @Override
    public void onBindViewHolder(OfflineDayDeparturesAdapter.ViewHolder holder, int position) {
        Departure departure = getItem(position);

        holder.mLineTV.setText(departure.getLine().getName());
        holder.mDestinationTV.setText(departure.getLine().getArrivalStop().getName());
        holder.mStopTV.setText(departure.getStop().getName());

        Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
        if (Build.VERSION.SDK_INT >= 16) {
            holder.mLineTV.setBackground(circle);
        }
        else
        {
            //noinspection deprecation
            holder.mLineTV.setBackgroundDrawable(circle);
        }
        LineTools.configureTextView(holder.mLineTV, departure.getLine());
        holder.mBaseView.setActivated(mSelectedItems.get(position,false));

    }

    @Override
    public OfflineDayDeparturesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offline_day_departures, parent, false);

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
