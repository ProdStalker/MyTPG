package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.PhysicalStopDAO;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.engines.tools.SizeTools;
import com.mytpg.engines.tools.StopTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.core.CoreAdapter;
import com.mytpg.program.core.App;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class StopsAdapter extends CoreAdapter<StopsAdapter.ViewHolder> {
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private AdapterView.OnItemLongClickListener mItemLongClickListener = null;
    private List<Stop> mStops;
    private List<Stop> mOriginalsStops;
    private String mCurrentSearch = "";
    private boolean mCanShowTuto = false;
    private int mIndexWithMore = -1;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null)
        {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mTimer = new Timer();
        final Handler handler = new Handler();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mIndexWithMore != -1) {
                            mCanShowTuto = true;
                            notifyItemChanged(mIndexWithMore);
                            mTimer.cancel();
                        }


                    }
                });
            }
        };

        mTimer.scheduleAtFixedRate(mTimerTask, DateTools.SECOND_IN_MILLISECONDS * 2, DateTools.SECOND_IN_MILLISECONDS);
    }

    public void search(String argSearchText) {
        boolean isSearchByLine = argSearchText.toLowerCase().indexOf("l:") == 0;

        if (argSearchText.length() < mCurrentSearch.length() || mCurrentSearch.length() == 0 || argSearchText.length() == 0 ||
            isSearchByLine) {
            mStops = new ArrayList<>(mOriginalsStops);
            if (argSearchText.length() > 0) {
                notifyDataSetChanged();
            }
        }

        //mStops = new ArrayList<>(mOriginalsStops);

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
        String[] searchList = argSearchText.split(",");
        for (int i = mStops.size() - 1; i >= 0; i--) {
            boolean canRemove = true;
            for (Line conn : mStops.get(i).getConnections()) {
                for (String search : searchList) {
                    if (conn.getName().toLowerCase().equalsIgnoreCase(search)) {
                        canRemove = false;
                        break;
                    }
                }
            }

            if (canRemove) {
                mStops.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    private void searchByName(String argSearchText)
    {
        for (int i = mStops.size() - 1; i >= 0; i--) {
            if (!mStops.get(i).getName().toLowerCase().contains(argSearchText)) {
                mStops.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView mNameTV = null;
        public View m_baseView = null;
        public List<Line> m_distinctLines = null;
        public LinearLayout m_firstRowLinLay = null;
        public LinearLayout m_secondRowLinLay = null;
        public ImageView mFavoriteIV = null;


        public ViewHolder(View v)
        {
            super(v);
            mNameTV = (TextView)v.findViewById(R.id.nameTV);
            m_baseView = v;
            m_distinctLines = new ArrayList<>();
            m_firstRowLinLay = (LinearLayout)v.findViewById(R.id.firstRow);
            m_secondRowLinLay = (LinearLayout) v.findViewById(R.id.secondRow);
            mFavoriteIV = (ImageView)v.findViewById(R.id.favoriteIV);

            mFavoriteIV.setOnClickListener(this);
            mFavoriteIV.setOnLongClickListener(this);
            v.setOnClickListener(this);
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
                return mItemLongClickListener.onItemLongClick(null,v,getAdapterPosition(),v.getId());
            }

            return true;
        }
    }

    public StopsAdapter(Context argContext, List<Stop> argStops)
    {
        super(argContext);
        this.mContext = argContext;
        this.mOriginalsStops = argStops;
        this.mStops = new ArrayList<>(argStops);
    }

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

    @Override
    public int getItemCount() {
        if (mStops == null)
        {
            return 0;
        }

        return mStops.size();
    }

    @Override
    public void onBindViewHolder(StopsAdapter.ViewHolder holder, final int position) {
        final Stop stop = getItem(position);

        holder.mNameTV.setText(stop.getName());

        if (stop.getConnections().size() == 0)
        {
            final Handler handler = new Handler();
            new Runnable() {

                @Override
                public void run() {

                    if (stop.getPhysicalStops().size() == 0)
                    {
                        PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(DatabaseHelper.getInstance(mContext));
                        List<PhysicalStop> physicalStops = physicalStopDAO.findByStopId(stop.getId());
                        stop.setPhysicalStops(physicalStops);
                    }
                    else {
                        LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(mContext));
                        for (PhysicalStop physicalStop : stop.getPhysicalStops()) {
                            List<Line> lines = lineDAO.getAllByPhysicalStop(physicalStop.getId());
                            physicalStop.setConnections(lines);
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged(position);

                        }
                    });
                }
            }.run();
        }

        holder.m_distinctLines = new ArrayList<Line>();

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

                if (mIndexWithMore == -1)
                {
                    mIndexWithMore = position;
                }

                if (mCanShowTuto)
                {
                    App app = (App)mContext.getApplicationContext();
                    if (app != null)
                    {
                        if (app.isFirstMoreConnections())
                        {
                            Tutorial moreConnectionsTuto = new Tutorial(mContext.getString(R.string.showcase_more_connections), mContext.getString(R.string.showcase_click_to_see_more_connections),tv);
                            mTutorialManager.addTutorial(moreConnectionsTuto);
                            app.setFirstMoreConnections(false);
                        }
                        if (app.isFirstFavoriteStop()) {
                            Tutorial favoriteTuto = new Tutorial(mContext.getString(R.string.menu_favorites_stops), mContext.getString(R.string.showcase_click_to_favorite_this_stop), holder.mFavoriteIV);
                            mTutorialManager.addTutorial(favoriteTuto);
                            app.setFirstFavoriteStop(false);
                        }
                    }

                    mCanShowTuto = false;
                    showTuto();
                }
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



           /* tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null)
                    {
                        mItemClickListener.onItemClick(null, v, position, v.getId());
                    }
                }
            });*/

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

        holder.mFavoriteIV.setImageResource(R.drawable.ic_no_favorite);
        if (stop.isFavorite())
        {
            if (stop.isFavoriteDetailled())
            {
                holder.mFavoriteIV.setImageResource(R.drawable.ic_semi_favorite);
            }
            else {
                holder.mFavoriteIV.setImageResource(R.drawable.ic_favorite);
            }
        }
    }

    @Override
    public StopsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stop, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener argOnItemLongClickListener){
        mItemLongClickListener = argOnItemLongClickListener;
    }

}
