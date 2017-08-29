package com.mytpg.program.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.mytpg.engines.data.api.DisruptionAPI;
import com.mytpg.engines.data.api.LineAPI;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Disruption;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.program.R;
import com.mytpg.program.adapters.DisruptionsAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class DisruptionsFragment extends BaseFragment {
    public static final String ARG_SAVED_DISRUPTIONS = "savedDisruptions";
    
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private DisruptionsAdapter mDisruptionsAdapter;

    private List<Disruption> mDisruptions;
    private List<Line> mLines;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mDisruptionsAdapter == null)
        {
            return;
        }

        ArrayList<Disruption> disruptions = mDisruptionsAdapter.getDisruptionsToSave();

        if (!disruptions.isEmpty()) {
            outState.putParcelableArrayList(ARG_SAVED_DISRUPTIONS, disruptions);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ArrayList<Disruption> disruptions = null;
        if (savedInstanceState != null)
        {
            disruptions = savedInstanceState.getParcelableArrayList(ARG_SAVED_DISRUPTIONS);
        }

        if (disruptions == null || disruptions.isEmpty())
        {
            loadData();
        }
        else
        {
            if (mDisruptionsAdapter == null)
            {
                mDisruptionsAdapter = new DisruptionsAdapter(getActivity(), disruptions);
            }
            else {
                mDisruptionsAdapter.setDisruptions(disruptions);
            }
            mRecyclerView.setAdapter(mDisruptionsAdapter);

            //mDisruptionsAdapter.setOnItemClickListener(new DisruptionsAdapterClickListener());

        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_disruptions);
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_disruptions, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        //loadData();

        return rootView;
    }

    @Override
    public boolean needToBeFullViewport() {
        return false;
    }

    private void loadData()
    {
        showPD();

        if (getApp() != null && getApp().isFirstLaunchOfDay(App.FirstLaunchType.Lines)) {
            loadLines();
        }
        else
        {
            loadLinesFromDB();
        }


    }

    private void loadDisruptions()
    {
        DisruptionAPI disruptionAPI = new DisruptionAPI(getActivity());
        disruptionAPI.getAll(new IAPIListener<Disruption>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissPD();
            }

            @Override
            public void onSuccess(Disruption argObject) {

            }

            @Override
            public void onSuccess(List<Disruption> argObjects) {
                mDisruptions = argObjects;
                for (Disruption disruption : mDisruptions)
                {
                    String name = disruption.getLine().getName();
                    if (name.substring(0,1).equalsIgnoreCase("T") && name.length() > 1)
                    {
                        //name = name.substring(1);
                    }
                    for (Line line : mLines)
                    {
                        if (line.getName().equalsIgnoreCase(name))
                        {
                            disruption.setLine(new Line(line));
                            break;
                        }
                    }
                }

                mDisruptionsAdapter = new DisruptionsAdapter(getActivity(), mDisruptions);
                mRecyclerView.setAdapter(mDisruptionsAdapter);
                dismissPD();
            }
        });

    }

    private void loadLines()
    {
        LineAPI lineAPI = new LineAPI(getActivity());
        lineAPI.getAll(new IAPIListener<Line>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissPD();
            }

            @Override
            public void onSuccess(Line argObject) {

            }

            @Override
            public void onSuccess(List<Line> argObjects) {
                mLines = argObjects;
                loadDisruptions();
            }
        });
    }

    private void loadLinesFromDB()
    {
       // final Handler handler = new Handler();
        new Runnable(){

            @Override
            public void run() {
                LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
                mLines = lineDAO.getAll();

                loadDisruptions();
            }
        }.run();

    }

    @Override
    public void search(String argSearchText) {
        if(mDisruptionsAdapter != null)
        {
            mDisruptionsAdapter.search(argSearchText);
        }
    }

}
