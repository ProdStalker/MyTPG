package com.mytpg.program.dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mytpg.engines.entities.Disruption;
import com.mytpg.program.R;
import com.mytpg.program.adapters.DisruptionsAdapter;
import com.mytpg.program.dialogs.core.CustomDialogFragment;
import com.mytpg.program.fragments.DisruptionsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 16.11.14.
 */
public class DisruptionsDialogFragment extends CustomDialogFragment {
    public static final String ARG_DISRUPTIONS = "disruptions";
    private DisruptionsAdapter m_disruptionsAdapter = null;
    private RecyclerView m_disruptionsRV = null;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Disruption> m_disruptions = null;

    public static Bundle createBundle(ArrayList<Disruption> ArgDisruptions) {
        Bundle bundle = new Bundle();

        bundle.putParcelableArrayList(DisruptionsDialogFragment.ARG_DISRUPTIONS,ArgDisruptions);

        return bundle;
    }

    public static Bundle createBundle(List<Disruption> argDisruptions) {
        ArrayList<Disruption> disruptions = new ArrayList<>();

        for (Disruption disruption : argDisruptions) {
            disruptions.add(disruption);
        }

        return createBundle(disruptions);
    }

    protected void initializeComponents(View ArgRootView) {
        if (ArgRootView == null)
        {
            return;
        }

        m_disruptionsRV = (RecyclerView)ArgRootView.findViewById(R.id.mainRV);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        m_disruptionsRV.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());// new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        m_disruptionsRV.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        m_disruptionsAdapter = new DisruptionsAdapter(getActivity(),m_disruptions);
        m_disruptionsRV.setAdapter(m_disruptionsAdapter);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_disruptions = getArguments().getParcelableArrayList(ARG_DISRUPTIONS);
        if (m_disruptions == null)
        {
            m_disruptions = new ArrayList<>();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ArrayList<Disruption> disruptions = null;
        if (savedInstanceState != null)
        {
            disruptions = savedInstanceState.getParcelableArrayList(DisruptionsFragment.ARG_SAVED_DISRUPTIONS);
        }

        if (disruptions != null)
        {
            m_disruptionsAdapter.setDisruptions(disruptions);
            m_disruptionsRV.setAdapter(m_disruptionsAdapter);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (m_disruptionsAdapter == null)
        {
            return;
        }
        ArrayList<Disruption> disruptions = m_disruptionsAdapter.getDisruptionsToSave();
        if (disruptions == null || disruptions.size() == 0)
        {
            return;
        }
        outState.putParcelableArrayList(DisruptionsFragment.ARG_SAVED_DISRUPTIONS, disruptions);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_disruptions, container,
                false);
        getDialog().setTitle(getString(R.string.menu_disruptions));

        initializeComponents(rootView);

        return rootView;
    }



}
