package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.HourDeparture;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.tools.SizeTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.core.CoreAdapter;
import com.mytpg.program.core.App;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class HourDeparturesAdapter extends CoreAdapter<HourDeparturesAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<HourDeparture> mHourDepartures;


    public ArrayList<HourDeparture> getHourDeparturesToSave() {
        ArrayList<HourDeparture> hourDepartures = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (HourDeparture hourDeparture : mHourDepartures)
            {
                hourDepartures.add(hourDeparture);
            }
        }

        return hourDepartures;
    }

    public List<HourDeparture> getHourDepartures() {
        return mHourDepartures;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mBaseView = null;
        TextView mHourTV = null;
        LinearLayout mFirstLinLay = null;
        LinearLayout mMainLinLay = null;
        LinearLayout mSecondLinLay = null;


        public ViewHolder(View v)
        {
            super(v);
            mBaseView = v;
            //m_departuresGV = (GridView) ArgView.findViewById(R.id.departuresGV);
            mHourTV = (TextView) v.findViewById(R.id.hourTV);
            mFirstLinLay = (LinearLayout) v.findViewById(R.id.firstLinLay);
            mMainLinLay = (LinearLayout) v.findViewById(R.id.mainLinLayV);
            mSecondLinLay = (LinearLayout) v.findViewById(R.id.secondLinLay);

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

    public HourDeparturesAdapter(Context argContext, List<HourDeparture> argHourDepartures)
    {
        super(argContext);
        this.mContext = argContext;
        this.mHourDepartures = argHourDepartures;
    }

    public void setHourDepartures(List<HourDeparture> argHourDepartures)
    {
        this.mHourDepartures = argHourDepartures;
    }

    public HourDeparture getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mHourDepartures.size())
        {
            return new HourDeparture();
        }

        return mHourDepartures.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mHourDepartures == null)
        {
            return 0;
        }

        return mHourDepartures.size();
    }

    @Override
    public void onBindViewHolder(HourDeparturesAdapter.ViewHolder holder, int position) {
        HourDeparture hd = mHourDepartures.get(position);

        holder.mHourTV.setText(hd.getHourOfDay());

        holder.mFirstLinLay.removeAllViews();
        holder.mSecondLinLay.removeAllViews();

        final DisplayMetrics Metrics = mContext.getResources().getDisplayMetrics();
        final int Density = Metrics.densityDpi;


        if (hd.getDepartures() != null)
        {
            int numberDp = 5;
            final int NumberDepartures = hd.getDepartures().size();
            if (NumberDepartures == 12)
            {
                //numberDp = 4;
            }
            else if (NumberDepartures >= 13)
            {
                //numberDp = 3;
            }

            int i = 1;
            for (final Departure dep : hd.getDepartures())
            {

                LinearLayout.LayoutParams layPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                layPar.setMargins(SizeTools.dpToPx(mContext, numberDp), 0, 0, 0);

                TextView tv = new TextView(mContext);
                // tv.setSingleLine(true);
                tv.setGravity(Gravity.CENTER);
                tv.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
                tv.setLayoutParams(layPar);


                final int Minutes = dep.getDate().get(Calendar.MINUTE);
                String minutesText = String.valueOf(Minutes);
                if (minutesText.length() == 1)
                {
                    minutesText = "0" + minutesText;
                }

                tv.setText(minutesText);


                if (Density >= DisplayMetrics.DENSITY_XXHIGH) {
                    holder.mFirstLinLay.addView(tv);
                }
                else
                {
                    int numberByLine = 11;

                    if (Density < DisplayMetrics.DENSITY_HIGH)
                    {
                        numberByLine = 9;
                    }

                    if (i <= numberByLine)
                    {
                        holder.mFirstLinLay.addView(tv);
                    }
                    else
                    {
                        holder.mSecondLinLay.addView(tv);
                    }
                }

                if (dep.hasAlarm())
                {
                    int color;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        color = mContext.getColor(R.color.colorPrimary);
                    }
                    else
                    {
                        color = mContext.getResources().getColor(R.color.colorPrimary);
                    }

                    tv.setBackgroundColor(color);

                    tv.setTextColor(Color.WHITE);
                }
                else
                {
                    tv.setBackgroundColor(Color.WHITE);
                    tv.setTextColor(Color.BLACK);
                }

                tv.setTag(i-1);
                final int FinalPosition = position;
                tv.setOnClickListener(holder);


                if (position == 0 && i == 1)
                {
                    App app = (App)mContext.getApplicationContext();
                    if (app != null && app.isFirstDayDepartures())
                    {
                        Tutorial stopNumberTuto = new Tutorial(mContext.getString(R.string.alarm), mContext.getString(R.string.showcase_click_to_alarm), tv);
                        mTutorialManager.addTutorial(stopNumberTuto);

                        app.setFirstDayDepartures(false);
                    }
                }
                i++;

            }
        }

    }

    @Override
    public HourDeparturesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_departures, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
