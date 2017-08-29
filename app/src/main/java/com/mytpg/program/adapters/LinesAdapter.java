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

import com.mytpg.engines.entities.Line;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class LinesAdapter extends RecyclerView.Adapter<LinesAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<Line> mLines;
    private List<Line> mOriginalsLines;
    private String mCurrentSearch = "";


    public ArrayList<Line> getLinesToSave() {
        ArrayList<Line> lines = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Line line : mOriginalsLines)
            {
                lines.add(line);
            }
        }

        return lines;
    }
    
    public void search(String argSearchText) {

        if (argSearchText.length() < mCurrentSearch.length() || mCurrentSearch.length() == 0 || argSearchText.length() == 0) {
            mLines = new ArrayList<>(mOriginalsLines);
            if (argSearchText.length() > 0) {
                notifyDataSetChanged();
            }
        }

        //mLines = new ArrayList<>(mOriginalsLines);

        mCurrentSearch = argSearchText;
        argSearchText = argSearchText.toLowerCase();

        if (argSearchText.length() > 0) {

            searchByName(argSearchText);

        }
        else
        {
            notifyDataSetChanged();
        }


    }

    private void searchByName(String argSearchText)
    {
        for (int i = mLines.size() - 1; i >= 0; i--) {
            if (!mLines.get(i).getName().toLowerCase().contains(argSearchText)) {
                mLines.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mNameTV = null;
        public View m_baseView = null;


        public ViewHolder(View v)
        {
            super(v);
            mNameTV = (TextView)v.findViewById(R.id.nameTV);
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

    public LinesAdapter(Context argContext, List<Line> argLines)
    {
        this.mContext = argContext;
        this.mOriginalsLines = argLines;
        this.mLines = new ArrayList<>(argLines);
    }

    public void setLines(List<Line> argLines)
    {
        this.mOriginalsLines = argLines;
        search(mCurrentSearch);
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
    public void onBindViewHolder(LinesAdapter.ViewHolder holder, int position) {
        Line line = getItem(position);

        holder.mNameTV.setText(line.getName());

        Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
        if (Build.VERSION.SDK_INT >= 16) {
            holder.mNameTV.setBackground(circle);
        }
        else
        {
            //noinspection deprecation
            holder.mNameTV.setBackgroundDrawable(circle);
        }
        LineTools.configureTextView(holder.mNameTV, line);

    }

    @Override
    public LinesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_line, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
