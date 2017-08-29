package com.mytpg.program.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.mytpg.engines.entities.opendata.OConnection;
import com.mytpg.engines.entities.opendata.OSection;
import com.mytpg.program.R;
import com.mytpg.program.adapters.opendata.SectionsAdapter;
import com.mytpg.program.fragments.core.BaseFragment;

/**
 * Created by stalker-mac on 25.10.16.
 */

public class ConnectionFragment extends BaseFragment {
    public final static String ARG_CONNECTION = "connection";
    private final static String ARG_SAVED_CONNECTION = "savedConnection";

    private RecyclerView mRecyclerView = null;
    private RecyclerView.LayoutManager mLayoutManager = null;

    private OConnection mConnection = null;
    private SectionsAdapter mSectionsAdapter = null;

    public static Bundle createBundle(OConnection argConnection)
    {
        if (argConnection == null)
        {
            return null;
        }

        Bundle bundle = new Bundle();

        bundle.putParcelable(ARG_CONNECTION,argConnection);

        return bundle;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
        {
            mConnection = savedInstanceState.getParcelable(ARG_SAVED_CONNECTION);
        }

        if (mConnection == null)
        {
            loadData();
        }
        else
        {
            if (mSectionsAdapter == null)
            {
                mSectionsAdapter = new SectionsAdapter(getActivity(), mConnection.getSections());
                mSectionsAdapter.setOnItemClickListener(new SectionClickListener());
            }
            else {
                mSectionsAdapter.setOSections(mConnection.getSections());
            }
            mRecyclerView.setAdapter(mSectionsAdapter);

            //mOSectionsAdapter.setOnItemClickListener(new OSectionsAdapterClickListener());

        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

       if (mConnection != null)
       {
           outState.putParcelable(ARG_SAVED_CONNECTION, mConnection);
       }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        Bundle bundle = getArguments();
        if (bundle == null)
        {
            getMainActivity().onBackPressed();
            return null;
        }

        mConnection = bundle.getParcelable(ARG_CONNECTION);
        if (mConnection == null)
        {
            getMainActivity().onBackPressed();
            return null;
        }

        return rootView;
    }

    @Override
    public boolean needToBeFullViewport() {
        return false;
    }

    private void loadData()
    {
        if (mConnection == null)
        {
            return;
        }

        mSectionsAdapter = new SectionsAdapter(getActivity(),mConnection.getSections());
        mRecyclerView.setAdapter(mSectionsAdapter);
        mSectionsAdapter.setOnItemClickListener(new SectionClickListener());
    }

    @Override
    public void search(String argSearchText) {

    }

    private class SectionClickListener implements AdapterView.OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            OSection section = mSectionsAdapter.getItem(position);
            if (section == null)
            {
                return;
            }

            if (id == R.id.chevronIV || id == R.id.infosRelLay)
            {
                if (section.getJourney().getName().isEmpty())
                {
                    if (getMainActivity() != null) {
                        getMainActivity().openMapDirection(section.getDeparture().getLocation().getLocation(), section.getArrival().getLocation().getLocation());
                    }
                    return;
                }

                Bundle bundle = SectionDetails.createBundle(section);
                SectionDetails sd = new SectionDetails();
                sd.setArguments(bundle);
                changeFragment(sd,true);
                return;
            }

        }
    }
}
