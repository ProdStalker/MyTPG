package com.mytpg.program.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.opendata.OCheckPoint;
import com.mytpg.engines.entities.opendata.OSection;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.SizeTools;
import com.mytpg.program.R;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 25.10.16.
 */

public class SectionDetails extends BaseFragment {
    private final static String ARG_SAVED_SECTION = "savedSection";
    public final static String ARG_SECTION = "section";

    private TableLayout mMainTLay = null;

    private OSection mSection = null;
    private StopDAO mStopDAO = null;
    private List<Stop> mStopsAlreadyFound = new ArrayList<>();

    public static Bundle createBundle(OSection argSection)
    {
        if (argSection == null)
        {
            return null;
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_SECTION, argSection);

        return bundle;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_SAVED_SECTION, mSection);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mStopDAO = new StopDAO(DatabaseHelper.getInstance(getActivity()));

        if (savedInstanceState != null)
        {
            mSection = savedInstanceState.getParcelable(ARG_SAVED_SECTION);
        }

        loadData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_details, container, false);

        mMainTLay = (TableLayout)rootView.findViewById(R.id.mainTLay);

        Bundle bundle = getArguments();
        if (bundle == null)
        {
            getMainActivity().onBackPressed();
            return null;
        }

        mSection = bundle.getParcelable(ARG_SECTION);
        if (mSection == null)
        {
            getMainActivity().onBackPressed();
            return null;
        }



        return rootView;
    }

    private Stop getStopForCFFName(String argCFFName)
    {
        for (Stop cStop : mStopsAlreadyFound)
        {
            if (cStop.getCFF().equalsIgnoreCase(argCFFName))
            {
                return cStop;
            }
        }

        Stop stop = mStopDAO.findByCFFName(argCFFName,false);
        if (stop != null) {
            mStopsAlreadyFound.add(stop);
        }

        return stop;
    }

    private void loadData()
    {
        if (mSection == null || mSection.getJourney().getPassList().isEmpty())
        {
            return;
        }

        loadPassList();
    }

    private void loadPassList() {

        int color = getResources().getColor(R.color.colorPrimaryDark);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            color = getActivity().getColor(R.color.colorPrimaryDark);
        }

        int index = 0;
        for (OCheckPoint pass : mSection.getJourney().getPassList())
        {
            String platform = pass.getPlatform();
            if (platform.isEmpty() && (index == 0 || index == mSection.getJourney().getPassList().size()-1))
            {
                if (index == 0)
                {
                    platform = mSection.getDeparture().getPlatform();
                }
                else
                {
                    platform = mSection.getArrival().getPlatform();
                }
            }
            TableRow tableRow = new TableRow(getActivity());
            tableRow.setWeightSum(6);
            tableRow.setBackgroundColor(color);
            tableRow.setPadding(0,0,0,SizeTools.dpToPx(getActivity(),2));

            TextView stopNameTV = new TextView(getActivity());
            stopNameTV.setBackgroundColor(Color.WHITE);
            stopNameTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 2f));
            //((TableRow.LayoutParams)stopNameTV.getLayoutParams()).setMargins(SizeTools.dpToPx(getActivity(),8),0,0,0);
            stopNameTV.setPadding(SizeTools.dpToPx(getActivity(),8),0,0,0);
            TextView arrTV = new TextView(getActivity());
            arrTV.setBackgroundColor(Color.WHITE);
            arrTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            TextView depTV = new TextView(getActivity());
            depTV.setBackgroundColor(Color.WHITE);
            depTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            TextView platformTV = new TextView(getActivity());
            platformTV.setBackgroundColor(Color.WHITE);
            platformTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            TextView busyTV = new TextView(getActivity());
            busyTV.setBackgroundColor(Color.WHITE);
            busyTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

            Stop stop = getStopForCFFName(pass.getStation().getName());
            stopNameTV.setText(stop != null ? stop.getName() : pass.getStation().getName());
            arrTV.setText(pass.getArrivalDate().getTimeInMillis() == 0 ? "" : DateTools.dateToString(pass.getArrivalDate(), DateTools.FormatType.OnlyHourWithoutSeconds));
            depTV.setText(pass.getDepartureDate().getTimeInMillis() == 0 ? "" : DateTools.dateToString(pass.getDepartureDate(), DateTools.FormatType.OnlyHourWithoutSeconds));
            platformTV.setText(platform);
            busyTV.setText("");

            tableRow.addView(stopNameTV);
            tableRow.addView(arrTV);
            tableRow.addView(depTV);
            tableRow.addView(platformTV);
            tableRow.addView(busyTV);

            mMainTLay.addView(tableRow);

            index++;
        }
    }

    @Override
    public void search(String argSearchText) {

    }
}
