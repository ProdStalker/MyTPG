package com.mytpg.engines.data.api;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mytpg.engines.data.abstracts.APIWithKey;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.network.VolleySingleton;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public class StopAPI extends APIWithKey<Stop> {
    public final static String ENDPOINT_ALL = DataSettings.API_STOPS_ENDPOINT;
    public final static String ENDPOINT_PHYSICAL_ALL = DataSettings.API_PHYSICAL_STOPS_ENDPOINT;
    public final static String ENDPOINT_PROXIMITY = ENDPOINT_ALL + "?latitude=%1$s&longitude=%2$s";

    public StopAPI(Context argContext)
    {
        super(argContext);
    }

    @Override
    protected Stop getObjectFromJSON(JSONObject argJSONOBject) {
        Stop stop = new Stop();
        stop.fromJson(argJSONOBject);

        return stop;
    }

    @Override
    protected List<Stop> getObjectsFromJson(JSONArray argJSONArray) {
        List<Stop> stops = new ArrayList<>();
        if (argJSONArray != null)
        {
            for (int i = 0; i < argJSONArray.length(); i++)
            {
                JSONObject stopJson = argJSONArray.optJSONObject(i);
                Stop stop = getObjectFromJSON(stopJson);
                if (stop.getId() == -1)
                {
                    stop.setId(i);
                }
                if (stop.getName().length() > 0) {
                    for (PhysicalStop physicalStop : stop.getPhysicalStops())
                    {
                        physicalStop.setStopId(stop.getId());
                    }
                                    /*for (int j = 0; j < stop.getConnections().size(); j++)
                                    {
                                        for (Line line : mLines) {
                                            if (stop.getConnections().get(j).getName().equalsIgnoreCase(line.getName())) {
                                                stop.getConnections().get(j).setColor(line.getColor());
                                            }
                                        }
                                    }*/
                    stops.add(stop);
                }
            }
        }

        return stops;
    }

    public void getAll(boolean argIsPhysical, final IAPIListener<Stop> argStopListener)
    {
        String baseEndpoint = ENDPOINT_ALL;
        if (argIsPhysical)
        {
            baseEndpoint = ENDPOINT_PHYSICAL_ALL;
        }

        String endpoint = String.format("%1$s%2$s?%3$s",getBaseUrl(),baseEndpoint,generateUrlKey());
        Log.d("Endpoint", endpoint);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String timestamp = response.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
                        DateTools.setCurrentDate(timestamp);
                        JSONArray stopsJsonArray = response.optJSONArray("stops");

                        List<Stop> stops = getObjectsFromJson(stopsJsonArray);

                        Stop stop = new Stop();
                        stop.setName("Bus Scolaire");
                        stop.setCode("Bus scolaire");
                        stop.setVisible(false);
                        stops.add(stop);

                        if (argStopListener != null)
                        {
                            argStopListener.onSuccess(stops);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argStopListener != null)
                        {
                            argStopListener.onError(error);
                        }

                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }

    public void getAllProximity(Location argLoc, final IAPIListener<Stop> argStopListener){
        String baseEndpoint = ENDPOINT_PROXIMITY;
        String endpoint = String.format("%1$s%2$s&%3$s",getBaseUrl(),
                                                    String.format(baseEndpoint, argLoc.getLatitude(),argLoc.getLongitude()),
                                                    generateUrlKey());
        Log.d("Endpoint", endpoint);
        /*String url = DataSettings.API_STOPS_ENDPOINT + "?" +
                "latitude="+String.valueOf(mLoc.getLatitude())+"&"+
                "longitude="+String.valueOf(mLoc.getLongitude()) + "&" +
                DataSettings.API_URL_KEY;
        Log.d("URL", url);*/

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String timestamp = response.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
                        DateTools.setCurrentDate(timestamp);
                        JSONArray stopsJsonArray = response.optJSONArray("stops");
                        List<Stop> stops = getObjectsFromJson(stopsJsonArray);
                        if (argStopListener != null)
                        {
                            argStopListener.onSuccess(stops);
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argStopListener != null)
                        {
                            argStopListener.onError(error);
                        }
                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }
}
