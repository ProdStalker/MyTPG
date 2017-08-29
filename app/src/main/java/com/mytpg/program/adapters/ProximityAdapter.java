package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mytpg.engines.data.dao.PhysicalStopDAO;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.engines.tools.SizeTools;
import com.mytpg.engines.tools.StopTools;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class ProximityAdapter extends RecyclerView.Adapter<ProximityAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<Stop> mStops;
    private Location mLoc;

    public ArrayList<Stop> getStopsToSave() {
        ArrayList<Stop> stops = new ArrayList<>();

        for (Stop stop : mStops)
        {
            stops.add(stop);
        }

        return stops;
    }

    public void setLoc(Location argLoc) {
        mLoc = argLoc;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mNameTV = null;
        public TextView mDistanceTV = null;
        public View m_baseView = null;
        public List<Line> m_distinctLines = null;
        public LinearLayout m_firstRowLinLay = null;
        public LinearLayout m_secondRowLinLay = null;


        public ViewHolder(View v)
        {
            super(v);
            mNameTV = (TextView)v.findViewById(R.id.nameTV);
            mDistanceTV = (TextView)v.findViewById(R.id.distanceTV);
            m_baseView = v;
            m_distinctLines = new ArrayList<Line>();
            m_firstRowLinLay = (LinearLayout)v.findViewById(R.id.firstRow);
            m_secondRowLinLay = (LinearLayout) v.findViewById(R.id.secondRow);

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

    public ProximityAdapter(Context argContext, Location argLoc, List<Stop> argStops)
    {
        this.mContext = argContext;
        this.mStops = argStops;
        this.mLoc = argLoc;
    }

    public void setStops(List<Stop> argStops)
    {
        this.mStops = argStops;
    }

    public Stop getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mStops.size())
        {
            return new Stop();
        }

        return mStops.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mStops == null)
        {
            return 0;
        }

        return mStops.size();
    }

    @Override
    public void onBindViewHolder(ProximityAdapter.ViewHolder holder, final int position) {
        final Stop stop = getItem(position);

        holder.mNameTV.setText(stop.getName());

        int distance = (int) mLoc.distanceTo(stop.getPhysicalStops().get(0).getLocation());
        holder.mDistanceTV.setText(String.format("%1d m", distance));

        if (stop.getPhysicalStops().size() == 0)
        {
            final Handler handler = new Handler();
            new Runnable() {

                @Override
                public void run() {
                    PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(DatabaseHelper.getInstance(mContext));
                    final List<PhysicalStop> physicalStops = physicalStopDAO.findByStopId(stop.getId());

                    stop.setPhysicalStops(physicalStops);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged(position);

                        }
                    });
                }
            }.run();
        }

        holder.m_distinctLines = new ArrayList<>();

        for (Line conn : stop.getConnections()) {
            boolean found = false;

            int i = 0;
            while (i < holder.m_distinctLines.size()) {
                if (conn.getName().equalsIgnoreCase(holder.m_distinctLines.get(i).getName())) {
                    found = true;
                    break;
                }
                i++;
            }

            if (!found) {
                holder.m_distinctLines.add(conn);
            }
        }

        StopTools.sortConnections(holder.m_distinctLines);



        holder.mNameTV.setText(stop.getName());


        holder.m_firstRowLinLay.removeAllViews();
        holder.m_secondRowLinLay.removeAllViews();
        final int MaxConnections = mContext.getResources().getInteger(R.integer.max_connections);
        int numberConnectionsVisible = holder.m_distinctLines.size();
        if (numberConnectionsVisible > MaxConnections)
        {
            numberConnectionsVisible = MaxConnections;
        }

        int numberByLine = MaxConnections / 2;
		/*if (Density < DisplayMetrics.DENSITY_HIGH)
		{
			numberByLine = 9;
		}*/
		/*final DisplayMetrics Metrics = mContext.getResources().getDisplayMetrics();
		final int Density = Metrics.densityDpi;*/
        final int Margin = SizeTools.dpToPx(mContext, 3);
        final int Size = SizeTools.dpToPx(mContext, mContext.getResources().getInteger(R.integer.connection_size));

        if (numberConnectionsVisible <= numberByLine)
        {
            holder.m_secondRowLinLay.setVisibility(View.GONE);
        }
        else
        {
            holder.m_secondRowLinLay.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < numberConnectionsVisible; i++)
        {
            final boolean IsMore = i + 1 == MaxConnections;
            Line line = holder.m_distinctLines.get(i);
            LinearLayout.LayoutParams layPar = new LinearLayout.LayoutParams(Size,Size);
            layPar.setMargins(0, 0, Margin, 0);

            TextView tv = new TextView(mContext);
            //tv.setSingleLine(true);
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            tv.setLayoutParams(layPar);
            tv.setOnClickListener(holder);

            String text = line.getName();
            if (IsMore)
            {
                text = "...";
            }
            tv.setText(text);

            Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
            if (Build.VERSION.SDK_INT >= 16) {
                tv.setBackground(circle);
            }
            else
            {
                //noinspection deprecation
                tv.setBackgroundDrawable(circle);
            }
            LineTools.configureTextView(tv, line);


            if (i < numberByLine)
            {
                holder.m_firstRowLinLay.addView(tv);
            }
            else
            {
                holder.m_secondRowLinLay.addView(tv);
            }

          /*  tv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    TextView tmpTV = (TextView)v;
                    if (tmpTV != null)
                    {
                        //showConnections(tmpTV.getText().toString());
                    }
                }


            });*/
        }
    }

    @Override
    public ProximityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proximity_stop, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
