package com.mytpg.program.adapters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TimePicker;

import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.directions.Direction;
import com.mytpg.engines.entities.interfaces.ILocationClickAdapter;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.core.CoreAdapter;
import com.mytpg.program.core.App;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class DirectionsAdapter extends CoreAdapter<DirectionsAdapter.ViewHolder> {

    private AdapterView.OnItemClickListener mItemClickListener = null;
    private ILocationClickAdapter mLocClickAdapter = null;
    private List<Direction> mDirections;
    private List<Stop> mStops = null;
    private StopsAutoSuggestAdapter mStopsAutoSuggestAdapter = null;


    public DirectionsAdapter(Context argContext, List<Direction> argDirections, List<Stop> argStops)
    {
        super(argContext);

        this.mDirections = argDirections;
        this.mStops = argStops;
        try {
            this.mStopsAutoSuggestAdapter = new StopsAutoSuggestAdapter(mContext, R.layout.item_auto_suggest_stop, argStops);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void remove(int argPosition) {
        if (argPosition < 0 || argPosition >= getItemCount() || getItemCount() == 0)
        {
            return;
        }

        mDirections.remove(argPosition);
        notifyItemRemoved(argPosition);
    }

    public ArrayList<Direction> getDirectionsToSave() {
        ArrayList<Direction> directions = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Direction direction : mDirections)
            {
                directions.add(direction);
            }
        }

        return directions;
    }

    public void add(int argPosition, Direction argDirection) {
        if (mDirections == null)
        {
            mDirections = new ArrayList<>();
        }

        int finalPosition = argPosition;

        if (argPosition < 0)
        {
            finalPosition = 0;
        }

        if (argPosition >= getItemCount()) {
           finalPosition = getItemCount();
        }

        mDirections.add(finalPosition, argDirection);
        notifyItemInserted(finalPosition);
    }

    public StopsAutoSuggestAdapter getStopsAutoSuggestAdapter() {
        return mStopsAutoSuggestAdapter;
    }

    public void setItems(List<Direction> argDirections) {
        mDirections = argDirections;
        notifyDataSetChanged();
    }

    public void setStops(List<Stop> argStops) {
        mStops = argStops;
        mStopsAutoSuggestAdapter = new StopsAutoSuggestAdapter(mContext,R.layout.item_auto_suggest_stop,mStops);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public AutoCompleteTextView mFromACTV = null;
        public AutoCompleteTextView mToACTV = null;
        public EditText mDateTimeET = null;
        public RadioButton mDepartureRB = null;
        public RadioButton mArrivalRB = null;
        public ImageView mResearchIV = null;
        public ImageView mSaveIV = null;
        public View mBaseView = null;
        public ImageView mFromGPSIV = null;
        public ImageView mToGPSIV = null;
        public LinearLayout mMainLinLay = null;
        public ImageView mDeleteIV = null;
        public ImageView mRefreshIV = null;


        public ViewHolder(View v) {
            super(v);
            mBaseView = v;
            mRefreshIV = (ImageView) v.findViewById(R.id.refreshIV);
            mRefreshIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Direction direction = getItem(getAdapterPosition());
                    direction.setFrom(mFromACTV.getText().toString());
                    direction.setTo(mToACTV.getText().toString());
                    direction.setDate(DateTools.now());
                    updateDateChoose(getAdapterPosition());
                }
            });
            mMainLinLay = (LinearLayout) v.findViewById(R.id.mainLinLay);
            mFromACTV = (AutoCompleteTextView) v.findViewById(R.id.fromACTV);
            mToACTV = (AutoCompleteTextView) v.findViewById(R.id.toACTV);
            mFromGPSIV = (ImageView) v.findViewById(R.id.fromGPSIV);
            mFromGPSIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLocClickAdapter != null) {
                        mLocClickAdapter.onLocationAsked(getAdapterPosition(), true);
                    }
                }
            });
            mToGPSIV = (ImageView) v.findViewById(R.id.toGPSIV);
            mToGPSIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLocClickAdapter != null) {
                        mLocClickAdapter.onLocationAsked(getAdapterPosition(), false);
                    }
                }
            });
            mDepartureRB = (RadioButton) v.findViewById(R.id.depRB);
            mDepartureRB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getItem(getAdapterPosition()).setDeparture(true);
                }
            });
            mArrivalRB = (RadioButton) v.findViewById(R.id.arrRB);
            mArrivalRB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getItem(getAdapterPosition()).setDeparture(false);
                }
            });
            mDateTimeET = (EditText) v.findViewById(R.id.dateTimeET);
            mDateTimeET.setFocusable(false);
            mDateTimeET.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Direction direction = getItem(getAdapterPosition());
                    direction.setFrom(mFromACTV.getText().toString());
                    direction.setTo(mToACTV.getText().toString());
                    chooseDate(getAdapterPosition());
                }
            });
            mResearchIV = (ImageView) v.findViewById(R.id.researchIV);
            mResearchIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    research(ViewHolder.this, getAdapterPosition());
                }
            });
            mSaveIV = (ImageView) v.findViewById(R.id.saveIV);
            mDeleteIV = (ImageView) v.findViewById(R.id.deleteIV);

            v.setOnClickListener(this);
            mDeleteIV.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null)
            {
                mItemClickListener.onItemClick(null, v, getAdapterPosition(), v.getId());
            }
        }
    }

    private void research(DirectionsAdapter.ViewHolder holder, int argPosition) {
        getItem(argPosition).setFrom(holder.mFromACTV.getText().toString().trim());
        getItem(argPosition).setTo(holder.mToACTV.getText().toString().trim());
        if (mItemClickListener != null)
        {
            mItemClickListener.onItemClick(null, holder.mResearchIV, argPosition, holder.mResearchIV.getId());
        }
    }

    private void chooseDate(final int argPosition) {
        final Direction direction = getItem(argPosition);
        DatePickerDialog dpd = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                direction.getDate().set(Calendar.YEAR, year);
                direction.getDate().set(Calendar.MONTH, month);
                direction.getDate().set(Calendar.DAY_OF_MONTH, dayOfMonth);
                chooseTime(argPosition);
            }
        }, direction.getDate().get(Calendar.YEAR), direction.getDate().get(Calendar.MONTH), direction.getDate().get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void chooseTime(final int argPosition) {
        final Direction direction = getItem(argPosition);

        TimePickerDialog tpd = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                direction.getDate().set(Calendar.HOUR_OF_DAY,hourOfDay);
                direction.getDate().set(Calendar.MINUTE, minute);
                updateDateChoose(argPosition);
            }
        }, direction.getDate().get(Calendar.HOUR_OF_DAY),direction.getDate().get(Calendar.MINUTE), DateFormat.is24HourFormat(mContext));
        tpd.show();
    }

    private void updateDateChoose(int position) {
        notifyItemChanged(position);
    }

    public void setDirections(List<Direction> argDirections)
    {
        this.mDirections = argDirections;
    }

    public Direction getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mDirections.size())
        {
            return new Direction();
        }

        return mDirections.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mDirections == null)
        {
            return 0;
        }

        return mDirections.size();
    }

    @Override
    public void onBindViewHolder(final DirectionsAdapter.ViewHolder holder, final int position) {
        final Direction direction = getItem(position);

        holder.mFromACTV.setAdapter(mStopsAutoSuggestAdapter);
        holder.mToACTV.setAdapter(mStopsAutoSuggestAdapter);
        
        holder.mFromACTV.setText(direction.getFrom());
        holder.mToACTV.setText(direction.getTo());
        holder.mDateTimeET.setText(DateTools.dateToString(direction.getDate(), DateTools.FormatType.DirectionDate));
        if (direction.isDeparture())
        {
            holder.mDepartureRB.setChecked(true);
        }
        else
        {
            holder.mArrivalRB.setChecked(true);
        }

        if (direction.getId() != -1)
        {
            holder.mDeleteIV.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.mDeleteIV.setVisibility(View.GONE);
        }

        holder.mSaveIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direction.setFrom(holder.mFromACTV.getText().toString());
                direction.setTo(holder.mToACTV.getText().toString());

                if (mItemClickListener != null)
                {
                    mItemClickListener.onItemClick(null, v, position, v.getId());
                }
            }
        });

        if (position == 0) {
            App app = (App)mContext.getApplicationContext();
            if (app != null && app.isFirstDirection()) {
                Tutorial tutorial = new Tutorial(mContext.getString(R.string.menu_directions), mContext.getString(R.string.showcase_click_to_search_direction), holder.mResearchIV);
                Tutorial tutorial2 = new Tutorial(mContext.getString(R.string.menu_directions), mContext.getString(R.string.showcase_click_to_save_direction), holder.mSaveIV);

                mTutorialManager.addTutorial(tutorial);
                mTutorialManager.addTutorial(tutorial2);
                mTutorialManager.launchTutorials();

                app.setFirstDirection(false);
            }
        }
    }

    @Override
    public DirectionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_direction, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }

    public void setOnLocClickListener(ILocationClickAdapter argOnLocClickListener)
    {
        this.mLocClickAdapter = argOnLocClickListener;
    }
}
