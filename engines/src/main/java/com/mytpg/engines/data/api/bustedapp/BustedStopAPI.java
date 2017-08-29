package com.mytpg.engines.data.api.bustedapp;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mytpg.engines.data.abstracts.busted.BustedAPI;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.bustedapp.BustedStop;
import com.mytpg.engines.entities.network.VolleySingleton;
import com.mytpg.engines.settings.DataSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public class BustedStopAPI extends BustedAPI<BustedStop> {
    public final static String ENDPOINT_BASE_ALL = DataSettings.BUSTED_API_ALL_STOPS_ENDPOINT;

    public BustedStopAPI(Context argContext)
    {
        super(argContext);

    }

    @Override
    protected BustedStop getObjectFromJSON(JSONObject argJSONOBject) {
        BustedStop bustedStop = new BustedStop();
        bustedStop.fromJson(argJSONOBject);
        return bustedStop;
    }

    @Override
    protected List<BustedStop> getObjectsFromJson(JSONArray argJSONArray) {
        List<BustedStop> bustedStops = new ArrayList<>();
        if (argJSONArray != null)
        {
            for (int i = 0; i < argJSONArray.length(); i++)
            {
                JSONObject bustedStopJson = argJSONArray.optJSONObject(i);
                BustedStop bustedStop = getObjectFromJSON(bustedStopJson);
                if (bustedStop.getId() == -1)
                {
                    bustedStop.setId(i);
                }
                if (!bustedStop.getName().isEmpty()) {
                    bustedStops.add(bustedStop);
                }
            }
        }

        return bustedStops;
    }

    public void  getAll(final IAPIListener<BustedStop> argBustedStopListener) {

        String endpoint = String.format("%1$s%2$s/%3$s",getBaseUrl(),generateUrlKey(),ENDPOINT_BASE_ALL);
        Log.d("Endpoint", endpoint);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                       // String timestamp = response.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
                        //DateTools.setCurrentDate(timestamp);

                        JSONArray bustedStopsJsonArray = response.optJSONArray("dataset");

                        List<BustedStop> bustedStops = getObjectsFromJson(bustedStopsJsonArray);

                        if (argBustedStopListener != null)
                        {
                            argBustedStopListener.onSuccess(bustedStops);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argBustedStopListener != null)
                        {
                            argBustedStopListener.onError(error);
                        }

                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }
}
