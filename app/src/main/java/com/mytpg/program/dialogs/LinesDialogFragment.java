package com.mytpg.program.dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mytpg.engines.entities.Line;
import com.mytpg.engines.tools.SortTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.LinesAdapter;
import com.mytpg.program.dialogs.core.CustomDialogFragment;
import com.mytpg.program.fragments.LinesFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.11.14.
 */
public class LinesDialogFragment extends CustomDialogFragment {
    public static final String ARG_LINES = "lines";
    private LinesAdapter m_linesAdapter = null;
    private RecyclerView m_linesRV = null;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Line> m_lines = null;

    public static Bundle createBundle(ArrayList<Line> argLines, boolean argUniqueNames) {
        Bundle bundle = new Bundle();


        ArrayList<Line> lines = new ArrayList<>();
        if (argUniqueNames) {
            List<String> lineNames = new ArrayList<>();
            for (Line line : argLines) {
                if (!lineNames.contains(line.getName())) {
                    lines.add(line);
                    lineNames.add(line.getName());
                }
            }

        }
        else {
            lines = argLines;
        }

        SortTools.sortEntityWithName(lines, SortTools.FilterType.AZ);

        bundle.putParcelableArrayList(LinesDialogFragment.ARG_LINES,lines);

        return bundle;
    }


    public static Bundle createBundle(List<Line> argLines, boolean argUniqueNames)
    {
        ArrayList<Line> lines = new ArrayList<>();

        if (argLines != null)
        {
            for (Line line : argLines)
            {
                lines.add(line);
            }
        }

        return LinesDialogFragment.createBundle(lines,argUniqueNames);
    }

    protected void initializeComponents(View ArgRootView) {
        if (ArgRootView == null)
        {
            return;
        }

        m_linesRV = (RecyclerView)ArgRootView.findViewById(R.id.mainRV);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        m_linesRV.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(getActivity(),getResources().getInteger(R.integer.columns_lines_small));
        m_linesRV.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        m_linesAdapter = new LinesAdapter(getActivity(),m_lines);
        m_linesRV.setAdapter(m_linesAdapter);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_lines = getArguments().getParcelableArrayList(ARG_LINES);
        if (m_lines == null)
        {
            m_lines = new ArrayList<>();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ArrayList<Line> lines = null;
        if (savedInstanceState != null)
        {
            lines = savedInstanceState.getParcelableArrayList(LinesFragment.ARG_SAVED_LINES);
        }

        if (lines != null)
        {
            m_linesAdapter.setLines(lines);
            m_linesRV.setAdapter(m_linesAdapter);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (m_linesAdapter == null)
        {
            return;
        }
        ArrayList<Line> lines = m_linesAdapter.getLinesToSave();
        if (lines == null || lines.size() == 0)
        {
            return;
        }
        outState.putParcelableArrayList(LinesFragment.ARG_SAVED_LINES, lines);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lines, container,
                false);
        getDialog().setTitle(getString(R.string.menu_lines));

        initializeComponents(rootView);

        return rootView;
    }


}
