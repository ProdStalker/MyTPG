package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mytpg.engines.entities.CheckPoint;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.engines.tools.SizeTools;
import com.mytpg.engines.tools.StopTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.core.CoreAdapter;
import com.mytpg.program.core.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class CheckPointsAdapter extends CoreAdapter<CheckPointsAdapter.ViewHolder> {
    private final static int KEY_HOUR_VIEW_TYPE = R.id.timeTV;

    private AdapterView.OnItemClickListener mItemClickListener = null;
    private AdapterView.OnItemLongClickListener mItemLongClickListener = null;
    private List<CheckPoint> mCheckPoints;
    private boolean mIsHHMM = false;

    public ArrayList<CheckPoint> getCheckPointsToSave() {
        ArrayList<CheckPoint> checkPoints = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (CheckPoint checkPoint : mCheckPoints)
            {
                checkPoints.add(checkPoint);
            }
        }

        return checkPoints;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View mBaseView = null;
        public List<Line> mDistinctLines = null;
        public LinearLayout mFirstRowLinLay = null;
        public TextView mNextMinutesTV = null;
        public ImageView mNowIV = null;
        public LinearLayout mSecondRowLinLay = null;
        public TextView mStateTV = null;
        public TextView mStopTV = null;


        public ViewHolder(View v)
        {
            super(v);
            mBaseView = v;
            mFirstRowLinLay = (LinearLayout) v.findViewById(R.id.firstRow);
            mNextMinutesTV = (TextView) v.findViewById(R.id.nextMinutesTV);
            mNowIV = (ImageView) v.findViewById(R.id.nowIV);
            mSecondRowLinLay = (LinearLayout) v.findViewById(R.id.secondRow);
            mStateTV = (TextView) v.findViewById(R.id.stateTV);
            mStopTV = (TextView) v.findViewById(R.id.stopTV);

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
                return mItemLongClickListener.onItemLongClick(null,v,getAdapterPosition(),v.getId());
            }

            return true;
        }
    }

    public boolean isHHMM() {
        return mIsHHMM;
    }

    public void setHHMM(boolean ArgIsHHMM)
    {
        mIsHHMM = ArgIsHHMM;
        notifyDataSetChanged();
    }

    public CheckPointsAdapter(Context argContext, List<CheckPoint> argCheckPoints)
    {
        super(argContext);
        this.mContext = argContext;
        this.mCheckPoints = argCheckPoints;
    }

    public void setCheckPoints(List<CheckPoint> argCheckPoints)
    {
        this.mCheckPoints = argCheckPoints;
        notifyDataSetChanged();
    }

    public CheckPoint getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mCheckPoints.size())
        {
            return new CheckPoint();
        }

        return mCheckPoints.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mCheckPoints == null)
        {
            return 0;
        }

        return mCheckPoints.size();
    }

    @Override
    public void onBindViewHolder(CheckPointsAdapter.ViewHolder holder, final int position) {

        
        final CheckPoint chP = mCheckPoints.get(position);

        holder.mStopTV.setText(chP.getStop().getName());

        String stateText;
        int backgroundColor = Color.WHITE;//Color.parseColor("#FF189700");
        int strokeColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            strokeColor = mContext.getColor(R.color.colorPrimaryDark);
        }
        else {
            strokeColor = mContext.getResources().getColor(R.color.colorPrimaryDark);
        }
        int textColor = Color.BLACK;
        GradientDrawable gd = (GradientDrawable) holder.mStateTV.getBackground();

       /* final Drawable greenCircle = mContext.getResources().getDrawable(R.drawable.ic_circle_green);
        final Drawable crossDrawable = mContext.getResources().getDrawable(R.drawable.ic_cross);
        final Drawable tickDrawable = mContext.getResources().getDrawable(R.drawable.ic_tick);*/


        /*if (checkPoint.getStop().getPhysicalStops().size() == 0)
        {
            final Handler handler = new Handler();
            new Runnable() {

                @Override
                public void run() {
                    PhysicalStopDAO physicalStopDAO = new PhysicalStopDAO(DatabaseHelper.getInstance(mContext));
                    final List<PhysicalStop> physicalStops = physicalStopDAO.findByStopId(checkPoint.getStop().getId());

                    checkPoint.getStop().setPhysicalStops(physicalStops);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged(position);

                        }
                    });
                }
            }.run();
        }*/

        holder.mDistinctLines = new ArrayList<Line>();
        for (Line conn : chP.getStop().getConnections())
        {
            boolean found = false;

            int i = 0;
            while (i < holder.mDistinctLines.size())
            {
                if (conn.getName().equalsIgnoreCase(holder.mDistinctLines.get(i).getName()))
                {
                    found = true;
                    break;
                }
                i++;
            }

            if (!found)
            {
                holder.mDistinctLines.add(conn);
            }
        }

        StopTools.sortConnections(holder.mDistinctLines);

        int layoutBG = Color.WHITE;

        if (chP.getArrivalTime() > -1 && chP.isVisible())
        {
            int index = 0;

            int i = 0;
            while (i < mCheckPoints.size())
            {
                CheckPoint currentChP = mCheckPoints.get(i);
                if (currentChP != null)
                {
                    if (currentChP.getCode() == chP.getCode())
                    {
                        break;
                    }

                    if (currentChP.getArrivalTime() > -1 && currentChP.isVisible())
                    {
                        index++;
                    }
                }
                i++;
            }


            if (index == 0)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    backgroundColor = mContext.getColor(R.color.colorPrimaryDark);
                }
                else {
                    backgroundColor = mContext.getResources().getColor(R.color.colorPrimaryDark);
                }
                textColor = Color.WHITE;
                stateText = mContext.getString(R.string.tick);

            }
            else
            {
                stateText = String.valueOf(index);
                if (index == 1)
                {
                    App app = (App)mContext.getApplicationContext();
                    if (app != null && app.isFirstThermometer())
                    {
                        Tutorial stopNumberTuto = new Tutorial(mContext.getString(R.string.stop_number), mContext.getString(R.string.showcase_stop_number), holder.mStateTV);
                        mTutorialManager.addTutorial(stopNumberTuto);

                        app.setFirstThermometer(false);
                    }
                }
            }



        }
        else
        {
            backgroundColor = Color.RED;
            textColor = Color.BLACK;
            stateText = "X";
            layoutBG = Color.LTGRAY;

        }
        gd.setColor(backgroundColor);
        gd.setStroke(7,strokeColor);
        holder.mStateTV.setText(stateText);
        if (Build.VERSION.SDK_INT >= 16) {
            holder.mStateTV.setBackground(gd);
        }
        else
        {
            //noinspection deprecation
            holder.mStateTV.setBackgroundDrawable(gd);
        }

        holder.mStateTV.setTextColor(textColor);
        holder.mBaseView.setBackgroundColor(layoutBG);
        holder.mNextMinutesTV.setText("");

        String timeBeforeArrival = String.valueOf(chP.getArrivalTime()) + "'";
        if (chP.getArrivalTime() == 0)
        {
            timeBeforeArrival = "now";
        }


        if (chP.getArrivalTime() == 0)
        {
            int indexFirstNow = -1;
            int numberNow = 0;
            int i = 0;
            while (i < mCheckPoints.size())
            {
                CheckPoint currentChP = mCheckPoints.get(i);
                if (currentChP != null)
                {
                    if (currentChP.getArrivalTime() == 0)
                    {
                        if (indexFirstNow == -1)
                        {
                            indexFirstNow = i;
                        }
                        numberNow++;
                    }
                }
                i++;
            }

            if (numberNow > 1)
            {
                if (indexFirstNow == position)
                {
                    holder.mNextMinutesTV.setVisibility(View.INVISIBLE);
                    holder.mNowIV.setVisibility(View.VISIBLE);
                }
                else
                {
                    holder.mNextMinutesTV.setText("<1'");
                    holder.mNextMinutesTV.setVisibility(View.VISIBLE);
                    holder.mNowIV.setVisibility(View.INVISIBLE);
                }
            }
            else
            {
                holder.mNextMinutesTV.setVisibility(View.INVISIBLE);
                holder.mNowIV.setVisibility(View.VISIBLE);
            }
        }
        else if (chP.getArrivalTime() > 0)
        {
            if (chP.getReliabiltyType() == Departure.ReliabilityType.Theorical)
            {
                timeBeforeArrival = "~" + timeBeforeArrival;
            }
            holder.mNowIV.setVisibility(View.INVISIBLE);
            holder.mNextMinutesTV.setVisibility(View.VISIBLE);



            String nextMinutesText = timeBeforeArrival;
            Object tag = DateTools.dateToString(chP.getDate(),DateTools.FormatType.OnlyHourWithoutSeconds);

            if (isHHMM())
            {
                nextMinutesText = DateTools.dateToString(chP.getDate(),DateTools.FormatType.OnlyHourWithoutSeconds);
                tag = timeBeforeArrival;
            }

            holder.mNextMinutesTV.setText(nextMinutesText);
            holder.mNextMinutesTV.setTag(KEY_HOUR_VIEW_TYPE, tag);

            holder.mNextMinutesTV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    TextView tv = (TextView)v;
                    if (tv != null)
                    {
                        CharSequence text = (CharSequence) tv.getTag(KEY_HOUR_VIEW_TYPE);

                        CharSequence newText = tv.getText();

                        tv.setText(text);
                        tv.setTag(KEY_HOUR_VIEW_TYPE,newText);
                        //tv.setText(DateTools.dateToString(departure.getDate(),DateTools.FormatType.OnlyHour));
						/*}
						else
						{
							tv.setTag(R.id.timeTV,Boolean.valueOf(true));
							tv.setText(DateTools.timeBeforeDeparture(departure.getDate()));
						}*/
                    }
                }
            });
        }
        else
        {
            holder.mNowIV.setVisibility(View.INVISIBLE);
            holder.mNextMinutesTV.setVisibility(View.VISIBLE);
        }

        holder.mFirstRowLinLay.removeAllViews();
        holder.mSecondRowLinLay.removeAllViews();
        final int MaxConnections = mContext.getResources().getInteger(R.integer.max_connections_thermometer);
        int numberConnectionsVisible = holder.mDistinctLines.size();
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
        final int Size = SizeTools.dpToPx(mContext, mContext.getResources().getInteger(R.integer.connection_size_thermometer));

        if (numberConnectionsVisible <= numberByLine)
        {
            holder.mSecondRowLinLay.setVisibility(View.GONE);
        }
        else
        {
            holder.mSecondRowLinLay.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < numberConnectionsVisible; i++)
        {
            final boolean IsMore = i + 1 == MaxConnections;
            Line line = holder.mDistinctLines.get(i);
            LinearLayout.LayoutParams layPar = new LinearLayout.LayoutParams(Size,Size);
            layPar.setMargins(0, 0, Margin, 0);

            TextView tv = new TextView(mContext);
            // tv.setSingleLine(true);
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
            LineTools.configureTextView(tv,line);

            if (i < numberByLine)
            {
                holder.mFirstRowLinLay.addView(tv);
            }
            else
            {
                holder.mSecondRowLinLay.addView(tv);
            }


        }


    }

    @Override
    public CheckPointsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thermometer, parent, false);

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
