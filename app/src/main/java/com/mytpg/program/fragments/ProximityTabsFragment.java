package com.mytpg.program.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mytpg.program.R;
import com.mytpg.program.fragments.core.BaseFragment;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class ProximityTabsFragment extends BaseFragment {

    private FragmentTabHost mFragTabHost;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_proximity_tabs, container, false);

        mFragTabHost = (FragmentTabHost)rootView.findViewById(R.id.mainTabHost);
        mFragTabHost.setup(getActivity(),getActivity().getSupportFragmentManager(),android.R.id.tabcontent);

        mFragTabHost.addTab(mFragTabHost.newTabSpec("tab1").setIndicator("Proximity"),
                ProximityFragment.class,null);

       // loadData();

        return rootView;
    }


    @Override
    public void search(String argSearchText) {
    }

}
