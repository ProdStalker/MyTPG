package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mytpg.engines.data.api.ThermometerAPI;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.CheckPoint;
import com.mytpg.engines.entities.DepartureAlarm;
import com.mytpg.engines.entities.Thermometer;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.core.CoreAdapter;
import com.mytpg.program.core.App;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class DepartureAlarmsAdapter extends CoreAdapter<DepartureAlarmsAdapter.ViewHolder> {
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<DepartureAlarm> mDepartureAlarms;
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
                Calendar now = DateTools.now();
                for (int i = getItemCount()-1; i>= 0; i--)
                {
                    if (mDepartureAlarms.get(i).getNewDate().before(now) && mDepartureAlarms.get(i).getDate().before(now))
                    {
                        mDepartureAlarms.remove(i);
                        notifyItemRemoved(i);
                    }
                    if (mDepartureAlarms.isEmpty())
                    {
                        mTimer.cancel();
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        notifyDataSetChanged();
                    }
                });
            }
        };
        long startAt = DateTools.MINUTE_IN_MILLISECONDS;
        if (getItemCount() > 0)
        {
            Calendar now = DateTools.now();
            startAt = getItem(0).getDate().getTimeInMillis() - now.getTimeInMillis() - (10 * DateTools.MINUTE_IN_MILLISECONDS);
            if (startAt < DateTools.MINUTE_IN_MILLISECONDS)
            {
                startAt = DateTools.MINUTE_IN_MILLISECONDS;
            }
        }
        Log.d("Start at", String.valueOf(startAt) + "(" + String.valueOf(startAt/1000) + ")");
        mTimer.scheduleAtFixedRate(mTimerTask,startAt,DateTools.MINUTE_IN_MILLISECONDS);
    }

    public ArrayList<DepartureAlarm> getDepartureAlarmsToSave() {
        ArrayList<DepartureAlarm> DepartureAlarms = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (DepartureAlarm DepartureAlarm : mDepartureAlarms)
            {
                DepartureAlarms.add(DepartureAlarm);
            }
        }

        return DepartureAlarms;
    }

    public List<DepartureAlarm> getDepartureAlarms() {
        return mDepartureAlarms;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View m_baseView = null;
        public TextView mLineTV = null;
        public TextView mStopTV = null;
        public TextView mDestinationTV = null;
        public TextView mConsequenceTV = null;
        public TextView mPreviewTV = null;
        public TextView mDepartureTV = null;
        public ImageView mArrowRightHourIV = null;
        public ImageView mViewIV = null;
        public ImageView mDeleteIV = null;
        public ImageView mWarningIV = null;

        public ViewHolder(View v)
        {
            super(v);
            m_baseView = v;

            mLineTV = (TextView)v.findViewById(R.id.lineTV);
            mStopTV = (TextView)v.findViewById(R.id.stopTV);
            mDestinationTV = (TextView)v.findViewById(R.id.destinationTV);
            mConsequenceTV = (TextView)v.findViewById(R.id.consequenceTV);
            mPreviewTV = (TextView)v.findViewById(R.id.previewTV);
            mDepartureTV = (TextView)v.findViewById(R.id.departureTV);
            mArrowRightHourIV = (ImageView)v.findViewById(R.id.arrowRightHourIV);
            mViewIV = (ImageView)v.findViewById(R.id.viewIV);
            mDeleteIV = (ImageView)v.findViewById(R.id.deleteIV);
            mWarningIV = (ImageView)v.findViewById(R.id.warningIV);

            mViewIV.setOnClickListener(this);
            mDeleteIV.setOnClickListener(this);
            mWarningIV.setOnClickListener(this);
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

    public DepartureAlarmsAdapter(Context argContext, List<DepartureAlarm> argDepartureAlarms)
    {
        super(argContext);
        this.mContext = argContext;
        this.mDepartureAlarms = argDepartureAlarms;
    }

    public void setDepartureAlarms(List<DepartureAlarm> argDepartureAlarms)
    {
        this.mDepartureAlarms = argDepartureAlarms;
    }

    public DepartureAlarm getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mDepartureAlarms.size())
        {
            return new DepartureAlarm();
        }

        return mDepartureAlarms.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mDepartureAlarms == null)
        {
            return 0;
        }

        return mDepartureAlarms.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final DepartureAlarmsAdapter.ViewHolder holder, int position) {
        final DepartureAlarm departureAlarm = getItem(position);
        departureAlarm.getDate().set(Calendar.SECOND, 0);
        Calendar now = DateTools.now();
        if (departureAlarm.getNewDate().getTimeInMillis() == 0)
        {
            departureAlarm.getNewDate().setTimeInMillis(departureAlarm.getDate().getTimeInMillis());
        }

        String datePreviewString = DateTools.dateToString(departureAlarm.getDate(),DateTools.FormatType.OnlyHourWithoutSeconds);

        final Calendar dateDeparture = DateTools.now();
        dateDeparture.setTimeInMillis(departureAlarm.getDate().getTimeInMillis());

        Calendar tempDate = DateTools.now();
        tempDate.set(Calendar.SECOND,0);
        tempDate.setTimeInMillis(departureAlarm.getDate().getTimeInMillis());
        tempDate.add(Calendar.MINUTE,-10);
        Calendar tempNewDate = DateTools.now();
        tempNewDate.set(Calendar.SECOND,0);
        tempNewDate.setTimeInMillis(departureAlarm.getDate().getTimeInMillis());
        tempNewDate.add(Calendar.MINUTE,-10);
        boolean canShowLiveHour = false;
        if ((tempDate.compareTo(now) <= 0 || tempNewDate.compareTo(now) <= 0) && (departureAlarm.getDate().after(now) || departureAlarm.getNewDate().after(now)))
        {
            canShowLiveHour = true;
        }

        /*if (canShowLiveHour)
        {
            int diffMinutes = 8;
            if (position == 2)
            {
                diffMinutes *= -1;
            }
            if (position == 0 || position == 2) {
                dateDeparture.add(Calendar.MINUTE, diffMinutes);
            }
        }*/
        String dateDepartureString = DateTools.dateToString(dateDeparture, DateTools.FormatType.OnlyHourWithoutSeconds);

        holder.mLineTV.setText(departureAlarm.getLine().getName());
        holder.mStopTV.setText(departureAlarm.getStop().getName());
        holder.mDestinationTV.setText(departureAlarm.getLine().getArrivalStop().getName());
        holder.mPreviewTV.setText(datePreviewString);


        holder.mConsequenceTV.setText("");
        holder.mDepartureTV.setText("");
        holder.mDepartureTV.setVisibility(View.GONE);
        holder.mArrowRightHourIV.setVisibility(View.GONE);
        holder.mWarningIV.setVisibility(View.GONE);

        String consequencesText = mContext.getString(R.string.info_update_departure_alarm,DateTools.dateToString(tempDate, DateTools.FormatType.OnlyHourWithoutSeconds));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.mConsequenceTV.setText(Html.fromHtml(consequencesText, Html.FROM_HTML_MODE_LEGACY));
        }
        else
        {
            holder.mConsequenceTV.setText(Html.fromHtml(consequencesText));
        }

        if (canShowLiveHour)// && System.currentTimeMillis() - holder.mLastUpdate >= DateTools.MINUTE_IN_MILLISECONDS)
        {
            ThermometerAPI thermometerAPI = new ThermometerAPI(mContext);
            thermometerAPI.getByCode(departureAlarm.getDepartureCode(), new IAPIListener<Thermometer>() {
                @Override
                public void onError(VolleyError argVolleyError) {

                }

                @Override
                public void onSuccess(Thermometer argThermometer) {
                    if (argThermometer == null) {
                        return;
                    }

                    CheckPoint checkPoint = argThermometer.getCheckPointByCode(departureAlarm.getDepartureCode());
                    CheckPoint busCheckPoint = argThermometer.getActualBusCheckPoint();

                    if (checkPoint == null)
                    {
                        return;
                    }


                    departureAlarm.setDisruptions(new ArrayList<>(argThermometer.getDisruptions()));
                    if (!argThermometer.getDisruptions().isEmpty()) {
                        holder.mWarningIV.setVisibility(View.VISIBLE);
                    }

                    String newConsequencesText;
                    checkPoint.getDate().set(Calendar.SECOND, 0);
                    if (DateTools.dateAreDifferent(checkPoint.getDate(), departureAlarm.getDate(), DateTools.ComparisonType.All)) {
                        holder.mDepartureTV.setVisibility(View.VISIBLE);
                        holder.mArrowRightHourIV.setVisibility(View.VISIBLE);
                        holder.mDepartureTV.setText(DateTools.dateToString(checkPoint.getDate(), DateTools.FormatType.OnlyHourWithoutSeconds));

                        departureAlarm.getNewDate().setTimeInMillis(checkPoint.getDate().getTimeInMillis());

                        newConsequencesText = (checkPoint.getDate().before(departureAlarm.getDate())) ? mContext.getString(R.string.bus_comes_before) : mContext.getString(R.string.bus_comes_after);
                    }
                    else
                    {
                        newConsequencesText = mContext.getString(R.string.bus_comes_correct);
                    }

                    if (argThermometer.getCheckPoints().indexOf(busCheckPoint) > argThermometer.getCheckPoints().indexOf(checkPoint))
                    {
                        newConsequencesText = mContext.getString(R.string.bus_aldready_passed);
                    }

                    if (busCheckPoint != null)
                    {
                        newConsequencesText += "<br>" + mContext.getString(R.string.bus_at_stop_name, busCheckPoint.getStop().getName());
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        holder.mConsequenceTV.setText(Html.fromHtml(newConsequencesText, Html.FROM_HTML_MODE_LEGACY));
                    }
                    else
                    {
                        holder.mConsequenceTV.setText(Html.fromHtml(newConsequencesText));
                    }
                }

                @Override
                public void onSuccess(List<Thermometer> argThermometers) {

                }
            });

        }

        holder.mDepartureTV.setText(dateDepartureString);


        Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
        if (Build.VERSION.SDK_INT >= 16) {
            holder.mLineTV.setBackground(circle);
        }
        else
        {
            //noinspection deprecation
            holder.mLineTV.setBackgroundDrawable(circle);
        }
        LineTools.configureTextView(holder.mLineTV, departureAlarm.getLine());



        if (position == 0)
        {
            App app = (App)mContext.getApplicationContext();
            if (app != null && app.isFirstAlarm())
            {
                Tutorial delayTuto = new Tutorial(mContext.getString(R.string.alarm_dep_info), mContext.getString(R.string.showcase_alarm_dep_info), holder.mPreviewTV);
                Tutorial viewTuto = new Tutorial(mContext.getString(R.string.action_view), mContext.getString(R.string.showcase_alarm_view), holder.mViewIV);

                mTutorialManager.addTutorial(delayTuto);
                mTutorialManager.addTutorial(viewTuto);

                showTuto();

                app.setFirstAlarm(false);
            }
        }
    }

    @Override
    public DepartureAlarmsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_departure_alarm, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
