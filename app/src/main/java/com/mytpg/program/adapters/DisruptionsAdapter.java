package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mytpg.engines.entities.Disruption;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class DisruptionsAdapter extends RecyclerView.Adapter<DisruptionsAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<Disruption> mDisruptions;
    private List<Disruption> mOriginalsDisruptions;
    private String mCurrentSearch = "";

    public void search(String argSearchText) {
        boolean isSearchByLine = argSearchText.toLowerCase().indexOf("l:") == 0;

        if (argSearchText.length() < mCurrentSearch.length() || mCurrentSearch.length() == 0 || argSearchText.length() == 0 ||
            isSearchByLine) {
            mDisruptions = new ArrayList<>(mOriginalsDisruptions);
            if (argSearchText.length() > 0) {
                notifyDataSetChanged();
            }
        }

        //mDisruptions = new ArrayList<>(mOriginalsDisruptions);

        mCurrentSearch = argSearchText;
        argSearchText = argSearchText.toLowerCase();

        if (argSearchText.length() > 0) {
            if (isSearchByLine)
            {
                searchByLine(argSearchText);
            }
            else {
                searchByName(argSearchText);
            }
        }
        else
        {
            notifyDataSetChanged();
        }


    }

    private void searchByLine(String argSearchText)
    {
        argSearchText = argSearchText.replace("l:","");
        for (int i = mDisruptions.size() - 1; i >= 0; i--) {
            if (!mDisruptions.get(i).getLine().getName().equalsIgnoreCase(argSearchText))
            {
                mDisruptions.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    private void searchByName(String argSearchText)
    {
        for (int i = mDisruptions.size() - 1; i >= 0; i--) {
            if (!mDisruptions.get(i).getName().toLowerCase().contains(argSearchText)) {
                mDisruptions.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public ArrayList<Disruption> getDisruptionsToSave() {
        ArrayList<Disruption> disruptions = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Disruption disruption : mOriginalsDisruptions)
            {
                disruptions.add(disruption);
            }
        }

        return disruptions;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View m_baseView = null;
        public TextView mLineTV;
        public TextView mPlaceTV;
        public TextView mNatureTV;
        public TextView mConsequenceTV;
        public TextView mStopNameTV;
        public TextView mDateTV;

        public ViewHolder(View v)
        {
            super(v);
            m_baseView = v;

            mLineTV = (TextView)v.findViewById(R.id.lineTV);
            mPlaceTV = (TextView)v.findViewById(R.id.placeTV);
            mNatureTV = (TextView)v.findViewById(R.id.natureTV);
            mConsequenceTV = (TextView)v.findViewById(R.id.consequenceTV);
            mStopNameTV = (TextView)v.findViewById(R.id.stopNameTV);
            mDateTV = (TextView)v.findViewById(R.id.dateTV);

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

    public DisruptionsAdapter(Context argContext, List<Disruption> argDisruptions)
    {
        this.mContext = argContext;
        this.mOriginalsDisruptions = argDisruptions;
        this.mDisruptions = new ArrayList<>(argDisruptions);
    }

    public void setDisruptions(List<Disruption> argDisruptions)
    {
        this.mOriginalsDisruptions = argDisruptions;
        search(mCurrentSearch);
    }

    public Disruption getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mDisruptions.size())
        {
            return new Disruption();
        }

        return mDisruptions.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mDisruptions == null)
        {
            return 0;
        }

        return mDisruptions.size();
    }

    @Override
    public void onBindViewHolder(DisruptionsAdapter.ViewHolder holder, int position) {
        Disruption disruption = getItem(position);

        String dateString;
        if (DateTools.dateAreDifferent(disruption.getDate(), Calendar.getInstance(), DateTools.ComparisonType.OnlyDay))
        {
            dateString = DateTools.dateToString(disruption.getDate());
        }
        else
        {
            dateString = DateTools.dateToString(disruption.getDate(),DateTools.FormatType.OnlyHourWithoutSeconds);
        }

        holder.mLineTV.setText(disruption.getLine().getName());
        holder.mDateTV.setText(dateString);
        holder.mConsequenceTV.setText(disruption.getConsequences());
        holder.mNatureTV.setText(disruption.getNature());
        holder.mStopNameTV.setText(disruption.getStop().getName());
        holder.mPlaceTV.setText(disruption.getPlace());


        Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
        if (Build.VERSION.SDK_INT >= 16) {
            holder.mLineTV.setBackground(circle);
        }
        else
        {
            //noinspection deprecation
            holder.mLineTV.setBackgroundDrawable(circle);
        }
        LineTools.configureTextView(holder.mLineTV, disruption.getLine());

    }

    @Override
    public DisruptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disruption, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
