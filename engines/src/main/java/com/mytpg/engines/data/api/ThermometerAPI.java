package com.mytpg.engines.data.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mytpg.engines.data.abstracts.APIWithKey;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Thermometer;
import com.mytpg.engines.entities.network.VolleySingleton;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public class ThermometerAPI extends APIWithKey<Thermometer> {
    public final static String ENDPOINT_BASE = DataSettings.API_THERMOMETER;
    public final static String ENDPOINT_BY_CODE = ENDPOINT_BASE + "?departureCode=%1$s";

    public ThermometerAPI(Context argContext)
    {
        super(argContext);
    }

    @Override
    protected Thermometer getObjectFromJSON(JSONObject argJSONOBject) {
        Thermometer thermometer = new Thermometer();
        thermometer.fromJson(argJSONOBject);

        return thermometer;
    }

    @Override
    protected List<Thermometer> getObjectsFromJson(JSONArray argJSONArray) {
        List<Thermometer> thermometers = new ArrayList<>();
        if (argJSONArray != null)
        {
            for (int i = 0; i < argJSONArray.length(); i++)
            {
                JSONObject thermometerJson = argJSONArray.optJSONObject(i);
                Thermometer thermometer = getObjectFromJSON(thermometerJson);
                if (thermometer.getId() == -1)
                {
                    thermometer.setId(i);
                }

                thermometers.add(thermometer);

            }
        }

        return thermometers;
    }

    public void getByCode(int argDepartureCode, final IAPIListener<Thermometer> argThermometerListener)
    {
        String baseEndpoint = ENDPOINT_BY_CODE;

        String endpoint = String.format("%1$s%2$s&%3$s",getBaseUrl(),
            String.format(baseEndpoint, argDepartureCode),
            generateUrlKey());
        Log.d("Endpoint", endpoint);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String timestamp = response.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
                        DateTools.setCurrentDate(timestamp);
                        Thermometer thermometer = getObjectFromJSON(response);

                        if (argThermometerListener != null)
                        {
                            argThermometerListener.onSuccess(thermometer);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argThermometerListener != null)
                        {
                            argThermometerListener.onError(error);
                        }

                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }
}
