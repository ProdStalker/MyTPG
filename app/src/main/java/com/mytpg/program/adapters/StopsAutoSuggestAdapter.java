package com.mytpg.program.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.program.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 21.10.16.
 */

public class StopsAutoSuggestAdapter extends ArrayAdapter<Stop> {
    private LayoutInflater mLayoutInflater;
    private StopFilter mStopFilter = new StopFilter();
    private List<Stop> mStops = new ArrayList<>();

    public StopsAutoSuggestAdapter(Context context, int resource, List<Stop> objects) {
        super(context, resource, objects);
        mStops = new ArrayList<>(objects);

        mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return mStopFilter;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.item_auto_suggest_stop, null);
        }

        Stop stop = getItem(position);

        TextView name = (TextView) view.findViewById(R.id.nameTV);
        name.setText(stop.getName());

        return view;
    }

    private class StopFilter extends Filter
    {
        @Override
        public String convertResultToString(Object resultValue) {
            Stop stop = (Stop)resultValue;
            return stop.getName();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null) {
                ArrayList<Stop> suggestions = new ArrayList<Stop>();
                String textToSearch = constraint.toString().toLowerCase();
                for (Stop stop : mStops) {

                    if (stop.getName().toLowerCase().contains(textToSearch) ||
                        stop.getCode().toLowerCase().contains(textToSearch) ||
                        stop.getCFF().toLowerCase().contains(textToSearch)) {
                        suggestions.add(stop);
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                addAll((ArrayList<Stop>) results.values);
            } else {
                addAll(mStops);
            }
            notifyDataSetChanged();
        }
    }
}
