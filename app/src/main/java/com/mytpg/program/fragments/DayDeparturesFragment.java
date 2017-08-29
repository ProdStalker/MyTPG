package com.mytpg.program.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.mytpg.engines.data.api.DepartureAPI;
import com.mytpg.engines.data.dao.DepartureAlarmDAO;
import com.mytpg.engines.data.dao.DepartureDAO;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.MnemoDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.DepartureAlarm;
import com.mytpg.engines.entities.HourDeparture;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.interfaces.IShareDialogListener;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.AlarmSettings;
import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.LineTools;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.adapters.HourDeparturesAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.dialogs.ShareDialog;
import com.mytpg.program.fragments.core.BaseFragment;
import com.mytpg.program.tools.AlarmTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by stalker-mac on 18.11.14.
 */
public class DayDeparturesFragment extends BaseFragment {
    public static final String ARG_DESTINATION ="destination";
    public static final String ARG_DESTINATION_CODE ="destinationCode";
    public static final String ARG_IS_OFFLINE = "isOffline";
    public static final String ARG_LINE = "line";
    public static final String ARG_MNEMO = "mnemo";
    private static final String ARG_SAVED_DESTINATION = "savedDestination";
    private static final String ARG_SAVED_LAST_UPDATED = "savedLastUpdated";
    private static final String ARG_SAVED_LINE = "savedLine";
    private static final String ARG_SAVED_STOP = "savedStop";
    private static final String ARG_SAVED_IS_OFFLINE = "savedIsOffline";
    private static final String ARG_SAVED_HOUR_DEPARTURES = "savedHourDepartures";

    private RecyclerView m_departuresRV = null;
    private TextView mDestinationTV = null;
    private TextView mLastUpdatedTV = null;
    private LinearLayoutManager mLayoutManager;
    private TextView mLineTV = null;
    private TextView mStopTV = null;


    private Stop mDestination = null;
    private Line mLine = null;
    private HourDeparturesAdapter mDayDeparturesAdapter = null;
    private Stop mStop = null;

    private boolean mIsOffline = false;
    private Calendar mLastUpdated = DateTools.getCurrentDate();

    public static Bundle createBundle(String ArgMnemo, long ArgLineId, String ArgDestinationCode, String ArgDestination, boolean ArgIsOffline){
        Bundle bundle = new Bundle();

        bundle.putString(ARG_MNEMO, ArgMnemo);
        bundle.putLong(ARG_LINE, ArgLineId);
        bundle.putString(ARG_DESTINATION_CODE, ArgDestinationCode);
        bundle.putString(ARG_DESTINATION, ArgDestination);
        bundle.putBoolean(ARG_IS_OFFLINE, ArgIsOffline);

        return bundle;
    }

    public static Bundle createBundleFromUrl(Uri argUrl, Context argContext) {
        String mnemo = argUrl.getQueryParameter("mnemoDepart");
        String lineName = argUrl.getQueryParameter("ligne");
        String destination = argUrl.getQueryParameter("destination");
        MnemoDAO mnemoDAO = new MnemoDAO(DatabaseHelper.getInstance(argContext));
        if (mnemoDAO.findByName(mnemo) ==  null)
        {
            return null;
        }
        LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(argContext));
        List<Line> lines = lineDAO.getAllByName(lineName);
        Line line = null;
        for (Line cLine : lines)
        {
            if (cLine.getArrivalStop().getCode().equalsIgnoreCase(destination))
            {
                line = cLine;
                break;
            }
        }
        if (line == null)
        {
            return null;
        }

        return DayDeparturesFragment.createBundle(mnemo,line.getId(), line.getArrivalStop().getCode(),line.getArrivalStop().getName(),false);
    }

    public static DayDeparturesFragment newInstance() {
        DayDeparturesFragment ddf = new DayDeparturesFragment();


        return ddf;
    }

    /**
     *
     */
    public DayDeparturesFragment() {
        // TODO Auto-generated constructor stub
    }

    protected void initializeComponents(View ArgRootView) {
        if (ArgRootView == null){
            return;
        }


        m_departuresRV = (RecyclerView)ArgRootView.findViewById(R.id.departuresRV);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        m_departuresRV.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());// new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        m_departuresRV.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mDayDeparturesAdapter = new HourDeparturesAdapter(getActivity(),null);
        m_departuresRV.setAdapter(mDayDeparturesAdapter);

        mDestinationTV = (TextView)ArgRootView.findViewById(R.id.destinationTV);
        mLastUpdatedTV = (TextView)ArgRootView.findViewById(R.id.lastUpdatedTV);
        mLineTV = (TextView)ArgRootView.findViewById(R.id.lineTV);
        mStopTV = (TextView)ArgRootView.findViewById(R.id.stopTV);

    }

    @Override
    public void onActivityCreated(Bundle ArgBundle) {
        ArrayList<HourDeparture> hds = null;
        if (ArgBundle != null)
        {
            hds = ArgBundle.getParcelableArrayList(ARG_SAVED_HOUR_DEPARTURES);
            mLastUpdated = DateTools.getCurrentDate();
            long millis = ArgBundle.getLong(ARG_SAVED_LAST_UPDATED,-1);
            if (millis != -1)
            {
                mLastUpdated.setTimeInMillis(millis);
            }
            mDestination = ArgBundle.getParcelable(ARG_SAVED_DESTINATION);
            mLine = ArgBundle.getParcelable(ARG_SAVED_LINE);
            mStop = ArgBundle.getParcelable(ARG_SAVED_STOP);
            mIsOffline = ArgBundle.getBoolean(ARG_SAVED_IS_OFFLINE,false);
        }

        if (hds == null || hds.isEmpty())
        {
            loadData();
        }
        else
        {
            mDayDeparturesAdapter.setHourDepartures(hds);
            m_departuresRV.setAdapter(mDayDeparturesAdapter);

            mDayDeparturesAdapter.setOnItemClickListener(new HourDeparturesAdapterClickListener());

            updateInfos();
        }
        super.onActivityCreated(ArgBundle);
    }

    @Override
    public String getTitle()
    {
        return getString(R.string.menu_day_departures);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.save, menu);
        inflater.inflate(R.menu.favorite,menu);
        inflater.inflate(R.menu.share,menu);
    }

    /* (non-Javadoc)
         * @see com.otpg.program.core.BaseFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
         */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_day_departures, container, false);



        initializeComponents(rootView);

        mStop = new Stop();
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            mIsOffline = bundle.getBoolean(DayDeparturesFragment.ARG_IS_OFFLINE,false);
            String destinationName = bundle.getString(DayDeparturesFragment.ARG_DESTINATION);
            //String destinationCode = bundle.getString(DayDeparturesFragment.ARG_DESTINATION_CODE);
            long lineId = bundle.getLong(DayDeparturesFragment.ARG_LINE);
            String mnemo = bundle.getString(DayDeparturesFragment.ARG_MNEMO);

            StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));

            mStop = stopDAO.find(mnemo);

            mDestination = stopDAO.search(destinationName);

            if (mDestination != null && lineId == -1)
            {

            }

            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
            mLine = lineDAO.find(lineId);


            if (mStop != null && mLine != null && mDestination != null)
            {

        		/*mLine.getArrivalStop().setName(destinationName);
        		mLine.getArrivalStop().getMnemo().setName(mnemo);
        		mLine.getArrivalStop().setCode(destinationCode);
        		if (mDestination != null)
        		{
        			mLine.getArrivalStop().setId(mDestination.getId());
        		}*/
                setFavorite(mStop.isFavorite());


                //  updateView();
            }
            else
            {
                getMainActivity().onBackPressed();
                return null;
            }



        }
        else
        {
            getMainActivity().onBackPressed();
            return null;
        }




        return rootView;
    }

    private void updateInfos() {
        mLineTV.setText(mLine.getName());
        LineTools.configureTextView(mLineTV,mLine);
        mStopTV.setText(mStop.getName());
        mDestinationTV.setText(mDestination.getName());

        String lastUpdated = DateTools.dateToString(mLastUpdated, DateTools.FormatType.OnlyHourWithoutSeconds);
        if (mIsOffline)
        {
            lastUpdated = getString(R.string.offline_schedules);
            if (mDayDeparturesAdapter != null && mDayDeparturesAdapter.getItemCount() > 0)
            {
                Calendar cal = DateTools.now();
                HourDeparture hd = mDayDeparturesAdapter.getItem(0);
                Departure dep = hd.getDepartures().get(0);
                cal.setTimeInMillis(dep.getDate().getTimeInMillis());

                lastUpdated += " (" + cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + ")";
            }
        }
        mLastUpdatedTV.setText(lastUpdated);
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
            setFavorite(mStop.isFavorite());
        }
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.action_favorite :
                changeFavoriteStop();
                return true;

            case R.id.action_save :
                save();
                return true;

            case R.id.action_share:
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

        // "?destination=%1$s&ligne=%2$s&mnemoDepart=%3$s";
        String url = String.format(DataSettings.URL_MOBILE_TPG_TIMETABLE, mLine.getArrivalStop().getCode(), mLine.getName(), mStop.getMnemo().getName());

        if (getMainActivity() != null)
        {
            getMainActivity().shareLink(getString(R.string.next_day_departures_stop,mStop.getName()),url);
        }
    }

    private void shareText(boolean argIsForMessage)
    {
        if (mDayDeparturesAdapter == null || mDayDeparturesAdapter.getItemCount() == 0)
        {
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(DateTools.dateToString(DateTools.getCurrentDate(), DateTools.FormatType.WithoutSeconds)).append("\n");

        sb.append(String.format("%1$s : %2$s -> %3$s \n", mLine.getName(), mStop.getName(), mLine.getArrivalStop().getName()));
        for (HourDeparture hd : mDayDeparturesAdapter.getHourDepartures())
        {
            sb.append(hd.toShareText(argIsForMessage)).append("\n");
        }

        if (getMainActivity() != null)
        {
            getMainActivity().shareText(getString(R.string.next_day_departures_stop,mStop.getName()), sb.toString());
        }
    }


    private void loadDepartures()
    {
        DepartureAPI depAPI = new DepartureAPI(getActivity());

        depAPI.getDayDepartures(mLine.getName(), mStop.getMnemo().getName(), mDestination.getCode(), new IAPIListener<Departure>() {
            @Override
            public void onError(VolleyError argVolleyError) {
                dismissPD();
            }

            @Override
            public void onSuccess(Departure argObject) {

            }

            @Override
            public void onSuccess(List<Departure> argObjects) {
                new DayDeparturesAsyncTask().execute(argObjects);
            }
        });
    }

    private void loadDeparturesFromDB()
    {
        final Handler handler = new Handler();
        new Runnable(){
            @Override
            public void run() {
                DepartureDAO depDAO = new DepartureDAO(DatabaseHelper.getInstance(getActivity()));
                final List<Departure> departures = depDAO.getDayDepartures(mLine.getName(), mStop.getMnemo().getName(), mDestination.getCode());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new DayDeparturesAsyncTask().execute(departures);
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
            if (app.isFirstDayDepartures()) {
                Tutorial dayDeparturesTuto = new Tutorial(getContext().getString(R.string.action_day_departures), getContext().getString(R.string.showcase_click_to_save_day_departures), getActivity().findViewById(R.id.action_save));
                mTutorialManager.addTutorial(dayDeparturesTuto);
                //app.setFirstDayDepartures(false);
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
                    if (mDayDeparturesAdapter != null) {
                        mDayDeparturesAdapter.showTuto();
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
        else if (mDayDeparturesAdapter != null)
        {
            mDayDeparturesAdapter.showTuto();
        }
    }


    public boolean isFavorite()
    {
        if (mStop != null)
        {
            return mStop.isFavorite();
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mDayDeparturesAdapter == null)
        {
            return;
        }
        ArrayList<HourDeparture> hds = mDayDeparturesAdapter.getHourDeparturesToSave();
        if (hds == null || hds.size() == 0)
        {
            return;
        }
        outState.putLong(ARG_SAVED_LAST_UPDATED, mLastUpdated.getTimeInMillis());
        outState.putParcelable(ARG_SAVED_DESTINATION, mDestination);
        outState.putParcelable(ARG_SAVED_LINE, mLine);
        outState.putParcelable(ARG_SAVED_STOP, mStop);
        outState.putBoolean(ARG_SAVED_IS_OFFLINE, mIsOffline);
        outState.putParcelableArrayList(ARG_SAVED_HOUR_DEPARTURES, hds);
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
            Snackbar.make(getView(),getString(R.string.favorite_stop_removed),Snackbar.LENGTH_SHORT).show();
        }
        else
        {
            Snackbar.make(getView(),getString(R.string.favorite_stop_error_removed),Snackbar.LENGTH_SHORT).show();
        }

        return done;
    }

	/* (non-Javadoc)
	 * @see com.otpg.program.core.BaseFragment#updateView()
	 */

    private void save() {
        if (mDayDeparturesAdapter.getItemCount() == 0 || mDestination == null)
        {
            return;
        }

        Runnable depSave = new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {

                DepartureDAO depDAO = new DepartureDAO(DatabaseHelper.getInstance(getActivity()));

                List<Departure> departures = new ArrayList<>();
                List<HourDeparture> hourDepartures = mDayDeparturesAdapter.getHourDepartures();


                for (HourDeparture hd : hourDepartures)
                {
                    for (Departure dep : hd.getDepartures())
                    {
                        departures.add(dep);
                    }
                }

                boolean success = false;
                if (departures.size() > 0)
                {
                    depDAO.deleteByLineAndStopAndDestination(departures.get(0).getLine().getId(), departures.get(0).getStop().getId(), departures.get(0).getLine().getArrivalStop().getId());
                    if (depDAO.create(departures))
                    {
                        success = true;
                    }

                }

                if (success)
                {
                    Snackbar.make(getView(), getString(R.string.departures_saved), Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    Snackbar.make(getView(), getString(R.string.departures_not_saved), Snackbar.LENGTH_SHORT).show();
                }
            }
        };

        depSave.run();
    }

    protected void loadData() {
        showPD();
        if (mIsOffline)
        {
            loadDeparturesFromDB();
        }
        else
        {
            loadDepartures();
        }

    }


    private class DayDeparturesAsyncTask extends AsyncTask<List<Departure>, Void, List<HourDeparture>> {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            showPD();
        }

        @Override
        protected List<HourDeparture> doInBackground(List<Departure>... lists) {
            List<HourDeparture> hourDepartures;
            try
            {
                LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(getActivity()));
                List<Line> lines = lineDAO.getAll();

                DepartureAlarmDAO depAlarmDAO = new DepartureAlarmDAO(DatabaseHelper.getInstance(getActivity()));
                List<DepartureAlarm> depAlarms = depAlarmDAO.getAll();

                List<Departure> departures = lists[0];

                long stopId = -1;
                Stop stop;
                if (departures.size() > 0)
                {
                    StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));
                    stop = stopDAO.find(departures.get(0).getStop().getMnemo().getName());
                    if (stop != null)
                    {
                        stopId = stop.getId();
                    }
                }

                int i = departures.size()-1;
                while (i >= 0)
                {
                    Departure currentDep = departures.get(i);
                    for (DepartureAlarm depAlarm : depAlarms)
                    {
                        if (currentDep.getCode() == depAlarm.getDepartureCode())
                        {
                            currentDep.setAlarm(true);
                            break;
                        }
                    }


                    if (stopId != -1)
                    {
                        currentDep.getStop().setId(stopId);
                    }
                    if (mDestination != null)
                    {
                        currentDep.getLine().getArrivalStop().setId(mDestination.getId());
                    }

                    boolean found = false;
                    for (Line line : lines)
                    {
                        String lineCode = departures.get(i).getLine().getName();
                        //Log.d("COMPARISON", String.valueOf(lineCode) + "<==>" + String.valueOf(line.getId()));
                        if (lineCode.equalsIgnoreCase(line.getName()))
                        {
                            found = true;

                            currentDep.getLine().setId(line.getId());
                            currentDep.getLine().setColor(line.getColor());
                            break;
                        }
                    }
                    if (!found)
                    {
                        departures.remove(i);
                    }
                    i--;
                }

               /* if (!departures.isEmpty())
                {
                    mStop.setConnections(departures.get(0).getStop().getConnections());
                }*/

                hourDepartures = new ArrayList<HourDeparture>();

                for (Departure dep : departures)
                {
                    final int HourOfDay = dep.getDate().get(Calendar.HOUR_OF_DAY);
                    String hourOfDayText = String.valueOf(HourOfDay);
                    if (hourOfDayText.length() == 1)
                    {
                        hourOfDayText = "0" + hourOfDayText;
                    }
                    //hourOfDayText += "H";

                    boolean added = false;
                    i = 0;
                    while (i < hourDepartures.size())
                    {
                        HourDeparture currentHD = hourDepartures.get(i);
                        if (currentHD.getHourOfDay().equalsIgnoreCase(hourOfDayText))
                        {
                            added = true;
                            currentHD.getDepartures().add(dep);
                            break;
                        }
                        i++;
                    }

                    if (!added)
                    {
                        HourDeparture hd = new HourDeparture();
                        hd.setHourOfDay(hourOfDayText);
                        hd.getDepartures().add(dep);

                        hourDepartures.add(hd);
                    }
                }
            }
            catch (NullPointerException npe)
            {
                npe.printStackTrace();
                hourDepartures = null;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                hourDepartures = null;
            }

            return hourDepartures;

        }

        @Override
        protected void onPostExecute(List<HourDeparture> ArgHourDepartures)
        {
            try
            {

                if (mStop != null)
                {
                   /* if (m_isFirstTime)
                    {
                        //m_connections = new ArrayList<Line>(mStop.getConnections());
                        m_isFirstTime = false;
                    }*/
                    setFavorite(mStop.isFavorite());
                }



                if (isAdded())
                {
                    /*MenuItem actionSave = getMainActivity().getAction(R.id.action_save);
                    if (actionSave != null)
                    {
                        actionSave.setVisible(!mIsOffline);
                    }*/
                    if (getMainActivity() != null)
                    {
                        getMainActivity().changeToolbarMenu(R.id.action_save, !mIsOffline);
                    }

                    mDayDeparturesAdapter = new HourDeparturesAdapter(getActivity(), ArgHourDepartures);

                    m_departuresRV.setAdapter(mDayDeparturesAdapter);
                    showTuto();
                    mDayDeparturesAdapter.setOnItemClickListener(new HourDeparturesAdapterClickListener());

                    List<Line> lines = new ArrayList<>();
                    for (HourDeparture currentHD : ArgHourDepartures)
                    {
                        for (Departure dep : currentHD.getDepartures())
                        {
                            boolean lineFound = false;
                            Line line = dep.getLine();
                            int i = 0;
                            while (i < lines.size())
                            {
                                if (lines.get(i).getName().equalsIgnoreCase(line.getName()) &&
                                        lines.get(i).getArrivalStop().getMnemo().getName().equalsIgnoreCase(line.getArrivalStop().getMnemo().getName()))
                                {
                                    lineFound = true;
                                    break;
                                }
                                i++;
                            }

                            if (!lineFound)
                            {
                                lines.add(line);
                            }
                        }
                    }

                    mLastUpdated = DateTools.getCurrentDate();

                    updateInfos();

                }


                dismissPD();

                if (isAdded())
                {
                    if (ArgHourDepartures == null || ArgHourDepartures.size() == 0)
                    {
                        Toast.makeText(getActivity(), getString(R.string.none_departures),Toast.LENGTH_LONG).show();
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

    private void setFavorite(boolean argIsFavorite) {
        MainActivity mainActivity = getMainActivity();
        if (mainActivity == null)
        {
            return;
        }

        if (argIsFavorite)
        {
            mainActivity.changeToolbarMenuDrawable(R.id.action_favorite,R.drawable.ic_action_favorite);
        }
        else {
            mainActivity.changeToolbarMenuDrawable(R.id.action_favorite,R.drawable.ic_action_no_favorite);
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
                                    if (mDayDeparturesAdapter != null)
                                    {
                                        if (AlarmTools.setAlarm(getActivity(), argDeparture, argView, Integer.valueOf(prefChoiceValues.get(finalChoice)))) {
                                            mDayDeparturesAdapter.notifyItemChanged(argPosition);
                                        }
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
            if (mDayDeparturesAdapter != null)
            {
                if (AlarmTools.setAlarm(getActivity(), argDeparture, argView, Integer.valueOf(prefChoiceValues.get(0)))) {
                    mDayDeparturesAdapter.notifyItemChanged(argPosition);
                }
            }
        }
        else
        {
            if (mDayDeparturesAdapter != null)
            {
                if (AlarmTools.setAlarm(getActivity(), argDeparture, argView, AlarmSettings.MINIMUM_MINUTES_ALARM)) {
                    mDayDeparturesAdapter.notifyItemChanged(argPosition);
                }
            }
        }
    }

    private class HourDeparturesAdapterClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            //Toast.makeText(m_context, DateTools.dateToString(dep.getDate()), Toast.LENGTH_LONG).show();
            if (!(view instanceof TextView))
            {
                return;
            }

            TextView tv = (TextView)view;
            if (tv == null)
            {
                return;
            }

            //final int Position = (int)tv.getTag(TAG_POSITION);
            final int I = (Integer)tv.getTag();

            HourDeparture hd = mDayDeparturesAdapter.getItem(position);
            if (hd == null)
            {
                return;
            }

            if (I >= 0 && I < hd.getDepartures().size())
            {
                Departure currentDep = hd.getDepartures().get(I);
                if (currentDep.hasAlarm())
                {
                    DepartureAlarmDAO departureAlarmDAO = new DepartureAlarmDAO(DatabaseHelper.getInstance(getContext()));
                    List<DepartureAlarm> depAlarms = departureAlarmDAO.getAllByCode(currentDep.getCode());
                    for (DepartureAlarm depAlarm : depAlarms) {
                        if (AlarmTools.removeAlarm(getActivity(), currentDep, true, getView(), depAlarm.getMinutes())) {
                            mDayDeparturesAdapter.notifyItemChanged(position);
                        }
                    }
                }
                else
                {
                    setAlarm(currentDep, position, getView());
                }
            }
        }
    }

}
