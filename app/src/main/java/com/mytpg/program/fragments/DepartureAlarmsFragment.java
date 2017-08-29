package com.mytpg.program.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.mytpg.engines.data.dao.DepartureAlarmDAO;
import com.mytpg.engines.entities.DepartureAlarm;
import com.mytpg.engines.entities.Disruption;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.adapters.DepartureAlarmsAdapter;
import com.mytpg.program.dialogs.DisruptionsDialogFragment;
import com.mytpg.program.fragments.core.BaseFragment;
import com.mytpg.program.tools.AlarmTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class DepartureAlarmsFragment extends BaseFragment {
    public static final String ARG_SAVED_DEPARTURE_ALARMS = "savedDepartureAlarms";
    public static final String ARG_MINUTES = "minutes";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private DepartureAlarmsAdapter mDepartureAlarmsAdapter;

    private List<DepartureAlarm> mDepartureAlarms;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mDepartureAlarmsAdapter == null)
        {
            return;
        }

        ArrayList<DepartureAlarm> departureAlarms = mDepartureAlarmsAdapter.getDepartureAlarmsToSave();

        if (!departureAlarms.isEmpty()) {
            outState.putParcelableArrayList(ARG_SAVED_DEPARTURE_ALARMS, departureAlarms);
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_departures_alarms);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ArrayList<DepartureAlarm> departureAlarms = null;
        if (savedInstanceState != null)
        {
            departureAlarms = savedInstanceState.getParcelableArrayList(ARG_SAVED_DEPARTURE_ALARMS);
        }

        if (departureAlarms == null || departureAlarms.isEmpty())
        {
            loadData();
        }
        else
        {
            if (mDepartureAlarmsAdapter == null)
            {
                mDepartureAlarmsAdapter = new DepartureAlarmsAdapter(getActivity(), departureAlarms);
            }
            else {
                mDepartureAlarmsAdapter.setDepartureAlarms(departureAlarms);
            }
            mRecyclerView.setAdapter(mDepartureAlarmsAdapter);


            mDepartureAlarmsAdapter.setOnItemClickListener(new DepartureAlarmsAdapterClickListener());

        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_departure_alarms, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        //loadData();
        return rootView;
    }

    private void loadData()
    {
        loadDepartureAlarms();
    }

    private void loadDepartureAlarms()
    {
        new LoadDepartureAlarmsAsyncTask().execute();
    }

    @Override
    public boolean needToBeFullViewport() {
        return false;
    }

    @Override
    public void search(String argSearchText) {

    }

    private class LoadDepartureAlarmsAsyncTask extends AsyncTask<Void,Void,List<DepartureAlarm>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showPD();
        }

        @Override
        protected void onPostExecute(List<DepartureAlarm> departureAlarms) {

            try
            {
                super.onPostExecute(departureAlarms);
                mDepartureAlarms = departureAlarms;
                mDepartureAlarmsAdapter = new DepartureAlarmsAdapter(getActivity(),departureAlarms);
                mRecyclerView.setAdapter(mDepartureAlarmsAdapter);
                showTuto();

                mDepartureAlarmsAdapter.setOnItemClickListener(new DepartureAlarmsAdapterClickListener());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            dismissPD();
        }

        @Override
        protected List<DepartureAlarm> doInBackground(Void... params) {
            try
            {
                DepartureAlarmDAO depAlarmDAO = new DepartureAlarmDAO(DatabaseHelper.getInstance(getActivity()));
                List<DepartureAlarm> departureAlarms = depAlarmDAO.getAll();
                Calendar now = DateTools.now();
                for (int i = departureAlarms.size()-1; i >= 0; i--)
                {
                    if (departureAlarms.get(i).getDate().before(now))
                    {
                        if (depAlarmDAO.delete(departureAlarms.get(i)))
                        {
                            departureAlarms.remove(i);
                        }
                    }
                    else {
                        departureAlarms.get(i).getDate().set(Calendar.SECOND,0);
                    }
                }
                return departureAlarms;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void showTuto() {
        if (!isAdded())
        {
            return;
        }

        mTutorialManager.getTutorials().clear();

        if (mDepartureAlarmsAdapter == null || mDepartureAlarmsAdapter.getItemCount() == 0)
        {
            Tutorial howToUseTuto = new Tutorial(getActivity().getString(R.string.menu_departures_alarms), getActivity().getString(R.string.showcase_departure_alarms));
            mTutorialManager.addTutorial(howToUseTuto);

            mTutorialManager.launchTutorials();
        }
        else
        {
            mDepartureAlarmsAdapter.showTuto();
        }

    }

    private class DepartureAlarmsAdapterClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DepartureAlarm departureAlarm = mDepartureAlarmsAdapter.getItem(position);
            if (departureAlarm == null)
            {
                return;
            }


            if (id == R.id.viewIV) {
                Bundle bundle = ThermometerFragment.createBundle(departureAlarm.getDepartureCode());
                ThermometerFragment thermometerFragment = ThermometerFragment.newInstance();
                thermometerFragment.setArguments(bundle);
                changeFragment(thermometerFragment, true);
            }
            else if (id == R.id.deleteIV)
            {
                if (AlarmTools.removeAlarm(getActivity(),departureAlarm,false, departureAlarm.getMinutes()))
                {
                    mDepartureAlarmsAdapter.getDepartureAlarms().remove(position);
                    mDepartureAlarmsAdapter.notifyItemRemoved(position);
                    Snackbar.make(getView(),getString(R.string.alarm_deleted),Snackbar.LENGTH_LONG).show();
                }
            }
            else if (id == R.id.warningIV)
            {
                for (Disruption disruption : departureAlarm.getDisruptions())
                {
                    disruption.setLine(new Line(departureAlarm.getLine()));
                }
                Bundle bundle = DisruptionsDialogFragment.createBundle(departureAlarm.getDisruptions());
                getMainActivity().openDialogFragment(MainActivity.DialogFragmentName.Disruptions,bundle);

                return;
            }
        }
    }
}
