package com.mytpg.program.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.program.R;
import com.mytpg.program.adapters.DestinationsAdapter;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 19.11.14.
 */
public class LineDestinationsFragment extends BaseFragment {
    public static final String ARG_LINE = "line";
    private static final String ARG_SAVED_DESTINATIONS = "savedDestinations";
    private static final String ARG_SAVED_LINE = "savedLine";

    private RecyclerView mDestinationsRV = null;
    private LinearLayoutManager mLayoutManager;

    private DestinationsAdapter mDestinationsAdapter = null;
    private Line mLine = null;

    public static Bundle createBundle(long ArgLineId)
    {
        Bundle bundle = new Bundle();

        bundle.putLong(ARG_LINE, ArgLineId);

        return bundle;
    }

    public static LineDestinationsFragment newInstance()
    {
        LineDestinationsFragment ldf = new LineDestinationsFragment();
        return ldf;
    }
    /**
     *
     */
    public LineDestinationsFragment() {
        // TODO Auto-generated constructor stub
    }

    protected void initializeComponents(View ArgRootView) {
        mDestinationsRV = (RecyclerView)ArgRootView.findViewById(R.id.mainRV);
        mDestinationsRV.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mDestinationsRV.setLayoutManager(mLayoutManager);

        mDestinationsAdapter = new DestinationsAdapter(getActivity(),null);
        mDestinationsRV.setAdapter(mDestinationsAdapter);
    }

    @Override
    public void onActivityCreated(Bundle ArgBundle) {
        ArrayList<Line> destinations = null;
        if (ArgBundle != null)
        {
            destinations = ArgBundle.getParcelableArrayList(ARG_SAVED_DESTINATIONS);
            mLine = ArgBundle.getParcelable(ARG_LINE);
        }

        if (destinations == null || destinations.isEmpty())
        {
            loadData();
        }
        else
        {
            mDestinationsAdapter.setLines(destinations);
            mDestinationsRV.setAdapter(mDestinationsAdapter);

            mDestinationsAdapter.setOnItemClickListener(new DestinationsAdapterClickListener());
        }
        super.onActivityCreated(ArgBundle);
    }

    /* (non-Javadoc)
     * @see com.otpg.program.core.BaseFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_line_destinations, container, false);

        initializeComponents(rootView);

        Bundle bundle = getArguments();
        if (bundle != null)
        {
            final long LineId = bundle.getLong(LineDestinationsFragment.ARG_LINE);

            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
            mLine = lineDAO.find(LineId);
        }

        if (mLine == null)
        {
            getMainActivity().onBackPressed();
            return null;
        }



        return rootView;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mDestinationsAdapter == null)
        {
            return;
        }
        ArrayList<Line> destinations = mDestinationsAdapter.getLinesToSave();
        if (destinations == null || destinations.size() == 0)
        {
            return;
        }
        
        outState.putParcelable(ARG_SAVED_LINE,mLine);
        outState.putParcelableArrayList(ARG_SAVED_DESTINATIONS, destinations);
    }

    protected void loadData() {
        new DestinationsAsyncTask().execute();
    }

    private class DestinationsAsyncTask extends AsyncTask<Void, Void, List<Line>> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            showPD();
        }

        @Override
        protected List<Line> doInBackground(Void... params) {
            List<Line> lines;
            try
            {
                LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
                lines = lineDAO.getAllByName(mLine.getName());
            }
            catch (NullPointerException npe)
            {
                npe.printStackTrace();
                lines = null;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                lines = null;
            }

            return lines;

        }

        @Override
        protected void onPostExecute(List<Line> ArgLines)
        {
            try
            {
                if (isAdded())
                {

                    mDestinationsAdapter = new DestinationsAdapter(getActivity(), ArgLines);

                    mDestinationsRV.setAdapter(mDestinationsAdapter);

                    mDestinationsAdapter.setOnItemClickListener(new DestinationsAdapterClickListener());

                }


                dismissPD();

                if (isAdded())
                {
                    if (ArgLines == null || ArgLines.size() == 0)
                    {
                        Snackbar.make(getView(), getString(R.string.none_lines), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
            catch (NullPointerException npe)
            {
                npe.printStackTrace();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            dismissPD();
        }

    }

    @Override
    public void search(String ArgTextToSearch){

    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_line_destinations);
    }

    private class DestinationsAdapterClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (mDestinationsAdapter == null)
            {
                return;
            }

            Line line = mDestinationsAdapter.getItem(position);
            if (line == null)
            {
                return;
            }


            //bundle.putLong(LineStopsFragment.ARG_LINE, line.getId());

            Bundle bundle = LineStopsFragment.createBundle(line.getId());
            LineStopsFragment lsf = LineStopsFragment.newInstance();
            lsf.setArguments(bundle);

            changeFragment(lsf,true);
        }
    }
}

