package com.mytpg.engines.data.api.bustedapp;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mytpg.engines.data.abstracts.busted.BustedAPI;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.bustedapp.Alert;
import com.mytpg.engines.entities.network.VolleySingleton;
import com.mytpg.engines.settings.DataSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public class AlertAPI extends BustedAPI<Alert> {
    public final static String ENDPOINT_BASE_ALL = DataSettings.BUSTED_API_ALL_ALERTS_ENDPOINT;

    public AlertAPI(Context argContext)
    {
        super(argContext);

    }

    @Override
    protected Alert getObjectFromJSON(JSONObject argJSONOBject) {
        Alert alert = new Alert();
        alert.fromJson(argJSONOBject);
        return alert;
    }

    @Override
    protected List<Alert> getObjectsFromJson(JSONArray argJSONArray) {
        List<Alert> alerts = new ArrayList<>();
        if (argJSONArray != null)
        {
            for (int i = 0; i < argJSONArray.length(); i++)
            {
                JSONObject alertJson = argJSONArray.optJSONObject(i);
                Alert alert = getObjectFromJSON(alertJson);
                if (alert.getId() == -1)
                {
                    alert.setId(i);
                }
                if (!alert.getName().isEmpty()) {
                    alerts.add(alert);
                }
            }
        }

        return alerts;
    }

    public void  getAll(final IAPIListener<Alert> argAlertListener) {

        String endpoint = String.format("%1$s%2$s/%3$s",getBaseUrl(),generateUrlKey(),ENDPOINT_BASE_ALL);
        Log.d("Endpoint", endpoint);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                       // String timestamp = response.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
                        //DateTools.setCurrentDate(timestamp);

                        JSONArray alertsJsonArray = response.optJSONArray("alerts");

                        List<Alert> alerts = getObjectsFromJson(alertsJsonArray);

                        if (argAlertListener != null)
                        {
                            argAlertListener.onSuccess(alerts);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argAlertListener != null)
                        {
                            argAlertListener.onError(error);
                        }

                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }
}
