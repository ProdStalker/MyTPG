package com.mytpg.program.adapters.core;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.mytpg.engines.entities.managers.TutorialManager;


/**
 * Created by stalker-mac on 08.11.16.
 */

abstract public class CoreAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>  {
    protected Context mContext;
    protected TutorialManager mTutorialManager = null;

    public CoreAdapter(Context argContext)
    {
        mContext = argContext;
        mTutorialManager = new TutorialManager(mContext);
    }

    public void showTuto()
    {
        if (mTutorialManager.getTutorials().isEmpty())
        {
            return;
        }

        mTutorialManager.launchTutorials();
    }

    /*@Override
    abstract public T onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    abstract public void onBindViewHolder(T holder, int position);

    @Override
    abstract public int getItemCount();*/




}
