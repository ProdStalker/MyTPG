/**
 * 
 */
package com.mytpg.engines.data.oldapi;

import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.mytpg.engines.data.interfaces.IStopDAO;
import com.mytpg.engines.entities.network.http.HttpResult;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.network.http.tools.HttpTools;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * @author stalker-mac
 *
 */
public class StopAPIDAO extends APIDAO<Stop> implements IStopDAO {

	@Override
	public boolean addFavorite(Stop ArgStop, boolean argIsDetailled) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public long countFavorites() {
        return 0;
    }

    @Override
	public boolean create(Stop ArgStop) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean create(List<Stop> ArgStops) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(Stop ArgStop) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Stop find(long ArgId, boolean ArgIsComplete) {
		return null;
	}

	@Override
	public Stop find(String ArgMnemo) {
		return null;
	}

	@Override
	public Stop findByCode(String ArgCode) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
	public Stop findByName(String ArgName) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public Stop fromCursor(Cursor ArgCursor, boolean ArgIsComplete, boolean ArgWithConnections) {
        return null;
    }

	@Override
	public List<Stop> getAll() {
		return getAll(true,true);
	}
	
	@Override
	public List<Stop> getAll(boolean ArgIsPhysical) {
		return getAll(ArgIsPhysical,true);
	}

	@Override
	public List<Stop> getAll(final boolean ArgIsPhysical, final boolean ArgIsComplete) {
		return getAll(ArgIsPhysical, ArgIsComplete, true);
	}

    @Override
    public List<Stop> getAll(boolean ArgIsPhysical, boolean ArgIsComplete, boolean ArgWithConnections) {
        List<Stop> stops = new ArrayList<Stop>();

        String url = DataSettings.API_BASE_URL + "Get";

        if (ArgIsPhysical)
        {
            url += "Physical";
        }

        url += "Stops";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("key", DataSettings.API_KEY));

        HttpResult httpResult = HttpTools.getHttpResultFromUrl(url, params, HttpResult.ResponseType.JSON, HttpTools.HttpMethod.GET);
        if (httpResult == null)
        {
            return stops;
        }

        try {

            JSONObject jsonObj = new JSONObject(httpResult.getResult());
            String timestamp = jsonObj.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
            DateTools.setCurrentDate(timestamp);

            JSONArray stopsJsonArray = jsonObj.getJSONArray("stops");

            int i = 0;
            while (i < stopsJsonArray.length())
            {
                Stop stop = new Stop();
                stop.fromJson(stopsJsonArray.getJSONObject(i));
                if (!ArgWithConnections)
                {
                    for (int j = 0; j < stop.getPhysicalStops().size();j++)
                    {
                        stop.getPhysicalStops().get(j).getConnections().clear();
                    }
                }
                stops.add(stop);
                i++;
            }

            Stop stop = new Stop();
            stop.setName("Bus Scolaire");
            stop.setCode("Bus scolaire");
            stop.setVisible(false);
            stops.add(stop);

            Stop stop2 = new Stop();
            stop2.setName("Hopitaux");
            stop2.setCode("Hopitaux");
            stop2.setVisible(false);
            stops.add(stop2);

            Stop stop3 = new Stop();
            stop3.setName("Divonne-gare");
            stop3.setCode("Divone-gare");
            stop3.setVisible(false);
            stops.add(stop3);

            Stop stop4 = new Stop();
            stop4.setName("Gland-gare");
            stop4.setCode("Gland-gare");
            stop4.setVisible(false);
            stops.add(stop4);

            Stop stop5 = new Stop();
            stop5.setName("Ville-la-Grand");
            stop5.setCode("Ville-la-Grand");
            stop5.setVisible(false);
            stops.add(stop5);

            Stop stop6 = new Stop();
            stop6.setName("Gex-le turet");
            stop6.setCode("Gex-le-turet");
            stop6.setVisible(false);
            stops.add(stop6);


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e("StopAPIDAO Line 90", e.getLocalizedMessage());
            stops = new ArrayList<Stop>();
        }
        catch (NullPointerException npe)
        {
            npe.printStackTrace();
        }

        return stops;
    }

    @Override
    public List<Stop> getAllByName(String ArgName, boolean ArgIsComplete) {
        return new ArrayList<Stop>();
    }

    @Override
	public List<Stop> getAllFavorites(boolean ArgIsPhysical, final boolean ArgIsComplete) {
		return new ArrayList<Stop>();
	}

	@Override
	public List<Stop> getByIds(List<Long> ArgStopIds) {
		return getByIds(ArgStopIds,true);
	}

    @Override
    public List<Stop> getByIds(List<Long> ArgStopIds, boolean ArgIsComplete) {
        return null;
    }

    @Override
    public List<Stop> getByLocation(Location ArgLoc, int ArgNumber) {
        return null;
    }

    @Override
    public Stop getLastFavorite() {
        return null;
    }

    @Override
    public int removeAllFavorites(List<Stop> ArgStops) {
        return 0;
    }

    @Override
	public boolean removeFavorite(Stop ArgStop) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Stop search(String ArgTextToSearch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean update(Stop ArgStop) {
		// TODO Auto-generated method stub
		return false;
	}

	

	
}
