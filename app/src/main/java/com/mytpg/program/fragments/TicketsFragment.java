package com.mytpg.program.fragments;


import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.mytpg.engines.data.dao.TicketDAO;
import com.mytpg.engines.entities.Ticket;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.RequestCodeSettings;
import com.mytpg.engines.settings.TPGSettings;
import com.mytpg.engines.settings.TicketSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.Manifest;
import com.mytpg.program.R;
import com.mytpg.program.adapters.TicketsAdapter;
import com.mytpg.program.core.App;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class TicketsFragment extends BaseFragment {
    private static final String ARG_SAVED_TICKETS = "savedTickets";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TicketsAdapter mTicketsAdapter;

    private List<Ticket> mTickets;
    private int mLastPreparePosition = -1;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mTicketsAdapter == null)
        {
            return;
        }

        ArrayList<Ticket> tickets = mTicketsAdapter.getTicketsToSave();

        if (!tickets.isEmpty()) {
            outState.putParcelableArrayList(ARG_SAVED_TICKETS, tickets);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ArrayList<Ticket> tickets = null;
        if (savedInstanceState != null)
        {
            tickets = savedInstanceState.getParcelableArrayList(ARG_SAVED_TICKETS);
        }

        if (tickets == null || tickets.isEmpty())
        {
            loadData();
        }
        else
        {
            if (mTicketsAdapter == null)
            {
                mTicketsAdapter = new TicketsAdapter(getActivity(), tickets);
            }
            else {
                mTicketsAdapter.setTickets(tickets);
            }
            mRecyclerView.setAdapter(mTicketsAdapter);

            //mTicketsAdapter.setOnItemClickListener(new TicketsAdapterClickListener());

        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public String getTitle() {
        return getString(R.string.menu_tickets);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_disruptions, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.mainRV);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        //loadData();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tickets, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_zone_10 :
                if (getMainActivity() != null)
                {
                    getMainActivity().openUrl(TPGSettings.PDF_ZONE_10_URL);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean needToBeFullViewport() {
        return false;
    }

    private void loadData()
    {
        loadTickets();
    }

    private void loadTickets()
    {
        new TicketsAsyncTask().execute();
    }

    private List<Ticket> createTickets()
    {
        List<Ticket> tickets = new ArrayList<>();
        try {

            int i = 0;
            while (i < 2) {
                tickets.add(new Ticket());
                tickets.get(i).setName(getString(R.string.all_geneva));
                tickets.get(i).setDescription(getString(R.string.all_geneva_description));
                if (i % 2 == 0) {
                    tickets.get(i).setFull(true);
                } else {
                    tickets.get(i).setFull(false);
                }
                i++;
            }

            i = 2;
            while (i < 4) {
                tickets.add(new Ticket());
                tickets.get(i).setName(getString(R.string.day_ticket));
                tickets.get(i).setDescription(getString(R.string.day_ticket_description));
                if (i % 2 == 0) {
                    tickets.get(i).setFull(true);
                } else {
                    tickets.get(i).setFull(false);
                }
                i++;
            }

            i = 4;
            while (i < 6) {
                tickets.add(new Ticket());
                tickets.get(i).setName(getString(R.string.day_9h_ticket));
                tickets.get(i).setDescription(getString(R.string.day_9h_ticket_description));
                if (i % 2 == 0) {
                    tickets.get(i).setFull(true);
                } else {
                    tickets.get(i).setFull(false);
                }
                i++;
            }


            //tickets.get(0).setFull(true);
            tickets.get(0).setPrice(3.0);
            tickets.get(0).setCode(TicketSettings.ALL_GENEVA_CODE);
            //tickets.get(1).setFull(false);
            tickets.get(1).setPrice(2.0);
            tickets.get(1).setCode(TicketSettings.ALL_GENEVA_CODE_NOT_FULL);

            tickets.get(2).setPrice(10.0);
            tickets.get(2).setCode(TicketSettings.ALL_DAY_CODE);
            tickets.get(3).setPrice(7.3);
            tickets.get(3).setCode(TicketSettings.ALL_DAY_CODE_NOT_FULL);

            tickets.get(4).setPrice(8.0);
            tickets.get(4).setCode(TicketSettings.ALL_DAY_9H_CODE);
            tickets.get(5).setPrice(5.6);
            tickets.get(5).setCode(TicketSettings.ALL_DAY_9H_CODE_NOT_FULL);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            tickets.clear();
        }

        return tickets;
    }

    @Override
    public void search(String argSearchText) {

    }

    private class TicketsAsyncTask extends AsyncTask<Void,Void,List<Ticket>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showPD();
        }

        @Override
        protected void onPostExecute(List<Ticket> tickets) {
            try {
                super.onPostExecute(tickets);

                if (tickets == null) {
                    tickets = new ArrayList<>();
                }
                mTicketsAdapter = new TicketsAdapter(getActivity(), tickets);
                mRecyclerView.setAdapter(mTicketsAdapter);

                mTicketsAdapter.setOnItemClickListener(new TicketClickListener());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            dismissPD();
        }

        @Override
        protected List<Ticket> doInBackground(Void... params) {
            try {
                List<Ticket> tickets = createTickets();

                TicketDAO ticketDAO = new TicketDAO(DatabaseHelper.getInstance(getActivity()));
                List<Ticket> oldTickets = ticketDAO.getAll();
                ticketDAO.deleteAll();

                Calendar now = DateTools.now();
                for (Ticket ticket : oldTickets)
                {
                    for (int i = 0; i < tickets.size(); i++)
                    {
                        if (ticket.getCode().equalsIgnoreCase(tickets.get(i).getCode()))
                        {
                            Calendar date = DateTools.now();
                            if (ticket.getDate().before(now))
                            {
                                date.setTimeInMillis(0);
                            }
                            else
                            {
                                date = ticket.getDate();
                            }
                            tickets.get(i).setDate(date);
                        }
                    }
                }

                if (ticketDAO.create(tickets))
                {
                    return tickets;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case RequestCodeSettings.REQ_PERMISSION_RECEIVE_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    try {
                        prepareSMS(mLastPreparePosition);
                    }
                    catch (SecurityException se)
                    {
                        se.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(getActivity(),getString(R.string.unable_loading),Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    protected boolean askReceiveSMS() {
        App app = getApp();
        if (app == null)
        {
            return false;
        }

        if (!app.getSharedPreferences().getBoolean(AppSettings.PREF_ALARM_TICKET_ACTIVE, true))
        {
            return false;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.RECEIVE_SMS))
            {
                AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.permlab_receiveSMS))
                        .setMessage(getString(R.string.permdesc_receiveSMS))
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECEIVE_SMS},
                                        RequestCodeSettings.REQ_PERMISSION_RECEIVE_SMS);
                            }
                        })
                        .setCancelable(false)
                        .create();

                ad.show();
            }
            else
            {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECEIVE_SMS},
                        RequestCodeSettings.REQ_PERMISSION_RECEIVE_SMS);
            }

        }
        else
        {
            return true;
        }

        return false;
    }

    private class TicketClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            //Ticket ticket = mTicketsAdapter.getItem(position);

            if (id == R.id.prepareActionIV) {
                prepareSMS(position);

                return;
            }
        }
    }

    private void prepareSMS(int argPosition) {
        mLastPreparePosition = argPosition;
        if (askReceiveSMS()) {
            Ticket ticket = mTicketsAdapter.getItem(argPosition);
            if (ticket == null) {
                return;
            }

            if (getMainActivity() != null) {
                getMainActivity().openSmsDefault(TicketSettings.TPG_SMS_TICKETS_NUMBER, ticket.getCode());
                mLastPreparePosition = -1;
            }
        }
    }

}
