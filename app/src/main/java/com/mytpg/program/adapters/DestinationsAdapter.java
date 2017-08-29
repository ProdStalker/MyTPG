package com.mytpg.program.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mytpg.engines.entities.Line;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class DestinationsAdapter extends RecyclerView.Adapter<DestinationsAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<Line> mLines;


    public ArrayList<Line> getLinesToSave() {
        ArrayList<Line> lines = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Line line : mLines)
            {
                lines.add(line);
            }
        }

        return lines;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mLineTV = null;
        public View m_baseView = null;
        public TextView mDestinationTV = null;


        public ViewHolder(View v)
        {
            super(v);
            mLineTV = (TextView)v.findViewById(R.id.lineTV);
            mDestinationTV = (TextView)v.findViewById(R.id.destinationTV);
            m_baseView = v;

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

    public DestinationsAdapter(Context argContext, List<Line> argLines)
    {
        this.mContext = argContext;
        this.mLines = argLines;
    }

    public void setLines(List<Line> argLines)
    {
        this.mLines= argLines;
    }

    public Line getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mLines.size())
        {
            return new Line();
        }

        return mLines.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mLines == null)
        {
            return 0;
        }

        return mLines.size();
    }

    @Override
    public void onBindViewHolder(DestinationsAdapter.ViewHolder holder, int position) {
        Line line = getItem(position);

        holder.mLineTV.setText(line.getName());
        holder.mDestinationTV.setText(line.getArrivalStop().getName());

        Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
        if (Build.VERSION.SDK_INT >= 16) {
            holder.mLineTV.setBackground(circle);
        }
        else
        {
            //noinspection deprecation
            holder.mLineTV.setBackgroundDrawable(circle);
        }
        LineTools.configureTextView(holder.mLineTV, line);

    }

    @Override
    public DestinationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_line_destination, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
