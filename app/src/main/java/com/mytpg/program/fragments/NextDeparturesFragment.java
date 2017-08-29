package com.mytpg.program.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.mytpg.engines.data.api.DepartureAPI;
import com.mytpg.engines.data.dao.ConnectionDAO;
import com.mytpg.engines.data.dao.DepartureAlarmDAO;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.MnemoDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.DepartureAlarm;
import com.mytpg.engines.entities.Disruption;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.interfaces.IShareDialogListener;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.AlarmSettings;
import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.adapters.CheckPointsAdapter;
import com.mytpg.program.adapters.NextDeparturesAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.dialogs.DisruptionsDialogFragment;
import com.mytpg.program.dialogs.ShareDialog;
import com.mytpg.program.fragments.core.BaseFragment;
import com.mytpg.program.tools.AlarmTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class NextDeparturesFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String ARG_MNEMO = "mnemo";
    public static final String ARG_FILTER = "filter";
    public static final String ARG_SAVED_CONNECTIONS = "savedConnections";
    public static final String ARG_SAVED_DEPARTURE_CODE = "savedDepartureCode";
    public static final String ARG_SAVED_DEPARTURES = "savedDepartures";
    public static final String ARG_SAVED_LAST_UPDATED = "savedLastUpdated";
    public static final String ARG_SAVED_STOP = "savedStop";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CheckPointsAdapter mCheckPointsAdapter;
    private TextView mLastUpdatedTV = null;

    private List<Line> mConnections = new ArrayList<>();
    private int mDepartureCode = -1;
    private NextDeparturesAdapter mNextDeparturesAdapter = null;
    private Stop mStop = null;
    private boolean mIsFirstTime = true;
    private Calendar mLastUpdated = DateTools.now();
    private List<Departure> mDepartures = new ArrayList<>();

    public static NextDeparturesFragment newInstance() {
        NextDeparturesFragment fragment = new NextDeparturesFragment();

        return fragment;
    }

    /**
     *
     */
    public NextDeparturesFragment() {
        super();
    }

    public static Bundle createBundle(String argMnemo, int argDepartureCode, ArrayList<String> argFilter)
    {
        Bundle bundle = new Bundle();

        bundle.putString(ARG_MNEMO,argMnemo);
        bundle.putInt(ThermometerFragment.ARG_DEPARTURE_CODE,argDepartureCode);
        if (argFilter != null && !argFilter.isEmpty()) {
            bundle.putStringArrayList(ARG_FILTER, argFilter);
        }

        return bundle;
    }

    public static Bundle createBundle(String argMnemo, List<Connection> argConnections)
    {
        ArrayList<String> filters = new ArrayList<>();

        for(Connection conn: argConnections)
        {
            filters.add(String.valueOf(conn.getLine().getId()));
        }

        return NextDeparturesFragment.createBundle(argMnemo, -1, filters);
    }

    public static Bundle createBundle(String argMnemo, ArrayList<String> argFilter)
    {
        return NextDeparturesFragment.createBundle(argMnemo, -1, argFilter);
    }


    public static Bundle createBundle(String argMnemo)
    {
        return createBundle(argMnemo,-1,null);
    }


    public static Bundle createBundle(String argMnemo, int argDepartureCode)
    {
        return createBundle(argMnemo,argDepartureCode,null);
    }

    public static Bundle createBundleFromUrl(Uri argUrl, Context argContext) {
        String mnemo = argUrl.getQueryParameter("mnemo");
        String departureCodeText = argUrl.getQueryParameter("horaireRef");
        MnemoDAO mnemoDAO = new MnemoDAO(DatabaseHelper.getInstance(argContext));
        if (mnemoDAO.findByName(mnemo) ==  null)
        {
            return null;
        }

        int departureCode = -1;
        if (departureCodeText != null && !departureCodeText.isEmpty())
        {
            departureCode = Integer.valueOf(departureCodeText).intValue();
        }

        return NextDeparturesFragment.createBundle(mnemo,departureCode);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mNextDeparturesAdapter == null)
        {
            return;
        }
        ArrayList<Departure> departures = mNextDeparturesAdapter.getDeparturesToSave();
        if (departures == null || departures.size() == 0)
        {
            return;
        }

        ArrayList<Line> lines = new ArrayList<Line>();
        for (Line line : mConnections)
        {
            lines.add(line);
        }
        outState.putInt(ARG_SAVED_DEPARTURE_CODE,mDepartureCode);
        outState.putParcelableArrayList(ARG_SAVED_CONNECTIONS,lines);
        outState.putParcelable(ARG_SAVED_STOP,mStop);
        outState.putLong(ARG_SAVED_LAST_UPDATED,mLastUpdated.getTimeInMillis());
        outState.putParcelableArrayList(ARG_SAVED_DEPARTURES, departures);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mLastUpdated = DateTools.getCurrentDate();
        ArrayList<Departure> departures = null;
        Stop stop = null;

        if (savedInstanceState != null)
        {
            departures = savedInstanceState.getParcelableArrayList(ARG_SAVED_DEPARTURES);
            long millis = savedInstanceState.getLong(ARG_SAVED_LAST_UPDATED,-1);
            if (millis != -1)
            {
                mLastUpdated.setTimeInMillis(millis);
            }
            stop = savedInstanceState.getParcelable(ARG_SAVED_STOP);
            mDepartureCode = savedInstanceState.getInt(ARG_SAVED_DEPARTURE_CODE,-1);
            ArrayList<Line> lines = savedInstanceState.getParcelableArrayList(ARG_SAVED_CONNECTIONS);
            if (lines != null) {
                mConnections = new ArrayList<Line>();
                for (Line line : lines) {
                    mConnections.add(line);
                }
            }
        }

        if (departures == null || departures.isEmpty())
        {
            loadData();
        }
        else
        {

            if (stop == null && mStop != null)
            {
                stop = mStop;
            }

            if (stop != null)
            {
                mStop = stop;
                setFavorite(mStop.isFavorite(), mStop.isFavoriteDetailled());
            }

            mLastUpdatedTV.setText(DateTools.dateToString(mLastUpdated, DateTools.FormatType.OnlyHourWithoutSeconds));
            mNextDeparturesAdapter = new NextDeparturesAdapter(getActivity(),departures,mDepartureCode);
            mNextDeparturesAdapter.setOnItemClickListener(new NextDeparturesAdapterClickListener());
            mNextDeparturesAdapter.setOnItemLongClickListener(new NextDeparturesAdapterClickListener());

            mRecyclerView.setAdapter(mNextDeparturesAdapter);

            int lastPosition = mNextDeparturesAdapter.lastIndexWaiting();
            if (lastPosition > 0 && lastPosition < mNextDeparturesAdapter.getItemCount())
            {
                mLayoutManager.scrollToPosition(lastPosition);
                // mLayoutManager.scrollToPositionWithOffset(lastPosition,0);
            }
        }

        super.onActivityCreated(savedInstanceState);

        updateFabVisibility(true);
        updateFabDrawable(R.drawable.ic_action_filter);
    }

    @Override
    public void fabClicked() {
        super.fabClicked();

        filter();
    }

    private void filter()
    {
        CharSequence[] choices = new CharSequence[mStop.getConnections().size()];
        boolean[] choicesBool = new boolean[choices.length];
        int i = 0;
        while (i < choicesBool.length)
        {
            Line currentLine = mStop.getConnections().get(i);
            choices[i] = currentLine.getName() + " : " + currentLine.getArrivalStop().getName();
            boolean found = false;
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
            }

            choicesBool[i] = found;
            i++;
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());

        adb.setTitle(getString(R.string.action_filter));
        adb.setMultiChoiceItems(choices, choicesBool, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                Line lineConnection = mStop.getConnections().get(which);

                int j = mConnections.size() -1;
                if (isChecked)
                {
                    mConnections.add(lineConnection);
                }
                else
                {
                    while (j >= 0)
                    {
                        Line currentConn = mConnections.get(j);
                        /*if (currentConn.getName().equalsIgnoreCase(lineConnection.getName()) &&
                                currentConn.getArrivalStop().getName().equalsIgnoreCase(lineConnection.getArrivalStop().getName()))
                        {
                            mConnections.remove(j);
                            break;
                        }*/
                        if (currentConn.getId() == lineConnection.getId())
                        {
                            mConnections.remove(j);
                            break;
                        }
                        j--;
                    }
                }

            }
        } );

        adb.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mConnections.isEmpty())
                {
                    mConnections = new ArrayList<>(mStop.getConnections());
                }
                loadData();
            }
        });

        adb.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        adb.show();
    }

    private void setFavorite(boolean argIsFavorite, boolean argIsDetailled) {
        MainActivity mainActivity = getMainActivity();
        if (mainActivity == null)
        {
            return;
        }

        if (argIsFavorite)
        {
            if (argIsDetailled)
            {
                mainActivity.changeToolbarMenuDrawable(R.id.action_favorite, R.drawable.ic_action_semi_favorite);
            }
            else {
                mainActivity.changeToolbarMenuDrawable(R.id.action_favorite, R.drawable.ic_action_favorite);
            }
        }
        else {
            mainActivity.changeToolbarMenuDrawable(R.id.action_favorite,R.drawable.ic_action_no_favorite);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_next_departures, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark);


        mLastUpdatedTV = (TextView)rootView.findViewById(R.id.lastUpdatedTV);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);
        // mRecentThermometersRV = (RecyclerView)rootView.findViewById(R.id.recentThermometersRV);

        mRecyclerView.setHasFixedSize(true);
        // mRecentThermometersRV.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mStop = new Stop();
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            String mnemo = bundle.getString(NextDeparturesFragment.ARG_MNEMO);
            mDepartureCode = bundle.getInt(ThermometerFragment.ARG_DEPARTURE_CODE,-1);
            ArrayList<String >filter  = bundle.getStringArrayList(NextDeparturesFragment.ARG_FILTER);

            StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));

            mStop = stopDAO.find(mnemo);

            if (mStop != null)
            {
                if (filter != null && filter.size() > 0)
                {
                    List<Long> lineIds = new ArrayList<Long>();
                    for (String lineIdString : filter)
                    {
                        lineIds.add(Long.valueOf(lineIdString));
                    }
                    ConnectionDAO connDAO = (ConnectionDAO)getApp().getAbsDAOFact().getAbsConnectionDAO();
                    List<Connection> connections = connDAO.getConnectionsByStop(mStop.getId());
                    int i = connections.size()-1;
                    while (i >= 0)
                    {
                        Connection conn = connections.get(i);

                        if (lineIds.contains(conn.getLine().getId()))
                        {
                            mConnections.add(conn.getLine());
                        }
                        i--;
                    }

                    mIsFirstTime = false;
                }

                setFavorite(mStop.isFavorite(), mStop.isFavoriteDetailled());
                setTitle(mStop.getName());
                //updateView();
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


        return rootView;
    }

    private boolean addToFavorite() {
        StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));

        boolean done = stopDAO.addFavorite(mStop, false);

        if (done)
        {
            mStop.setFavorite(true);
            Snackbar.make(getView(),getString(R.string.favorite_stop_added),Snackbar.LENGTH_SHORT).show();
        }
        else
        {
            Snackbar.make(getView(),getString(R.string.favorite_stop_error_added),Snackbar.LENGTH_SHORT).show();
        }

        return done;
    }

    private boolean removeFromFavorite() {
        StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));

        boolean done = stopDAO.removeFavorite(mStop);
        if (done)
        {
            mStop.setFavorite(false);
            mStop.setFavoriteDetailled(false);
            Snackbar.make(getView(),getString(R.string.favorite_stop_removed),Snackbar.LENGTH_SHORT).show();
        }
        else
        {
            Snackbar.make(getView(),getString(R.string.favorite_stop_error_removed),Snackbar.LENGTH_SHORT).show();
        }

        return done;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.favorite, menu);
        inflater.inflate(R.menu.share,menu);

    }

    private void loadData() {
        loadNextDepartures();
        //mStop = new Stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_favorite :
                changeFavoriteStop();
                return true;
            case R.id.action_share :
                share();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        String url;
        if (mDepartureCode != -1) {
            url = String.format(DataSettings.URL_MOBILE_TPG_DEPARTURE_WITH_CODE, mStop.getMnemo().getName().trim(), mDepartureCode);
        }
        else
        {
            url = String.format(DataSettings.URL_MOBILE_TPG_DEPARTURE, mStop.getMnemo().getName());
        }

        if (getMainActivity() != null)
        {
            getMainActivity().shareLink(getString(R.string.next_departures_stop,mStop.getName()),url);
        }
    }

    private void shareText(boolean argIsForMessage)
    {
        if (mNextDeparturesAdapter == null || mNextDeparturesAdapter.getItemCount() == 0)
        {
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(DateTools.dateToString(DateTools.getCurrentDate(), DateTools.FormatType.WithoutSeconds)).append("\n");

        sb.append(mStop.getName()).append("\n");
        for (Departure dep : mNextDeparturesAdapter.getDeparturesToSave())
        {
            sb.append(dep.toShareText(argIsForMessage)).append("\n");
        }

        if (getMainActivity() != null)
        {
            getMainActivity().shareText(getString(R.string.next_departures_stop,mStop.getName()), sb.toString());
        }
    }

    private void changeFavoriteStop() {
        boolean result;

        if (mStop.isFavorite())
        {
            result = removeFromFavorite();
        }
        else
        {
            result = addToFavorite();
        }

        if (result)
        {
            setFavorite(mStop.isFavorite(), mStop.isFavoriteDetailled());
        }
    }

    private void loadNextDepartures()
    {
        mSwipeRefreshLayout.setRefreshing(true);
        Line[] connections = null;
        if (mConnections != null)
        {
            if (mConnections.size() == mStop.getConnections().size())
            {
                connections = new Line[0];
            }
            else
            {
                connections = new Line[mConnections.size()];
                mConnections.toArray(connections);
            }
        }

        DepartureAPI depAPI = new DepartureAPI(getActivity());
        depAPI.getAllByMnemo(mStop.getMnemo().getName(), connections, mDepartureCode, new IAPIListener<Departure>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissSwipeRefresh();
                Snackbar.make(getView(),getString(R.string.error_loading),Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(Departure argObject) {

            }

            @Override
            public void onSuccess(List<Departure> argObjects) {
                mDepartures = argObjects;

                updateLineColors();
            }
        });
    }

    private void updateLineColors() {
        final Handler handler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                DepartureAlarmDAO depAlarmDAO = new DepartureAlarmDAO(DatabaseHelper.getInstance(getActivity()));
                List<DepartureAlarm> depAlarms = depAlarmDAO.getAll();

                if (mStop != null)
                {
                    if (mIsFirstTime)
                    {
                        mConnections = new ArrayList<>(mStop.getConnections());
                        mIsFirstTime = false;
                    }
                    setFavorite(mStop.isFavorite(), mStop.isFavoriteDetailled());


                }

                LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
                for (Departure dep : mDepartures)
                {
                    if (mStop != null) {
                        dep.setStop(new Stop(mStop));
                    }
                    Line line = lineDAO.findByLine(dep.getLine());
                    if (line != null)
                    {
                        dep.getLine().setId(line.getId());
                        dep.getLine().setColor(line.getColor());
                    }

                    for (DepartureAlarm depAlarm : depAlarms)
                    {
                        if (depAlarm.getDepartureCode() == dep.getCode())
                        {
                            dep.setAlarm(true);
                            break;
                        }
                    }
                }


                mNextDeparturesAdapter = new NextDeparturesAdapter(getActivity(),mDepartures,mDepartureCode);
                mRecyclerView.setAdapter(mNextDeparturesAdapter);
                mNextDeparturesAdapter.setOnItemClickListener(new NextDeparturesAdapterClickListener());
                mNextDeparturesAdapter.setOnItemLongClickListener(new NextDeparturesAdapterClickListener());


                mLastUpdated = DateTools.getCurrentDate();
                final String LastUpdated = DateTools.dateToString(mLastUpdated, DateTools.FormatType.OnlyHourWithoutSeconds);
                mLastUpdatedTV.setText(LastUpdated);// + " (" + String.valueOf(m_departureCode) + ")");

                int lastPosition = mNextDeparturesAdapter.lastIndexWaiting();
                if (lastPosition > 0 && lastPosition < mNextDeparturesAdapter.getItemCount())
                {
                    mLayoutManager.scrollToPosition(lastPosition);
                    //mLayoutManager.scrollToPositionWithOffset(lastPosition,0);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissSwipeRefresh();
                        showTuto();
                    }
                });
            }
        }.run();
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
            if (app.isFirstFilterNextDepartures()) {
                Tutorial filterTuto = new Tutorial(getContext().getString(R.string.action_filter), getContext().getString(R.string.showcase_click_to_filter_next_departures), m_fab);
                filterTuto.setButtonBottomRight(false);
                mTutorialManager.addTutorial(filterTuto);
                app.setFirstFilterNextDepartures(false);
            }
            if (app.isFirstFavoriteStop()) {
                Tutorial favoriteTuto = new Tutorial(getContext().getString(R.string.menu_favorites_stops), getContext().getString(R.string.showcase_click_to_favorite_this_stop), getActivity().findViewById(R.id.action_favorite));
                mTutorialManager.addTutorial(favoriteTuto);
                app.setFirstFavoriteStop(false);
            }
            if (app.isFirstShare())
            {
                Tutorial shareTuto = new Tutorial(getContext().getString(R.string.action_share), getContext().getString(R.string.showcase_click_to_share), getActivity().findViewById(R.id.action_share));
                mTutorialManager.addTutorial(shareTuto);
                app.setFirstShare(false);
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
                    if (mNextDeparturesAdapter != null) {
                        mNextDeparturesAdapter.showTuto();
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
        else if (mNextDeparturesAdapter != null)
        {
            mNextDeparturesAdapter.showTuto();
        }
    }

    @Override
    public void search(String argSearchText) {

    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    protected boolean fabNeeded() {
        return true;
    }

    @Override
    public String getTitle() {
        String title = super.getTitle();
        if (mStop != null)
        {
            title = mStop.getName();
        }



        return title;
    }


    private class NextDeparturesAdapterClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Departure departure = mNextDeparturesAdapter.getItem(position);
            if (departure == null)
            {
                return;
            }

            if (id == R.id.warningIV)
            {
                for (Disruption disruption : departure.getDisruptions())
                {
                    disruption.setLine(new Line(departure.getLine()));
                }
                Bundle bundle = DisruptionsDialogFragment.createBundle(departure.getDisruptions());
                getMainActivity().openDialogFragment(MainActivity.DialogFragmentName.Disruptions,bundle);

                return;
            }
            else if (id == R.id.alarmIV)
            {
                if (departure.hasAlarm()) {
                    DepartureAlarmDAO departureAlarmDAO = new DepartureAlarmDAO(DatabaseHelper.getInstance(getContext()));
                    List<DepartureAlarm> depAlarms = departureAlarmDAO.getAllByCode(departure.getCode());
                    for (DepartureAlarm depAlarm : depAlarms) {
                        mNextDeparturesAdapter.removeAlarm(departure, position, getView(), depAlarm.getMinutes());
                    }
                }
                else
                {
                    setAlarm(departure, position, getView());
                }

                return;
            }


            if (mNextDeparturesAdapter == null)
            {
                return;
            }

            if (m_fab != null && id == m_fab.getId())
            {
                return;
            }


            Bundle bundle = ThermometerFragment.createBundle(departure.getCode());
            ThermometerFragment thermometerFragment = ThermometerFragment.newInstance();
            thermometerFragment.setArguments(bundle);
            changeFragment(thermometerFragment,true);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mNextDeparturesAdapter == null) {
                return false;
            }

            Departure departure = mNextDeparturesAdapter.getItem(position);
            if (departure == null || departure.getCode() < 0) {
                return false;
            }

            setAlarm(departure, position, getView());

            return true;
        }
    }

    private void setAlarm(final Departure argDeparture, final int argPosition, final View argView) {
        App app = getApp();
        int minutesBefore = AlarmSettings.MINIMUM_MINUTES_ALARM;
        if (app != null)
        {
            minutesBefore = Integer.valueOf(app.getSharedPreferences().getString(AppSettings.PREF_ALARM_DEPARTURES_TIME, String.valueOf(AlarmSettings.MINIMUM_MINUTES_ALARM)));
        }

        String[] prefChoicesArray = getResources().getStringArray(R.array.pref_time_before_alarm_notification);
        String[] prefChoiceValuesArray = getResources().getStringArray(R.array.pref_time_before_alarm_notification_values);

        List<String> prefChoices = new ArrayList<>(Arrays.asList(prefChoicesArray));

        final List<String> prefChoiceValues= new ArrayList<>(Arrays.asList(prefChoiceValuesArray));

        for (int i = prefChoiceValues.size()-1; i >= 0; i--)
        {
            int value = Integer.valueOf(prefChoiceValues.get(i));
            if (value == -1 || !AlarmTools.canAddAlarm(argDeparture, value))
            {
                prefChoiceValues.remove(i);
                prefChoices.remove(i);
            }
        }

        String[] choices = new String[prefChoices.size()];
        prefChoices.toArray(choices);

        if (prefChoices.size() > 1 && (minutesBefore == -1 || !AlarmTools.canAddAlarm(argDeparture, minutesBefore))) {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                    .setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int finalChoice = which;
                                    dialog.dismiss();
                                    if (mNextDeparturesAdapter != null) {
                                        mNextDeparturesAdapter.setAlarm(argDeparture, argPosition, argView, Integer.valueOf(prefChoiceValues.get(finalChoice)));
                                    }
                                }
                            }

                    )
                    .setCancelable(true)
                    .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setNeutralButton(getString(R.string.action_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (getMainActivity() != null)
                            {
                                getMainActivity().openSettings();
                            }
                        }
                    });
            adb.show();
        }
        else if (prefChoices.size() == 1)
        {
            if (mNextDeparturesAdapter != null) {
                mNextDeparturesAdapter.setAlarm(argDeparture, argPosition, argView, Integer.valueOf(prefChoiceValues.get(0)));
            }
        }
        else
        {
            if (mNextDeparturesAdapter != null)
            {
                mNextDeparturesAdapter.setAlarm(argDeparture, argPosition, argView, AlarmSettings.MINIMUM_MINUTES_ALARM);
            }
        }
    }
}
