package com.mytpg.engines.data.oldapi;

import android.util.Log;

import com.mytpg.engines.data.interfaces.IDirectionDAO;
import com.mytpg.engines.entities.network.http.HttpResult;
import com.mytpg.engines.entities.olddirections.Direction;
import com.mytpg.engines.network.http.tools.HttpTools;
import com.mytpg.engines.tools.DateTools;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 01.01.15.
 */
public class DirectionAPIDAO extends APIDAO<Direction> implements IDirectionDAO {
    @Override
    public boolean create(Direction ArgDirection) {
        return false;
    }

    /**
     * @param ArgDirections
     * @return
     */
    @Override
    public boolean create(List<Direction> ArgDirections) {
        return false;
    }

    @Override
    public boolean delete(Direction ArgDirection) {
        return false;
    }

    @Override
    public Direction find(long ArgId, boolean ArgIsComplete) {
        return null;
    }

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Direction findByName(String ArgName) {
        return null;
    }

    @Override
    public List<Direction> getAll() {
        return null;
    }

    @Override
    public Direction search(String ArgDeparture, String ArgArrival, Calendar ArgDate, boolean ArgIsArrival) {
        Direction direction = null;
        try {
            String hour = DateTools.dateToString(ArgDate, DateTools.FormatType.DirectionTime);
            String dateString = DateTools.dateToString(ArgDate, DateTools.FormatType.DirectionDate);

            Log.d("DATE CHOOSE", dateString);
            Log.d("HOUR CHOOSE", hour);
            String isArrivalText = "1";
            if (ArgIsArrival) {
                isArrivalText = "0";
            }

            String url = "http://tpg.hafas.de/hafas/tp/query.exe/fn";
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("REQ0JourneyStopsS0G", ArgDeparture));
            params.add(new BasicNameValuePair("REQ0JourneyStopsZ0G", ArgArrival));
            params.add(new BasicNameValuePair("REQ0JourneyDate", dateString));
            params.add(new BasicNameValuePair("REQ0JourneyTime", hour));
            params.add(new BasicNameValuePair("REQ0HafasSearchForw", isArrivalText));
            params.add(new BasicNameValuePair("REQ0JourneyProduct_prod_list", "1:1111111111111111"));
            params.add(new BasicNameValuePair("REQ0JourneyStopsS0A", "255"));
            params.add(new BasicNameValuePair("REQ0JourneyStopsZ0A", "255"));
            params.add(new BasicNameValuePair("start.x", "90"));
            params.add(new BasicNameValuePair("start.y", "18"));

            HttpResult httpResult = HttpTools.getHttpResultFromUrl(url, params, HttpResult.ResponseType.HTML, HttpTools.HttpMethod.POST);
            if (httpResult != null) {
                String html = httpResult.getResult();
                direction = new Direction();
                direction.html = html;
                direction.fromHtml(html);
                direction.setArrival(ArgIsArrival);

                Log.d("RESULT", html);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            direction = null;
        }

        return direction;
    }

    @Override
    public boolean update(Direction ArgDirection) {
        return false;
    }


}
