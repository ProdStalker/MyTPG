package com.mytpg.program.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.mytpg.engines.data.api.LineAPI;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.tools.SizeTools;
import com.mytpg.engines.tools.SortTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.LinesAdapter;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class LinesFragment extends BaseFragment {
    public final static String ARG_SAVED_LINES = "savedLines";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinesAdapter mLinesAdapter;

    private List<Line> mLines;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mLinesAdapter == null)
        {
            return;
        }

        ArrayList<Line> lines = mLinesAdapter.getLinesToSave();

        if (!lines.isEmpty()) {
            outState.putParcelableArrayList(ARG_SAVED_LINES, lines);
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_lines);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ArrayList<Line> lines = null;
        if (savedInstanceState != null)
        {
            lines = savedInstanceState.getParcelableArrayList(ARG_SAVED_LINES);
        }

        if (lines == null || lines.isEmpty())
        {
            loadData();
        }
        else
        {
            if (mLinesAdapter == null)
            {
                mLinesAdapter = new LinesAdapter(getActivity(), lines);
                mLinesAdapter.setOnItemClickListener(new LineClickListener());
            }
            else {
                mLinesAdapter.setLines(lines);
            }
            mRecyclerView.setAdapter(mLinesAdapter);

            //mLinesAdapter.setOnItemClickListener(new LinesAdapterClickListener());

        }
        super.onActivityCreated(savedInstanceState);
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lines, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(getActivity(), SizeTools.calculateNoOfColumns(getActivity()));

        mRecyclerView.setLayoutManager(mLayoutManager);

        //loadData();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);
        if (getMainActivity() != null)
        {
            getMainActivity().initializeSearchView();
        }
    }

    private void loadData()
    {

       // loadLines();

        loadLinesFromDB();

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
                mLinesAdapter = new LinesAdapter(getActivity(),mLines);
                mLinesAdapter.setOnItemClickListener(new LineClickListener());
                mRecyclerView.setAdapter(mLinesAdapter);
                dismissPD();
            }
        });
    }

    private void loadLinesFromDB()
    {
        showPD();
        final Handler handler = new Handler();
        new Runnable(){
            @Override
            public void run() {
                LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
                mLines = lineDAO.getAll(true);

                SortTools.sortEntityWithName(mLines, SortTools.FilterType.AZ);

                handler.post(new Runnable() {
                   @Override
                   public void run() {
                       mLinesAdapter = new LinesAdapter(getActivity(),mLines);
                       mLinesAdapter.setOnItemClickListener(new LineClickListener());
                       mRecyclerView.setAdapter(mLinesAdapter);
                       dismissPD();
                   }
               });
            }
        }.run();
    }

    @Override
    public boolean needToBeFullViewport() {
        return true;
    }

    @Override
    public void search(String argSearchText) {
        if (mLinesAdapter != null) {
            mLinesAdapter.search(argSearchText);
        }
    }

    public void updateToolbarMenu()
    {
        super.updateToolbarMenu();

        if (getMainActivity() != null)
        {
            getMainActivity().changeToolbarMenu(R.id.search,true);
        }
    }

    private class LineClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Line line = mLinesAdapter.getItem(position);

            if (line == null)
            {
                return;
            }

            Bundle bundle = LineDestinationsFragment.createBundle(line.getId());
            LineDestinationsFragment ldf = LineDestinationsFragment.newInstance();
            ldf.setArguments(bundle);

            changeFragment(ldf,true);
        }
    }
}
