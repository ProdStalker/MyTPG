package com.mytpg.program.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mytpg.engines.data.api.opendata.ODirectionAPI;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.directions.Direction;
import com.mytpg.engines.entities.interfaces.IShareDialogListener;
import com.mytpg.engines.entities.opendata.OConnection;
import com.mytpg.engines.entities.opendata.ODirection;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.opendata.ConnectionsAdapter;
import com.mytpg.program.dialogs.ShareDialog;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stalker-mac on 24.10.16.
 */

public class ShowDirectionsFragment extends BaseFragment {
    private final static String ARG_SAVED_DIRECTION = "savedDirection";
    private final static String ARG_SAVED_ODIRECTION = "savedODirection";
    private final static String ARG_SAVED_PAGE_NUMBER = "savedPageNumber";
    public final static String ARG_DIRECTION = "direction";

    private Direction mDirection = null;
    private int mPageNumber = 0;
    private ConnectionsAdapter mConnectionsAdapter = null;
    private ODirection mODirection = null;

    private RecyclerView mRecyclerView = null;
    private RecyclerView.LayoutManager mLayoutManager = null;
    private Button mPreviousBtn = null;
    private Button mNextBtn = null;
    private TextView mFromTV = null;
    private TextView mToTV = null;
    private TextView mDateTV = null;
    private TextView mIsDepartureTV = null;
    private boolean mNamesAreAlreadySearched = false;

    public static Bundle createBundle(Direction argDirection)
    {
        if (argDirection == null)
        {
            return null;
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_DIRECTION, argDirection);

        return bundle;
    }

    public static Bundle createBundleFromUrl(Uri argUrl, Context argContext)
    {
        String from = argUrl.getQueryParameter("REQ0JourneyStopsS0G");
        String to = argUrl.getQueryParameter("REQ0JourneyStopsZ0G");
        String dateString  = argUrl.getQueryParameter("REQ0JourneyDate");
        String timeString  = argUrl.getQueryParameter("REQ0JourneyTime");
        String isDepartureString = argUrl.getQueryParameter("REQ0HafasSearchForw");

        Direction direction = new Direction();
        direction.setFrom(from);
        direction.setTo(to);

        String[] dateArray = dateString.split("\\.");
        if (dateArray.length != 3)
        {
            return null;
        }

        direction.getDate().set(Calendar.DAY_OF_MONTH, Integer.valueOf(dateArray[0]).intValue());
        direction.getDate().set(Calendar.MONTH, Integer.valueOf(dateArray[1]).intValue());
        direction.getDate().set(Calendar.YEAR, Integer.valueOf(dateArray[2]).intValue());

        String[] timeArray = timeString.split(":");
        if (timeArray.length != 2)
        {
            return null;
        }

        direction.getDate().set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeArray[0]).intValue());
        direction.getDate().set(Calendar.MINUTE, Integer.valueOf(timeArray[1]).intValue());

        direction.setDeparture(isDepartureString.equalsIgnoreCase("1"));

        return ShowDirectionsFragment.createBundle(direction);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_SAVED_DIRECTION, mDirection);
        outState.putParcelable(ARG_SAVED_ODIRECTION, mODirection);
        outState.putInt(ARG_SAVED_PAGE_NUMBER, mPageNumber);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getMainActivity() != null)
        {
            getMainActivity().updateCurrentNav(R.id.nav_show_directions);
        }

        if (savedInstanceState != null) {
            mDirection = savedInstanceState.getParcelable(ARG_SAVED_DIRECTION);
            mODirection = savedInstanceState.getParcelable(ARG_SAVED_ODIRECTION);
            mPageNumber = savedInstanceState.getInt(ARG_SAVED_PAGE_NUMBER,0);
        }

        if (mDirection == null || mODirection == null) {
            loadData();
        }
        else
        {
            updateInfos();
            if (mConnectionsAdapter == null)
            {
                mConnectionsAdapter = new ConnectionsAdapter(getActivity(), mODirection.getConnections(), mDirection);
                mConnectionsAdapter.setOnItemClickListener(new ConnectionClickListener());
            }
            else
            {
                mConnectionsAdapter.setDirection(mDirection);
                mConnectionsAdapter.setOConnections(mODirection.getConnections());
            }

            mRecyclerView.setAdapter(mConnectionsAdapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.share,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_share :
                share();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        ShareDialog sd = ShareDialog.newInstance(getActivity(),false,true,false);
        sd.setShareDialogListener(new IShareDialogListener() {
            @Override
            public void onImageClicked() {

                shareImage();
            }

            @Override
            public void onLinkClicked() {
                shareLink();
            }

            @Override
            public void onTextClicked() {
                shareText(false);
            }
        });
        sd.show();
    }

    private void shareLink()
    {
        if (mDirection == null || mODirection == null)
        {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("http://tpg.hafas.de/bin/tp/query.exe");
        sb.append("?queryPageDisplayed=yes&start=1");
        sb.append("&REQ0JourneyStopsS0G=");
        sb.append(mDirection.getFromStop().getId() != -1 ? mDirection.getFromStop().getName() : mODirection.getFrom().getName());
        sb.append("&REQ0JourneyStopsS0A=255");
        sb.append("&REQ0JourneyStopsZ0G=");
        sb.append(mDirection.getToStop().getId() != -1 ? mDirection.getToStop().getName() : mODirection.getTo().getName());
        sb.append("&REQ0JourneyStopsZ0A=7");
        sb.append("&REQ0JourneyDate=");
        sb.append(DateTools.dateToString(mDirection.getDate(), DateTools.FormatType.SearchTPGDirectionsDate));
        sb.append("&REQ0JourneyTime=");
        sb.append(DateTools.dateToString(mDirection.getDate(), DateTools.FormatType.SearchDirectionsHour));
        sb.append("&REQ0HafasSearchForw=");
        sb.append(mDirection.isDeparture() ? "1" : 0);
        sb.append("&REQ0JourneyProduct_prod_list=1:1111111111111111");

       /* String url = String.format("http://tpg.hafas.de/bin/tp/query.exe?start=1&REQ0JourneyStopsS0G=%1$s" +
                "&REQ0JourneyStopsZ0G=%2$s" +
                "&REQ0JourneyDate=%3$s" +
                "&REQ0JourneyTime=%4$s" +
                "&REQ0HafasSearchForw=%5$s", mODirection.getFrom().getName(), mODirection.getTo().getName(),
                DateTools.dateToString(mDirection.getDate(), DateTools.FormatType.SearchTPGDirectionsDate),
                DateTools.dateToString(mDirection.getDate(), DateTools.FormatType.SearchDirectionsHour),
                mDirection.isDeparture() ? "1" : "0").replaceAll(" ","%20");*/
        String url = sb.toString().replaceAll(" ", "%20");

        /*if (mDepartureCode != -1) {
            url = String.format(DataSettings.URL_MOBILE_TPG_DEPARTURE_WITH_CODE, mStop.getMnemo().getName().trim(), mDepartureCode);
        }
        else
        {
            url = String.format(DataSettings.URL_MOBILE_TPG_DEPARTURE, mStop.getMnemo().getName());
        }*/

        if (getMainActivity() != null)
        {
            getMainActivity().shareLink(getString(R.string.direction_names,mODirection.getFrom().getName(), mODirection.getTo().getName()),url);
        }
    }

    private void shareText(boolean argIsForMessage)
    {
       /* if (mNextDeparturesAdapter == null || mNextDeparturesAdapter.getItemCount() == 0)
        {
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(DateTools.dateToString(DateTools.getCurrentDate(), DateTools.FormatType.WithoutSeconds)+ "\n");

        for (Departure dep : mNextDeparturesAdapter.getDeparturesToSave())
        {
            sb.append(dep.toShareText(argIsForMessage)).append("\n");
        }

        if (getMainActivity() != null)
        {
            getMainActivity().shareText(getString(R.string.next_departures_stop,mStop.getName()), sb.toString());
        }*/
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_directions, container, false);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFromTV = (TextView)rootView.findViewById(R.id.fromTV);
        mToTV = (TextView)rootView.findViewById(R.id.toTV);
        mDateTV = (TextView)rootView.findViewById(R.id.dateTV);
        mIsDepartureTV = (TextView)rootView.findViewById(R.id.isDepartureTV);
        mPreviousBtn = (Button)rootView.findViewById(R.id.previousBtn);
        mPreviousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousDirections();
            }
        });
        mNextBtn = (Button)rootView.findViewById(R.id.nextBtn);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextDirections();
            }
        });

        Bundle bundle = getArguments();
        if (bundle == null)
        {
            getMainActivity().onBackPressed();
            return null;
        }

        mDirection = bundle.getParcelable(ARG_DIRECTION);
        if (mDirection == null)
        {
            getMainActivity().onBackPressed();
            return null;
        }


        return rootView;
    }

    private void previousDirections() {
        mPageNumber--;
        updateButtonsVisibility();
        loadData();
    }

    private void nextDirections()
    {
        mPageNumber++;
        updateButtonsVisibility();
        loadData();
    }

    private void updateButtonsVisibility() {
        if (mPageNumber < -9)
        {
            mPreviousBtn.setVisibility(View.INVISIBLE);
        }
        else
        {
            mPreviousBtn.setVisibility(View.VISIBLE);
        }

        if (mPageNumber > 9)
        {
            mNextBtn.setVisibility(View.INVISIBLE);
        }
        else
        {
            mNextBtn.setVisibility(View.VISIBLE);
        }
    }

    private void loadData()
    {
        if (mDirection == null)
        {
            if (getMainActivity() != null) {
                getMainActivity().onBackPressed();
            }
            return;
        }

        final Handler handler = new Handler();
        new Runnable(){
            @Override
            public void run()
            {
                StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                Stop fromStop = stopDAO.search(mDirection.getFrom(),false);
                Stop toStop = stopDAO.search(mDirection.getTo(),false);

                if (fromStop != null)
                {
                    mDirection.setFromStop(fromStop);
                }

                if (toStop != null)
                {
                    mDirection.setToStop(toStop);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadDirections();
                    }
                });
            }
        }.run();

    }

    private void loadDirections() {
        if (mDirection == null)
        {
            return;
        }

        final Handler handler = new Handler();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (mConnectionsAdapter.getItemCount() == 0)
                {
                    cancel();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mConnectionsAdapter.remove(0);
                    }
                });
            }
        };

        if (mConnectionsAdapter != null)
        {
            Timer timer = new Timer();
            timer.schedule(timerTask,0, 300);
        }

        ODirectionAPI oDirectionAPI = new ODirectionAPI(getActivity());
        oDirectionAPI.search(mDirection, mPageNumber, new IAPIListener<ODirection>() {
            @Override
            public void onError(VolleyError argVolleyError) {

            }

            @Override
            public void onSuccess(ODirection argObject) {
                mODirection = argObject;
                
                if (mConnectionsAdapter != null)
                {
                    timerTask.cancel();
                    mConnectionsAdapter.setItems(argObject.getConnections());
                }
                else
                {
                    mConnectionsAdapter = new ConnectionsAdapter(getActivity(), argObject.getConnections(), mDirection);
                }

                mRecyclerView.setAdapter(mConnectionsAdapter);
                mConnectionsAdapter.setOnItemClickListener(new ConnectionClickListener());
                updateInfos();
            }

            @Override
            public void onSuccess(List<ODirection> argObjects) {

            }
        });
    }

    private void updateInfos() {
        if (mODirection == null || !isAdded()) {
            return;
        }
        
        if (!mNamesAreAlreadySearched)
        {
            StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
            if (mDirection.getFromStop().getId() == -1)
            {
                Stop fromStop = stopDAO.findByName(mODirection.getFrom().getName());
                if (fromStop != null)
                {
                    mDirection.setFromStop(fromStop);
                }
            }
            if (mDirection.getToStop().getId() == -1)
            {
                Stop toStop = stopDAO.findByName(mODirection.getTo().getName());
                if (toStop != null)
                {
                    mDirection.setToStop(toStop);
                }
            }

            mNamesAreAlreadySearched = true;
        }

        mFromTV.setText((mDirection.getFromStop().getId() != -1) ? mDirection.getFromStop().getName() : mODirection.getFrom().getName());
        mToTV.setText((mDirection.getToStop().getId() != -1) ? mDirection.getToStop().getName() : mODirection.getTo().getName());
        mDateTV.setText(DateTools.dateToString(mDirection.getDate(), DateTools.FormatType.WithoutSeconds));
        mIsDepartureTV.setText(String.format("(%1$s)",mDirection.isDeparture() ? getString(R.string.departure) : getString(R.string.arrival)));
    }

    @Override
    public boolean needToBeFullViewport() {
        return false;
    }

    @Override
    public void search(String argSearchText) {

    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_directions);
    }

    private class ConnectionClickListener implements AdapterView.OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            OConnection connection = mConnectionsAdapter.getItem(position);
            if (connection == null)
            {
                return;
            }

            Bundle bundle = ConnectionFragment.createBundle(connection);
            ConnectionFragment cf = new ConnectionFragment();
            cf.setArguments(bundle);
            changeFragment(cf,true);
        }
    }
}
