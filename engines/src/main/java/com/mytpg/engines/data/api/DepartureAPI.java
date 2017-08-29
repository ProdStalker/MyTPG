package com.mytpg.engines.data.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mytpg.engines.R;
import com.mytpg.engines.data.abstracts.APIWithKey;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.network.VolleySingleton;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.engines.tools.UrlTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public class DepartureAPI extends APIWithKey<Departure> {
    public final static String ENDPOINT_BASE = DataSettings.API_NEXT_DEPARTURES_ENDPOINT;
    public final static String ENDPOINT_BASE_ALL = DataSettings.API_ALL_NEXT_DEPARTURES_ENDPOINT;
    public final static String ENDPOINT_ALL_WITH_PARAMS = ENDPOINT_BASE_ALL + "stopCode=%1$s&destinationCode=%2$s&lineCode=%3$s";

    public DepartureAPI(Context argContext)
    {
        super(argContext);
    }

    @Override
    protected Departure getObjectFromJSON(JSONObject argJSONOBject) {
        Departure departure = new Departure();
        departure.fromJson(argJSONOBject);
        return departure;
    }

    @Override
    protected List<Departure> getObjectsFromJson(JSONArray argJSONArray) {
        List<Departure> departures = new ArrayList<>();
        if (argJSONArray != null)
        {
            for (int i = 0; i < argJSONArray.length(); i++)
            {
                JSONObject departureJson = argJSONArray.optJSONObject(i);
                Departure departure = getObjectFromJSON(departureJson);
                if (departure.getId() == -1)
                {
                    departure.setId(i);
                }
                if (departure.getCode() > 0) {
                    departures.add(departure);
                }
            }
        }

        return departures;
    }

    public void getAllByMnemo(String argMnemo, Line[] argConnectionsFilter, int argDepartureCode, IAPIListener<Departure> argDepartureListener) {
        List<Line> connections = new ArrayList<Line>();

        if (argConnectionsFilter != null)
        {
            Collections.addAll(connections, argConnectionsFilter);
        }

        getAllByMnemo(argMnemo, connections, argDepartureCode, argDepartureListener);
    }

    public void getAllByMnemo(String argMnemo, List<Line> argConnectionsFilter, int argDepartureCode, final IAPIListener<Departure> argDepartureListener) {

        if (argMnemo.isEmpty())
        {
            if (argDepartureListener != null)
            {
                VolleyError volleyError = new VolleyError(mContext.getString(R.string.error_empty_mnemo));
                argDepartureListener.onError(volleyError);
            }
        }

        String baseEndpoint = getBaseUrl() + ENDPOINT_BASE + generateUrlKey();
        String endpoint = baseEndpoint + "&stopCode=" + argMnemo;

        if (argDepartureCode != -1)
        {
            endpoint += "&departureCode=" + String.valueOf(argDepartureCode);
        }



        if (!argConnectionsFilter.isEmpty())
        {
            String linesCode = "";
            String destinationsCode = "";

            int i = 0;
            while (i < argConnectionsFilter.size())
            {
                Line connection = argConnectionsFilter.get(i);

                linesCode += connection.getName();
                destinationsCode += UrlTools.format(connection.getArrivalStop().getCode());

                if (i < argConnectionsFilter.size() - 1)
                {
                    linesCode += ",";
                    destinationsCode += ",";
                }

                i++;
            }

            endpoint += "&linesCode=" + linesCode;
            endpoint += "&destinationsCode=" + destinationsCode;
        }

        Log.d("Endpoint", endpoint);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String timestamp = response.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
                        DateTools.setCurrentDate(timestamp);

                        JSONArray departuresJsonArray = response.optJSONArray("departures");

                        List<Departure> departures = getObjectsFromJson(departuresJsonArray);

                        if (argDepartureListener != null)
                        {
                            argDepartureListener.onSuccess(departures);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argDepartureListener != null)
                        {
                            argDepartureListener.onError(error);
                        }
                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);

    }

    public void  getDayDepartures(String argLine, String argMnemo, String argDestination, final IAPIListener<Departure> argDepartureListener) {

        if (argLine.isEmpty() || argMnemo.isEmpty() || argDestination.isEmpty())
        {
            if (argDepartureListener != null)
            {
                argDepartureListener.onError(new VolleyError(mContext.getString(R.string.error_bad_parameters)));
            }
        }

        String endpoint = String.format("%1$s%2$s&%3$s",getBaseUrl(),
                                                        String.format(ENDPOINT_ALL_WITH_PARAMS,
                                                                      argMnemo,
                                                                      UrlTools.format(argDestination),
                                                                      argLine),
                                                        generateUrlKey());
        Log.d("Endpoint", endpoint);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String timestamp = response.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
                        DateTools.setCurrentDate(timestamp);

                        JSONArray departuresJsonArray = response.optJSONArray("departures");
                        JSONObject stopJSONObject = response.optJSONObject("stop");

                        Stop stop = new Stop();
                        stop.fromJson(stopJSONObject);

                        List<Departure> departures = getObjectsFromJson(departuresJsonArray);

                        for (Departure dep : departures)
                        {
                            dep.setStop(new Stop(stop));
                        }

                        if (argDepartureListener != null)
                        {
                            argDepartureListener.onSuccess(departures);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argDepartureListener != null)
                        {
                            argDepartureListener.onError(error);
                        }

                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }
}
