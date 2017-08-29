package com.mytpg.program.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.mytpg.engines.data.api.ThermometerAPI;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.CheckPoint;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Thermometer;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.interfaces.IShareDialogListener;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.adapters.CheckPointsAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.dialogs.LinesDialogFragment;
import com.mytpg.program.dialogs.ShareDialog;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class ThermometerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String ARG_SAVED_THERMOMETER = "savedThermometer";
    public static final String ARG_SAVED_CHECKPOINTS = "savedCheckPoints";
    public static final String ARG_SAVED_FIRST_POSITION = "savedFirstPosition";
    public static final String ARG_SAVED_DEPARTURE_CODE = "savedDepartureCode";
    public static final String ARG_SAVED_LAST_UPDATED = "savedLastUpdated";
    public static final String ARG_DEPARTURE_CODE = "departureCode";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CheckPointsAdapter mCheckPointsAdapter;
    private TextView mDestinationTV = null;
    private TextView mLastUpdatedTV = null;
    private TextView mLineTV = null;
    private TextView mStopTV = null;

    private Thermometer mThermometer = null;
    private int mFirstPosition = 0;
    private Calendar mLastUpdated = DateTools.getCurrentDate();
    private int mDepartureCode = -1;

    public static Bundle createBundle(int ArgDepartureCode)
    {
        Bundle bundle = new Bundle();

        bundle.putInt(ARG_DEPARTURE_CODE,ArgDepartureCode);

        return bundle;
    }

    public static Bundle createBundleFromUrl(Uri argUrl) {
        String departureCodeText = argUrl.getQueryParameter("horaireRef");
        int departureCode = -1;
        if (departureCodeText != null && !departureCodeText.isEmpty())
        {
            departureCode = Integer.valueOf(departureCodeText).intValue();
        }

        if (departureCode == -1)
        {
            return null;
        }

        return ThermometerFragment.createBundle(departureCode);
    }


    public static ThermometerFragment newInstance() {
        return new ThermometerFragment();
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_thermometer);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d("EVENT", "ON SAVE INSTANCE STATE");

        if (mCheckPointsAdapter == null)
        {
            return;
        }

        ArrayList<CheckPoint> checkPoints = mCheckPointsAdapter.getCheckPointsToSave();
        if (checkPoints == null || checkPoints.size() == 0)
        {
            return;
        }

        outState.putInt(ARG_SAVED_FIRST_POSITION,mFirstPosition);
        outState.putLong(ARG_SAVED_LAST_UPDATED, mLastUpdated.getTimeInMillis());
        outState.putInt(ARG_SAVED_DEPARTURE_CODE,mDepartureCode);
        outState.putParcelable(ARG_SAVED_THERMOMETER, mThermometer);
        outState.putParcelableArrayList(ARG_SAVED_CHECKPOINTS, checkPoints);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d("EVENT", "ON ACTIVITY CREATED");
        List<CheckPoint> checkPoints = new ArrayList<>();
        if (savedInstanceState != null)
        {
            checkPoints = savedInstanceState.getParcelableArrayList(ARG_SAVED_CHECKPOINTS);
            mLastUpdated = DateTools.getCurrentDate();
            long millis = savedInstanceState.getLong(ARG_SAVED_LAST_UPDATED,-1);
            if (millis != -1)
            {
                mLastUpdated.setTimeInMillis(millis);
            }
            mDepartureCode = savedInstanceState.getInt(ARG_SAVED_DEPARTURE_CODE, -1);
            mThermometer = savedInstanceState.getParcelable(ARG_SAVED_THERMOMETER);
            mFirstPosition = savedInstanceState.getInt(ARG_SAVED_FIRST_POSITION);
        }


        if (mThermometer == null && (checkPoints == null || checkPoints.isEmpty()))
        {
            loadData();
        }
        else
        {
            if (checkPoints == null || checkPoints.size() == 0)
            {
                checkPoints = mThermometer.getCheckPoints();
            }

            if (mCheckPointsAdapter == null)
            {
                mCheckPointsAdapter = new CheckPointsAdapter(getActivity(), checkPoints);
            }
            else {
                mCheckPointsAdapter.setCheckPoints(checkPoints);
            }
            mRecyclerView.setAdapter(mCheckPointsAdapter);

            /*ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCheckPointsAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRecyclerView);*/

            mCheckPointsAdapter.setOnItemClickListener(new CheckPointsAdapterClickListener());

            updateInfos();

        }
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thermometer, container, false);

        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelableArrayList(ARG_SAVED_CHECKPOINTS) != null) {
                Log.d("DATA CHECKPOINT", savedInstanceState.getParcelableArrayList(ARG_SAVED_CHECKPOINTS).toString());
            }
        }
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            if (bundle != null) {
                if (bundle.getParcelableArrayList(ARG_SAVED_CHECKPOINTS) != null) {
                    Log.d("DATA CHECKPOINT 2", bundle.getParcelableArrayList(ARG_SAVED_CHECKPOINTS).toString());
                }
            }
            mDepartureCode = bundle.getInt(ThermometerFragment.ARG_DEPARTURE_CODE,-1);

            if (mDepartureCode == -1)
            {
                getMainActivity().onBackPressed();
            }
            else
            {
                setTitle(getTitle());
            }
        }
        else
        {
            getMainActivity().onBackPressed();
        }

        mDestinationTV = (TextView)rootView.findViewById(R.id.destinationTV);
        mLastUpdatedTV = (TextView)rootView.findViewById(R.id.lastUpdatedTV);
        mLineTV = (TextView)rootView.findViewById(R.id.lineTV);
        mStopTV = (TextView)rootView.findViewById(R.id.stopTV);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.thermometer, menu);
        inflater.inflate(R.menu.share, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_hhmm :
                if (mCheckPointsAdapter != null)
                {
                    mCheckPointsAdapter.setHHMM(!mCheckPointsAdapter.isHHMM());
                }
                return true;
            case R.id.action_day_departures:
                showDayDepartures();
                return true;
            case R.id.action_share:
                share();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDayDepartures() {
        Bundle bundle = DayDeparturesFragment.createBundle(mThermometer.getStop().getMnemo().getName(),
                                                           mThermometer.getLine().getId(),
                                                           mThermometer.getLine().getArrivalStop().getCode(),
                                                           mThermometer.getLine().getArrivalStop().getName(),
                                                           false);
        DayDeparturesFragment ddf = DayDeparturesFragment.newInstance();
        ddf.setArguments(bundle);

        changeFragment(ddf,true);

    }

    private void share() {
        ShareDialog sd = ShareDialog.newInstance(getActivity());
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
        //http://m.tpg.ch/thermometer.htm?destination=RIVE&ligne=10&mnemoDepart=BAIR&horaireRef=11458
        String url = String.format(DataSettings.URL_MOBILE_TPG_THERMOMETER, mThermometer.getLine().getArrivalStop().getCode().trim(),
                                                                            mThermometer.getLine().getName().trim(),
                                                                            mThermometer.getStop().getMnemo().getName().trim(),
                                                                            mDepartureCode);

        if (getMainActivity() != null)
        {
            getMainActivity().shareLink(String.format("%1s -> %2s",mThermometer.getStop().getName(), mThermometer.getLine().getArrivalStop().getName()),url);
        }
    }

    private void shareText(boolean argIsForMessage)
    {
        if (mThermometer == null || mCheckPointsAdapter == null || mCheckPointsAdapter.getItemCount() == 0)
        {
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(DateTools.dateToString(DateTools.getCurrentDate(), DateTools.FormatType.WithoutSeconds)).append("\n");

        sb.append(String.format("%1s -> %2s \n", mThermometer.getStop().getName(), mThermometer.getLine().getArrivalStop().getName()));
        int stopNumber = 0;
        for (CheckPoint chP : mCheckPointsAdapter.getCheckPointsToSave())
        {
            if (chP.getArrivalTime() > -1 && chP.isVisible()) {
                sb.append(chP.toShareText(argIsForMessage, stopNumber)).append("\n");
                stopNumber++;
            }
        }

        if (getMainActivity() != null)
        {
            getMainActivity().shareText(String.format("%1s -> %2s",mThermometer.getStop().getName(), mThermometer.getLine().getArrivalStop().getName()), sb.toString());
        }
    }


    private void loadData()
    {
        loadThermometer();
    }

    private void loadThermometer()
    {
        mSwipeRefreshLayout.setRefreshing(true);
        ThermometerAPI thermometerAPI = new ThermometerAPI(getActivity());
        thermometerAPI.getByCode(mDepartureCode, new IAPIListener<Thermometer>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissSwipeRefresh();
            }

            @Override
            public void onSuccess(Thermometer argObject) {
                mThermometer = argObject;

                final Handler handler = new Handler();
                new Runnable() {
                    @Override
                    public void run() {
                        LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
                        List<Line> lines = lineDAO.getAll();
                        List<CheckPoint> checkPoints = mThermometer.getCheckPoints();

                        int k = 0;
                        int indexLine = -1;
                        for (Line line : lines) {
                            if (mThermometer.getLine().getName().equalsIgnoreCase(line.getName())) {
                                if (indexLine == -1) {
                                    indexLine = k;
                                }
                                if (line.getArrivalStop().getCode().equalsIgnoreCase(mThermometer.getLine().getArrivalStop().getCode()) ||
                                        line.getArrivalStop().getName().equalsIgnoreCase(mThermometer.getLine().getArrivalStop().getName())) {
                                    mThermometer.getLine().setId(line.getId());
                                }
                                mThermometer.getLine().setColor(line.getColor());
                            }


                            int i = 0;
                            while (i < checkPoints.size()) {
                                CheckPoint chP = checkPoints.get(i);

                                if (chP.getLine().getName().equalsIgnoreCase(line.getName())) {
                                    chP.getLine().setColor(line.getColor());
                                }
                                int j = 0;
                                while (j < chP.getStop().getConnections().size()) {
                                    Line currentConn = chP.getStop().getConnections().get(j);
                                    if (currentConn.getName().equalsIgnoreCase(line.getName())) {
                                        currentConn.setColor(line.getColor());
                                    }
                                    j++;
                                }

                                i++;
                            }
                            k++;
                        }

                        if (mThermometer.getLine().getId() == -1 && indexLine > -1) {
                            mThermometer.getLine().setId(lines.get(indexLine).getId());
                        }

                        if (!checkPoints.isEmpty()) {
                            CheckPoint lastChP = checkPoints.get(checkPoints.size() - 1);
                            if (lastChP != null && lastChP.getStop().getName().isEmpty()) {
                                lastChP.setStop(new Stop(mThermometer.getLine().getArrivalStop()));
                            }
                        }

                        boolean firstVisibleFound = false;
                        int index = -1;

                        for (CheckPoint chP : mThermometer.getCheckPoints())
                        {
                            if (!firstVisibleFound)
                            {
                                if (chP.getArrivalTime() > -1 && chP.isVisible())
                                {
                                    firstVisibleFound = true;
                                }
                                else
                                {
                                    index++;
                                }
                            }
                        }


                        final int FirstVisibleIndex = index;



                        //m_lastUpdatedTV.setText(sb.toString());
                        //e(m_thermometer.getLine().getName() + " => " + m_thermometer.getLine().getArrivalStop().getName());


                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                mLastUpdated = DateTools.getCurrentDate();
                                updateInfos();

                                mCheckPointsAdapter = new CheckPointsAdapter(getActivity(), mThermometer.getCheckPoints());
                                mRecyclerView.setAdapter(mCheckPointsAdapter);
                                showTuto();
                                mCheckPointsAdapter.setOnItemClickListener(new CheckPointsAdapterClickListener());
                                dismissSwipeRefresh();

                                mRecyclerView.post(new Runnable() {

                                    @Override
                                    public void run() {

                                        mFirstPosition = FirstVisibleIndex + 1;
                                        mLayoutManager.scrollToPositionWithOffset(mFirstPosition,0);
                                    }
                                });
                            }
                        });

                    }

                }.run();
            }

            @Override
            public void onSuccess(List<Thermometer> argObjects) {
            }
        });
    }

    @Override
    public void search(String argSearchText) {

    }

    private void showTuto() {
        if (!isAdded())
        {
            return;
        }
        mTutorialManager.getTutorials().clear();
        App app = (App) getContext().getApplicationContext();
        if (app != null)
        {
            if (app.isFirstMenuDayDepartures()) {
                Tutorial dayDeparturesTuto = new Tutorial(getContext().getString(R.string.action_day_departures), getContext().getString(R.string.showcase_click_to_day_departures), getActivity().findViewById(R.id.action_day_departures));
                mTutorialManager.addTutorial(dayDeparturesTuto);
                app.setFirstMenuDayDepartures(false);
            }
        }

        mTutorialManager.launchTutorials();

        if (mTutorialManager.getShowcaseView() != null) {

            mTutorialManager.getShowcaseView().setOnShowcaseEventListener(new OnShowcaseEventListener() {
                @Override
                public void onShowcaseViewHide(ShowcaseView showcaseView) {

                }

                @Override
                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                    if (mCheckPointsAdapter != null) {
                        mCheckPointsAdapter.showTuto();
                    }
                }

                @Override
                public void onShowcaseViewShow(ShowcaseView showcaseView) {

                }

                @Override
                public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                }
            });
        }
        else if (mCheckPointsAdapter != null)
        {
            mCheckPointsAdapter.showTuto();
        }
    }

    private void updateInfos() {
        mLineTV.setText(mThermometer.getLine().getName());
        LineTools.configureTextView(mLineTV, mThermometer.getLine());
        mStopTV.setText(mThermometer.getStop().getName());
        mDestinationTV.setText(mThermometer.getLine().getArrivalStop().getName());

        final String LastUpdated = DateTools.dateToString(mLastUpdated, DateTools.FormatType.OnlyHourWithoutSeconds);
        mLastUpdatedTV.setText(LastUpdated);// + " (" + String.valueOf(m_departureCode) + ")");
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    private class CheckPointsAdapterClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CheckPoint checkPoint = mCheckPointsAdapter.getItem(position);
            if (checkPoint == null)
            {
                return;
            }

            if (view instanceof TextView)
            {
                TextView tv = (TextView) view;
                if (tv != null && tv.getText().toString().equalsIgnoreCase("..."))
                {
                    if (getMainActivity() != null)
                    {
                        Bundle bundle = LinesDialogFragment.createBundle(checkPoint.getStop().getConnections(),true);
                        getMainActivity().openDialogFragment(MainActivity.DialogFragmentName.Lines,bundle);
                    }
                }
                return;
            }
            else
            {
                NextDeparturesFragment nextDeparturesFragment = NextDeparturesFragment.newInstance();
                Bundle bundle = NextDeparturesFragment.createBundle(checkPoint.getStop().getMnemo().getName(),checkPoint.getCode());

                nextDeparturesFragment.setArguments(bundle);

                changeFragment(nextDeparturesFragment,true);
            }


        }
    }
}
