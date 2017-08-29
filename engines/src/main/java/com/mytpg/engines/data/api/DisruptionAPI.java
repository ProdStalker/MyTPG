package com.mytpg.engines.data.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mytpg.engines.data.abstracts.APIWithKey;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Disruption;
import com.mytpg.engines.entities.network.VolleySingleton;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public class DisruptionAPI extends APIWithKey<Disruption> {
    public final static String ENDPOINT_ALL = DataSettings.API_DISRUPTIONS_ENDPOINT;

    public DisruptionAPI(Context argContext)
    {
        super(argContext);
    }

    @Override
    protected Disruption getObjectFromJSON(JSONObject argJSONOBject) {
        Disruption disruption = new Disruption();
        disruption.fromJson(argJSONOBject);

        return disruption;
    }

    @Override
    protected List<Disruption> getObjectsFromJson(JSONArray argJSONArray) {
        List<Disruption> disruptions = new ArrayList<>();
        if (argJSONArray != null)
        {
            for (int i = 0; i < argJSONArray.length(); i++)
            {
                JSONObject disruptionJson = argJSONArray.optJSONObject(i);
                Disruption disruption = getObjectFromJSON(disruptionJson);
                if (disruption.getId() == -1)
                {
                    disruption.setId(i);
                }
                if (disruption.getName().length() > 0) {
                    disruptions.add(disruption);
                }
            }
        }

        Collections.sort(disruptions, new Comparator<Disruption>() {

            @Override
            public int compare(Disruption lhs, Disruption rhs) {
                // TODO Auto-generated method stub
                int result = lhs.getDate().compareTo(rhs.getDate()) * -1;

                return result;
            }
        });

        return disruptions;
    }

    public void getAll(final IAPIListener<Disruption> argDisruptionListener)
    {
        String baseEndpoint = ENDPOINT_ALL;

        String endpoint = String.format("%1s%2s?%3s",getBaseUrl(),baseEndpoint,generateUrlKey());
        Log.d("Endpoint", endpoint);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String timestamp = response.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
                        DateTools.setCurrentDate(timestamp);
                        JSONArray disruptionsJsonArray = response.optJSONArray("disruptions");

                        List<Disruption> disruptions = getObjectsFromJson(disruptionsJsonArray);

                        if (argDisruptionListener != null)
                        {
                            argDisruptionListener.onSuccess(disruptions);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argDisruptionListener != null)
                        {
                            argDisruptionListener.onError(error);
                        }

                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }
}
