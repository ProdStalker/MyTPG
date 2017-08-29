package com.mytpg.engines.data.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mytpg.engines.data.abstracts.APIWithKey;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.Line;
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

public class LineAPI extends APIWithKey<Line> {
    public final static String ENDPOINT_ALL = DataSettings.API_LINES_ENDPOINT;

    public LineAPI(Context argContext)
    {
        super(argContext);
    }

    @Override
    protected Line getObjectFromJSON(JSONObject argJSONOBject) {
        Line line = new Line();
        line.fromJson(argJSONOBject);

        return line;
    }

    @Override
    protected List<Line> getObjectsFromJson(JSONArray argJSONArray) {
        List<Line> lines = new ArrayList<>();
        if (argJSONArray != null)
        {
            for (int i = 0; i < argJSONArray.length(); i++)
            {
                JSONObject lineJson = argJSONArray.optJSONObject(i);
                Line line = getObjectFromJSON(lineJson);
                if (line.getId() == -1)
                {
                    line.setId(i);
                }
                if (line.getName().length() > 0) {
                    lines.add(line);
                }
            }
        }

        return lines;
    }

    public void getAll(final IAPIListener<Line> argLineListener)
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
                        JSONArray linesJsonArray = response.optJSONArray("colors");

                        List<Line> lines = getObjectsFromJson(linesJsonArray);

                        if (argLineListener != null)
                        {
                            argLineListener.onSuccess(lines);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argLineListener != null)
                        {
                            argLineListener.onError(error);
                        }

                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest, true);
    }
}
