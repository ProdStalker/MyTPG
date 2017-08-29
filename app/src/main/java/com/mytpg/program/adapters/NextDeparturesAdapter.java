package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.settings.AlarmSettings;
import com.mytpg.engines.tools.ColorTools;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.core.CoreAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.tools.AlarmTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class NextDeparturesAdapter extends CoreAdapter<NextDeparturesAdapter.ViewHolder> {
    private final static int KEY_HOUR_VIEW_TYPE = R.id.timeTV;

    private AdapterView.OnItemClickListener mItemClickListener = null;
    private AdapterView.OnItemLongClickListener mItemLongClickListener = null;
    private List<Departure> mDepartures;
    private int mDepartureCode = -1;

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

    public int lastIndexWaiting() {
        int index = 0;

        for (int i = 0; i < mDepartures.size(); i++)
        {
            if (mDepartures.get(i).getConnectionWaitingTime() >= -1)
            {
                break;
            }
            index++;
        }

        return index;
    }

    public boolean removeAlarm(Departure argDeparture,int argPosition, View argView, int argMinutesBefore) {
        if (AlarmTools.removeAlarm(mContext, argDeparture,true,argView, argMinutesBefore))
        {
            mDepartures.get(argPosition).setAlarm(false);
            notifyItemChanged(argPosition);
            return true;
        }

        return false;
    }

    public boolean setAlarm(Departure argDeparture, int argPosition, View argView, int argMinutesBefore)
    {
        if (AlarmTools.setAlarm(mContext, argDeparture, argView, argMinutesBefore))
        {
            mDepartures.get(argPosition).setAlarm(true);
            notifyItemChanged(argPosition);
            return true;
        }

        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View mBaseView = null;
        public ImageView mAlarmIV = null;
        public TextView mDestinationTV = null;
        public TextView mInfosDepartureTV = null;
        public TextView mLineTV = null;
        public ImageView mNowIV = null;
        public TextView mTimeTV = null;
        public ImageView mWarningIV = null;
        public ImageView mHandicapIV = null;


        public ViewHolder(View v)
        {
            super(v);
            mBaseView = v;
            mAlarmIV = (ImageView) v.findViewById(R.id.alarmIV);
            mDestinationTV = (TextView) v.findViewById(R.id.destinationTV);
            mInfosDepartureTV = (TextView)v.findViewById(R.id.infosDepartureTV);
            mLineTV = (TextView) v.findViewById(R.id.lineTV);
            mNowIV = (ImageView) v.findViewById(R.id.nowIV);
            mTimeTV = (TextView) v.findViewById(R.id.timeTV);
            mWarningIV = (ImageView) v.findViewById(R.id.warningIV);
            mHandicapIV = (ImageView) v.findViewById(R.id.handicapIV);

            mWarningIV.setOnClickListener(this);
            mAlarmIV.setOnClickListener(this);
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

    public NextDeparturesAdapter(Context argContext, List<Departure> argDepartures, int argDepartureCode)
    {
        super(argContext);
        this.mContext = argContext;
        this.mDepartures = argDepartures;
        this.mDepartureCode = argDepartureCode;
    }

    public void setDepartures(List<Departure> argDepartures)
    {
        this.mDepartures = argDepartures;
        notifyDataSetChanged();
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
    public void onBindViewHolder(NextDeparturesAdapter.ViewHolder holder, final int position) {
        final Departure departure = mDepartures.get(position);

      /*  if (departure.getLine().getId() == -1) {
            new SearchRealLineAsyncTask().execute(position);

        }*/

        //holder.mWarningIV.setOnClickListener(holder);
        /*holder.mWarningIV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int i = 0;
                while (i < departure.getDisruptions().size())
                {
                    Toast.makeText(m_context, departure.getDisruptions().get(i).getConsequences(), Toast.LENGTH_LONG).show();
                    i++;
                }
            }
        });*/


        holder.mDestinationTV.setText(departure.getLine().getArrivalStop().getName());
        if (departure.getLine().getArrivalStop().getName().equalsIgnoreCase("Hôpital Trois-Chêne"))
        {
            holder.mDestinationTV.setText("Hôpital 3-Chêne");
        }
        holder.mLineTV.setText(departure.getLine().getName());
        LineTools.configureTextView(holder.mLineTV, departure.getLine());

       // int alarmVisibility = View.VISIBLE;
        Drawable icon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            icon = mContext.getDrawable(R.drawable.ic_alarm_add);
        }
        else
        {
            icon = mContext.getResources().getDrawable(R.drawable.ic_alarm_add);
        }

        if (departure.hasAlarm())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                icon = mContext.getDrawable(R.drawable.ic_alarm);
            }
            else
            {
                icon = mContext.getResources().getDrawable(R.drawable.ic_alarm);
            }
        }
        else if (!AlarmTools.canAddAlarm(departure, AlarmSettings.MINIMUM_MINUTES_ALARM)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                icon = mContext.getDrawable(R.drawable.ic_alarm_unable);
            }
            else
            {
                icon = mContext.getResources().getDrawable(R.drawable.ic_alarm_unable);
            }
        }
        holder.mAlarmIV.setImageDrawable(icon);
        int visibility = View.VISIBLE;
        if (departure.getDisruptions().isEmpty())
        {
            visibility = View.GONE;
        }
        holder.mWarningIV.setVisibility(visibility);

		/*boolean isApi = false;
		App app = (App)m_context.getApplicationContext();
		if (app.getAbsDAOFact().getType() == AbstractDAOFactory.FactoryType.API)
		{
			isApi = true;
		}*/

        String timeBeforeDeparture = String.valueOf(departure.getWaitingTime());
        if (!timeBeforeDeparture.equalsIgnoreCase(">1h") &&
                !timeBeforeDeparture.equalsIgnoreCase("no more"))
        {
            timeBeforeDeparture += "'";
        }

        RelativeLayout.LayoutParams handicapLP = (RelativeLayout.LayoutParams) holder.mHandicapIV.getLayoutParams();

        if (departure.getWaitingTime().equalsIgnoreCase("0"))
        {
            handicapLP.addRule(RelativeLayout.LEFT_OF,R.id.nowIV);
            handicapLP.setMargins(0, 0, 0, 0);
            holder.mNowIV.setVisibility(View.VISIBLE);
            holder.mTimeTV.setVisibility(View.INVISIBLE);
        }
        else
        {
            handicapLP.addRule(RelativeLayout.LEFT_OF,R.id.timeTV);
            handicapLP.setMargins(0, 0, 5, 0);
            holder.mNowIV.setVisibility(View.INVISIBLE);
            holder.mTimeTV.setVisibility(View.VISIBLE);
        }
        holder.mTimeTV.setText(timeBeforeDeparture);
        holder.mTimeTV.setTag(KEY_HOUR_VIEW_TYPE, DateTools.dateToString(departure.getDate(), DateTools.FormatType.OnlyHourWithoutSeconds));

        if (position == lastIndexWaiting() && !timeBeforeDeparture.equalsIgnoreCase("0'") && position != 0)
        {
            holder.mInfosDepartureTV.setText(mContext.getString(R.string.estimate_arrival,timeBeforeDeparture,DateTools.dateToString(departure.getDate(), DateTools.FormatType.OnlyHourWithoutSeconds)));
            holder.mInfosDepartureTV.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.mInfosDepartureTV.setVisibility(View.GONE);
        }

        if (departure.isPRM())
        {
            holder.mHandicapIV.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.mHandicapIV.setVisibility(View.GONE);
        }

        if (departure.getCode() == mDepartureCode)
        {
            int layBG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                layBG = mContext.getColor(R.color.colorAccent);
            }
            else
            {
                layBG = mContext.getResources().getColor(R.color.colorAccent);
            }


            holder.mBaseView.setBackgroundColor(ColorTools.adjustAlpha(layBG,0.2f));
        }
        else
        {
            holder.mBaseView.setBackgroundColor(Color.WHITE);
        }

        holder.mTimeTV.setOnClickListener(new View.OnClickListener() {

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

        if (position == 0)
        {
            App app = (App) mContext.getApplicationContext();
            if (app != null && app.isFirstNextDepartures())
            {
                Tutorial alarmTuto= new Tutorial(mContext.getString(R.string.alarm), mContext.getString(R.string.showcase_alarms), holder.mAlarmIV);
                mTutorialManager.addTutorial(alarmTuto);
                app.setFirstNextDepartures(false);
            }
        }
    }

    @Override
    public NextDeparturesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_next_departure, parent, false);

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

    private class SearchRealLineAsyncTask extends AsyncTask<Integer, Void,Integer>{


        @Override
        protected Integer doInBackground(Integer... params) {
            int position = params[0].intValue();
            Departure departure = mDepartures.get(position);

            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(mContext));

            Line line = lineDAO.findByLine(departure.getLine());
            if (line != null)
            {
                mDepartures.get(position).getLine().setId(line.getId());
                mDepartures.get(position).getLine().setColor(line.getColor());
            }

            return position;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            notifyItemChanged(integer);
        }
    }
}
