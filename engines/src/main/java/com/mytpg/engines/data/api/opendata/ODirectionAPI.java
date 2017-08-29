package com.mytpg.engines.data.api.opendata;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mytpg.engines.data.abstracts.opendata.OAPI;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.directions.Direction;
import com.mytpg.engines.entities.network.VolleySingleton;
import com.mytpg.engines.entities.opendata.ODirection;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by stalker-mac on 23.10.16.
 */

public class ODirectionAPI extends OAPI<ODirection> {
    public final static String ENDPOINT_BASE = DataSettings.API_OPENDATA_DIRECTIONS_ENDPOINT;
    public final static String ENDPOINT_WITH_PARAMS = ENDPOINT_BASE + "?from=%1$s&to=%2$s&date=%3$s&time=%4$s&isArrivalTime=%5$s&page=%6$s";

    public ODirectionAPI(Context argContext) {
        super(argContext);
    }

    @Override
    protected ODirection getObjectFromJSON(JSONObject argJSONOBject) {
        ODirection direction = new ODirection();
        direction.fromJson(argJSONOBject);

        return direction;
    }

    @Override
    protected List<ODirection> getObjectsFromJson(JSONArray argJSONArray) {
        return null;
    }

    public void search(final Direction argDirection, int argPage, final IAPIListener<ODirection> argDirectionListener)
    {
        String baseEndpoint = ENDPOINT_WITH_PARAMS;

        String from = argDirection.getFrom();
        String to = argDirection.getTo();

        if (argDirection.getFromStop().getId() != -1)
        {
            from = argDirection.getFromStop().getCFF();
        }
        if (argDirection.getToStop().getId() != -1)
        {
            to = argDirection.getToStop().getCFF();
        }

        String endpoint = String.format("%1$s%2$s",getBaseUrl(),
                                                   String.format(baseEndpoint, from,
                                                                               to,
                                                                               DateTools.dateToString(argDirection.getDate(), DateTools.FormatType.SearchDirectionsDate),
                                                                               DateTools.dateToString(argDirection.getDate(), DateTools.FormatType.SearchDirectionsHour),
                                                                               argDirection.isDeparture() ? 0 : 1,
                                                                               argPage).replaceAll(" ", "%20"));
        Log.d("Endpoint", endpoint);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        ODirection direction = getObjectFromJSON(response);

                        if (argDirectionListener != null)
                        {
                            argDirectionListener.onSuccess(direction);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (argDirectionListener != null)
                        {
                            argDirectionListener.onError(error);
                        }

                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }
}
