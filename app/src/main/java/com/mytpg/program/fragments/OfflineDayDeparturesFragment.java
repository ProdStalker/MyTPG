package com.mytpg.program.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.mytpg.engines.data.dao.DepartureDAO;
import com.mytpg.engines.data.factories.AbstractDAOFactory;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.program.R;
import com.mytpg.program.adapters.OfflineDayDeparturesAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stalker-mac on 18.11.14.
 */
public class OfflineDayDeparturesFragment extends BaseFragment{
    private static final String ARG_SAVED_DEPARTURES = "savedDepartures";

    private ProgressDialog m_pd = null;
    private RecyclerView mDepartureRV = null;
    private LinearLayoutManager mLayoutManager;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();
    private ActionMode mActionMode = null;

    private OfflineDayDeparturesAdapter mOffDayDeparturesAdapter = null;


    public static OfflineDayDeparturesFragment newInstance() {
        OfflineDayDeparturesFragment oddf = new OfflineDayDeparturesFragment();

        return oddf;
    }

    /**
     *
     */
    public OfflineDayDeparturesFragment() {
        // TODO Auto-generated constructor stub
    }

    private void delete() {
        if (mDepartureRV == null || mOffDayDeparturesAdapter == null)
        {
            return;
        }

        new Runnable() {

            @Override
            public void run() {
                List<Integer> positionsChecked = mOffDayDeparturesAdapter.getSelectedItems();

               /* int i = 0;
                while (i < mOffDayDeparturesAdapter.getItemCount())
                {

                    if (mOffDayDeparturesAdapter.isItemSelected(i))
                    {
                        positionsChecked.add(Integer.valueOf(i));
                    }
                    i++;
                }*/

                if (positionsChecked.size() == 0)
                {
                    return;
                }
                else if (positionsChecked.size() == mOffDayDeparturesAdapter.getItemCount())
                {
                    deleteAll();
                    return;
                }

                int numberDeleted = 0;
                getApp().setFactory(AbstractDAOFactory.FactoryType.DB);
                DepartureDAO depDAO = (DepartureDAO) getApp().getAbsDAOFact().getAbsDepartureDAO();
                int i = 0;
                while (i < positionsChecked.size())
                {
                    final int Position = positionsChecked.get(i).intValue();

                    Departure dep = mOffDayDeparturesAdapter.getDepartures().get(Position);
                    if (dep != null)
                    {
                        if (depDAO.deleteByLineAndStopAndDestination(dep.getLine().getId(), dep.getStop().getId(), dep.getLine().getArrivalStop().getId()))
                        {
                           // mOffDayDeparturesAdapter.setItemSelected(Position,false);
                           // mDepartureRV.setItemChecked(Position, false);
                            mOffDayDeparturesAdapter.remove(Position);
                            numberDeleted++;

                            int j = i+1;
                            while (j < positionsChecked.size())
                            {
                                positionsChecked.set(j, Integer.valueOf(positionsChecked.get(j).intValue()-1));
                                j++;
                            }
                        }
                    }

                    i++;
                }

                String numberDeletedString;
                if (numberDeleted > 0)
                {
                    numberDeletedString = getResources().getQuantityString(R.plurals.number_departures_deleted, numberDeleted, numberDeleted, positionsChecked.size());
                }
                else
                {
                    numberDeletedString = getString(R.string.no_departures_deleted);
                }

                Toast.makeText(getActivity(), numberDeletedString, Toast.LENGTH_LONG).show();

                getApp().setFactory(AbstractDAOFactory.FactoryType.API);

                mOffDayDeparturesAdapter.clearSelections();
                final Handler handler = new Handler();
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {

                    @Override
                    public void run() {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                try
                                {
                                    mOffDayDeparturesAdapter.notifyDataSetChanged();
                                }
                                catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                };

                timer.schedule(timerTask, 800);
            }
        }.run();

    }

    private void deleteAll() {
        new Runnable() {

            @Override
            public void run() {
                getApp().setFactory(AbstractDAOFactory.FactoryType.DB);

                boolean success = false;

                DepartureDAO depDAO = (DepartureDAO)getApp().getAbsDAOFact().getAbsDepartureDAO();

                if (depDAO != null)
                {
                    success = depDAO.deleteAll();
                }

                String text = getString(R.string.all_departures_not_deleted);

                if (success)
                {
                    text = getString(R.string.all_departures_deleted);
                    if (mOffDayDeparturesAdapter != null)
                    {
                        mOffDayDeparturesAdapter.clear();
                        showTuto();
                    }
                    else
                    {
                        loadData();
                    }
                }

                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();

                getApp().setFactory(AbstractDAOFactory.FactoryType.API);
            }
        }.run();
    }

    protected void initializeComponents(View ArgRootView) {
        if (ArgRootView == null)
        {
            return;
        }

        mDepartureRV = (RecyclerView)ArgRootView.findViewById(R.id.mainRV);
        mDepartureRV.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());// new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mDepartureRV.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mOffDayDeparturesAdapter = new OfflineDayDeparturesAdapter(getActivity(),null);
        mDepartureRV.setAdapter(mOffDayDeparturesAdapter);

      //  mDepartureRV.setChoiceMode(AbsRecyclerView.CHOICE_MODE_MULTIPLE);


    }

    @Override
    public void onActivityCreated(Bundle ArgBundle) {
        ArrayList<Departure> departures = null;
        if (ArgBundle != null)
        {
            departures = ArgBundle.getParcelableArrayList(ARG_SAVED_DEPARTURES);
        }

        if (departures == null || departures.isEmpty())
        {
            loadData();
        }
        else
        {
            mOffDayDeparturesAdapter.setDepartures(departures);
            mDepartureRV.setAdapter(mOffDayDeparturesAdapter);

            mOffDayDeparturesAdapter.setOnItemClickListener(new OfflineDayDeparturesAdapterClickListener());

        }
        super.onActivityCreated(ArgBundle);
    }
    
    /* (non-Javadoc)
     * @see com.otpg.program.core.BaseFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_offline_day_departures, container, false);

        setTitle(getString(R.string.menu_offline_day_departures));

        initializeComponents(rootView);



        return rootView;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.action_delete :
                delete();
                return true;

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

        if (mOffDayDeparturesAdapter == null)
        {
            return;
        }
        ArrayList<Departure> departures = mOffDayDeparturesAdapter.getDeparturesToSave();
        if (departures == null || departures.size() == 0)
        {
            return;
        }
        outState.putParcelableArrayList(ARG_SAVED_DEPARTURES, departures);
    }

    protected void loadData() {
        new OfflineDayDeparturesAsyncTask().execute();
    }

    private class OfflineDayDeparturesAsyncTask extends AsyncTask<Void, String, List<Departure>> {

        @Override
        protected void onPreExecute()
        {
            showPD();
        }

        @Override
        protected List<Departure> doInBackground(Void... params) {
            List<Departure> departures = null;
            try
            {
                if (isAdded())
                {
                    DepartureDAO depDAO = new DepartureDAO(DatabaseHelper.getInstance(getActivity()));
                    departures = depDAO.getAllGroupedByLineAndStop();
                }
            }
            catch (NullPointerException npe)
            {
                npe.printStackTrace();
                departures = null;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                departures = null;
            }

            return departures;

        }

        @Override
        protected void onPostExecute(List<Departure> ArgDepartures)
        {
            try
            {
                if (mOffDayDeparturesAdapter != null)
                {
                    mOffDayDeparturesAdapter.clear();
                }


                if (isAdded())
                {

                    mOffDayDeparturesAdapter = new OfflineDayDeparturesAdapter(getActivity(), ArgDepartures);

                    mDepartureRV.setAdapter(mOffDayDeparturesAdapter);

                    mOffDayDeparturesAdapter.setOnItemClickListener(new OfflineDayDeparturesAdapterClickListener());

                    mOffDayDeparturesAdapter.setOnItemLongClickListener(new OfflineDayDeparturesAdapterClickListener());

                }


                dismissPD();

                if (isAdded())
                {
                    if (ArgDepartures == null || ArgDepartures.size() == 0)
                    {
                        Toast.makeText(getActivity(), getString(R.string.none_departures), Toast.LENGTH_LONG).show();
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
            showTuto();
        }

    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_offline_day_departures);
    }

    private void showTuto() {
        if (!isAdded())
        {
            return;
        }

        mTutorialManager.getTutorials().clear();

        if (mOffDayDeparturesAdapter == null || mOffDayDeparturesAdapter.getItemCount() == 0)
        {
            Tutorial howToUseTuto = new Tutorial(getActivity().getString(R.string.menu_offline_day_departures), getActivity().getString(R.string.showcase_offline_departures));
            mTutorialManager.addTutorial(howToUseTuto);
        }
        else
        {
            App app = (App)getContext().getApplicationContext();
            if (app != null && app.isFirstOfflineDepartures()) {
                Tutorial howDeleteTuto = new Tutorial(getActivity().getString(R.string.action_delete), getActivity().getString(R.string.showcase_delete_offline_departures));
                mTutorialManager.addTutorial(howDeleteTuto);

                app.setFirstOfflineDepartures(false);
            }
        }

        mTutorialManager.launchTutorials();
    }

    @Override
    public void search(String ArgTextToSearch){

    }

    public OfflineDayDeparturesFragment getInstance()
    {
        return this;
    }

    private class OfflineDayDeparturesAdapterClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (mOffDayDeparturesAdapter == null)
            {
                return;
            }
            Departure dep = mOffDayDeparturesAdapter.getItem(position);
            if (dep == null)
            {
                return;
            }

            Bundle bundle = DayDeparturesFragment.createBundle(dep.getStop().getMnemo().getName(),
                    dep.getLine().getId(),
                    dep.getLine().getArrivalStop().getCode(),
                    dep.getLine().getArrivalStop().getName(),
                    true);
            DayDeparturesFragment ddf = DayDeparturesFragment.newInstance();
            ddf.setArguments(bundle);

            changeFragment(ddf,true);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (mActionMode == null) {
                mActionMode = getMainActivity().startSupportActionMode(mActionModeCallback);
            }

            toggleSelection(position);

            return true;
        }
    }

    private void toggleSelection(int argPosition) {
        mOffDayDeparturesAdapter.toggleSelection(argPosition);
        int numberSelected = mOffDayDeparturesAdapter.getSelectedItemCount();
        String title = getResources().getQuantityString(R.plurals.selected_count, numberSelected, numberSelected);
        mActionMode.setTitle(title);
    }

    private class ActionModeCallback implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.delete, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    delete();
                    mActionMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mOffDayDeparturesAdapter.clearSelections();
        }

    }
}

