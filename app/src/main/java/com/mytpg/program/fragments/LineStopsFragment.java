package com.mytpg.program.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.mytpg.engines.data.dao.ConnectionDAO;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.factories.AbstractDAOFactory;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.StopsAdapter;
import com.mytpg.program.fragments.core.BaseFragment;
import com.mytpg.program.tools.WidgetTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 20.11.14.
 */
public class LineStopsFragment extends BaseFragment {
    public static final String ARG_LINE = "line";
    private static final String ARG_SAVED_LINE = "savedLine";
    private static final String ARG_SAVED_LINE_STOPS = "savedLineStops";

    private ProgressDialog m_pd = null;

    private TextView mDestinationTV = null;
    private LinearLayoutManager mLayoutManager;
    private TextView mLineTV = null;
    private RecyclerView mStopsRV = null;

    private StopsAdapter mStopsAdapter = null;
    private Line mLine = null;

    public static Bundle createBundle(long ArgLineId)
    {
        Bundle bundle = new Bundle();
        
        bundle.putLong(ARG_LINE,ArgLineId);
        
        return bundle;
    }
    
    public static LineStopsFragment newInstance(){
        LineStopsFragment lsf = new LineStopsFragment();
        
        return lsf;
    }
    
    /**
     *
     */
    public LineStopsFragment() {
        // TODO Auto-generated constructor stub
    }

    protected void initializeComponents(View ArgRootView) {
        if (ArgRootView == null)
        {
            return;
        }

        mDestinationTV = (TextView)ArgRootView.findViewById(R.id.destinationTV);
        mLineTV = (TextView)ArgRootView.findViewById(R.id.lineTV);
        mStopsRV = (RecyclerView)ArgRootView.findViewById(R.id.mainRV);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mStopsRV.setHasFixedSize(true);
        mStopsRV.setLayoutManager(mLayoutManager);


    }

    @Override
    public void onActivityCreated(Bundle ArgBundle) {
        ArrayList<Stop> lineStops = null;
        String textToSearch = "";
        if (ArgBundle != null)
        {
            lineStops = ArgBundle.getParcelableArrayList(ARG_SAVED_LINE_STOPS);
            mLine = ArgBundle.getParcelable(ARG_LINE);
        }

        if (lineStops == null || lineStops.isEmpty() || mLine == null)
        {
            loadData();
        }
        else
        {
            mStopsAdapter.setStops(lineStops);
            mStopsRV.setAdapter(mStopsAdapter);
            if (!textToSearch.isEmpty())
            {
                mStopsAdapter.search(textToSearch);
            }

            mStopsAdapter.setOnItemClickListener(new StopsAdapterClickListener());
            mStopsAdapter.setOnItemLongClickListener(new StopsAdapterClickListener());
            updateInfos();
        }
        super.onActivityCreated(ArgBundle);
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_line_stops);
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

    /* (non-Javadoc)
             * @see com.otpg.program.core.BaseFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
             */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_line_stops, container, false);

        initializeComponents(rootView);

        Bundle bundle = getArguments();
        if (bundle != null)
        {
            final long LineId = bundle.getLong(LineStopsFragment.ARG_LINE);

            getApp().setFactory(AbstractDAOFactory.FactoryType.DB);

            LineDAO lineDAO = (LineDAO) getApp().getAbsDAOFact().getAbsLineDAO();
            mLine = lineDAO.find(LineId);

            getApp().setFactory(AbstractDAOFactory.FactoryType.API);
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
    public void onPause()
    {
        super.onPause();
        if (m_pd != null)
        {
            m_pd.dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_SAVED_LINE,mLine);
        if (mStopsAdapter == null)
        {
            return;
        }
        ArrayList<Stop> lineStops = mStopsAdapter.getStopsToSave();
        if (lineStops == null || lineStops.size() == 0)
        {
            return;
        }

        outState.putParcelableArrayList(ARG_SAVED_LINE_STOPS, lineStops);
    }

    @Override
    public void search(String ArgTextToSearch) {
        if (mStopsAdapter != null) {
            mStopsAdapter.search(ArgTextToSearch);
        }
    }

    protected void loadData() {
        new StopsAsyncTask().execute();
    }

    private class StopsAsyncTask extends AsyncTask<Void, String, List<Stop>> {

        @Override
        protected void onPreExecute()
        {
            showPD();
        }

        @Override
        protected List<Stop> doInBackground(Void... params) {

            List<Stop> stops;
            try
            {

                if (!isAdded())
                {
                    return null;
                }


                getApp().setFactory(AbstractDAOFactory.FactoryType.DB);
                ConnectionDAO connDAO = new ConnectionDAO(DatabaseHelper.getInstance(getActivity()));
                List<Connection> connections = connDAO.getConnectionsByLine(mLine.getId());

                List<Long> stopIds = new ArrayList<>();
                for (Connection conn : connections)
                {
                    stopIds.add(conn.getPhysicalStop().getStopId());
                }

                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                stops = stopDAO.getByIds(stopIds,false);
            }
            catch (NullPointerException npe)
            {
                npe.printStackTrace();
                stops = null;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                stops = null;
            }

            return stops;

        }

        @Override
        protected void onPostExecute(List<Stop> ArgStops)
        {
            try
            {

                if (isAdded())
                {
                    mStopsAdapter = new StopsAdapter(getActivity(), ArgStops);

                    mStopsRV.setAdapter(mStopsAdapter);
                    mStopsAdapter.setOnItemClickListener(new StopsAdapterClickListener());
                    mStopsAdapter.setOnItemLongClickListener(new StopsAdapterClickListener());
                    updateInfos();

                }


                dismissPD();

                if (isAdded())
                {
                    if (ArgStops == null || ArgStops.size() == 0)
                    {
                        Toast.makeText(getActivity(), getString(R.string.none_stops), Toast.LENGTH_LONG).show();
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

    private void updateInfos() {
        mLineTV.setText(mLine.getName());
        LineTools.configureTextView(mLineTV, mLine);
        mDestinationTV.setText(mLine.getArrivalStop().getName());
    }

    private boolean configureFavoriteDetails(Stop argStop, int argPosition) {
        showConnections(argStop, argPosition);
        return true;
    }

    private void showConnections(final Stop argStop, final int argPosition)
    {
        ConnectionDAO connDAO = new ConnectionDAO(DatabaseHelper.getInstance(getContext()));
        final List<Connection> connections = connDAO.getConnectionsByStop(argStop.getId());
        CharSequence[] choices = new CharSequence[connections.size()];
        boolean[] choicesBool = new boolean[choices.length];
        int i = 0;
        while (i < choicesBool.length)
        {
            Line currentLine = connections.get(i).getLine();
            choices[i] = currentLine.getName() + " : " + currentLine.getArrivalStop().getName();
           /* boolean found = false;
            int k = 0;

            while (k < mConnections.size())
            {
                Line currentConn = mConnections.get(k);
                if (currentConn.getId() == currentLine.getId())
                {
                    found = true;
                    break;
                }
                k++;
            }*/

            choicesBool[i] = connections.get(i).isFavorite();
            i++;
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());

        adb.setTitle(getString(R.string.action_connections));
        adb.setMultiChoiceItems(choices, choicesBool, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                connections.get(which).setFavorite(isChecked);

            }
        } );

        adb.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                saveFavoriteDetails(argStop, argPosition, connections);
            }
        });

        adb.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        adb.show();
    }

    private void saveFavoriteDetails(Stop argStop, int argPosition, List<Connection> argConnections)
    {
        if (argStop == null || argStop.getId() < 1 || mStopsAdapter == null ||
                argPosition < 0 || argPosition >= mStopsAdapter.getItemCount() ||
                argConnections == null || argConnections.isEmpty())
        {
            return;
        }

        boolean oneFavorite = false;
        ConnectionDAO connDAO = new ConnectionDAO(DatabaseHelper.getInstance(getContext()));
        for (Connection conn : argConnections)
        {
            if (conn.isFavorite())
            {
                oneFavorite = true;
            }
            connDAO.update(conn);
        }

        argStop.setFavorite(oneFavorite);

        changeStopFavorite(argStop, argPosition, true);
    }

    private void changeStopFavorite(Stop argStop, int argPosition, boolean argIsDetailled) {
        boolean changeSuccess;
        argStop.setFavoriteDetailled(argIsDetailled);
        StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
        if (argStop.isFavorite())
        {
            changeSuccess = stopDAO.addFavorite(argStop, argIsDetailled);
        }
        else
        {
            changeSuccess = stopDAO.removeFavorite(argStop);
        }

        String textChange = getString(R.string.favorite_stop_added);
        if (changeSuccess)
        {
            if (!argStop.isFavorite())
            {
                textChange = getString(R.string.favorite_stop_removed);
            }
            mStopsAdapter.notifyItemChanged(argPosition);
            WidgetTools.updateWidget(getActivity(), WidgetTools.WidgetType.FavoriteStops);
        }
        else
        {
            if (argStop.isFavorite())
            {
                textChange = getString(R.string.favorite_stop_error_added);
            }
            else {
                textChange = getString(R.string.favorite_stop_error_removed);
            }
            argStop.setFavorite(!argStop.isFavorite());
        }

        Snackbar.make(getView(), textChange, Snackbar.LENGTH_SHORT).show();
    }

    private class StopsAdapterClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (mStopsAdapter == null)
            {
                return;
            }
            Stop stop = mStopsAdapter.getItem(position);
            if (stop == null)
            {
                return;
            }

            if (id == R.id.favoriteIV)
            {
                stop.setFavorite(!stop.isFavorite());
                changeStopFavorite(stop, position, false);

                return;
            }

            ArrayList<String> lineIds = new ArrayList<>();
            lineIds.add(String.valueOf(mLine.getId()));
            Bundle bundle = NextDeparturesFragment.createBundle(stop.getMnemo().getName(),
                    -1,
                    lineIds);

            NextDeparturesFragment ndf = NextDeparturesFragment.newInstance();
            ndf.setArguments(bundle);
            changeFragment(ndf,true);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
            if (mStopsAdapter == null) {
                return false;
            }

            Stop stop = mStopsAdapter.getItem(position);
            if (stop == null ||stop.getId() < 1) {
                return false;
            }

            return configureFavoriteDetails(stop, position);
        }
    }
}
