/**
 * 
 */
package com.mytpg.engines.data.oldapi;

import android.util.Log;

import com.mytpg.engines.data.interfaces.IDepartureDAO;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Line;
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
import java.util.Collections;
import java.util.List;


/**
 * @author stalker-mac
 *
 */
public class DepartureAPIDAO extends APIDAO<Departure> implements IDepartureDAO {

    @Override
    public long countOfflinesGrouped() {
        return 0;
    }

	@Override
	public boolean create(Departure ArgDeparture) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean create(List<Departure> ArgDepartures) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(Departure ArgDeparture) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
	public boolean deleteByLineAndStopAndDestination(long ArgLineId, long ArgStopId, long ArgDestinationId) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public Departure find(long ArgId, boolean ArgIsComplete) {
        return null;
    }

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Departure findByName(String ArgName) {
        return null;
    }

    @Override
	public List<Departure> getAll() {
		List<Departure> departures = new ArrayList<Departure>();
		return departures;
	}
	
	@Override
	public List<Departure> getAllByMnemo(String ArgMnemo, Line[] ArgConnectionsFilter, int ArgDepartureCode) {
		List<Line> connections = new ArrayList<Line>();
		
		if (ArgConnectionsFilter != null)
		{
			Collections.addAll(connections, ArgConnectionsFilter);
		}
		
		return getAllByMnemo(ArgMnemo,connections, ArgDepartureCode);
	}
	
	@Override
	public List<Departure> getAllByMnemo(String ArgMnemo, List<Line> ArgConnectionsFilter, int ArgDepartureCode) {
		List<Departure> departures = new ArrayList<Departure>();
		
		if (ArgMnemo.isEmpty())
		{
			return departures;
		}
		
		String url = DataSettings.API_BASE_URL + "GetNextDepartures";
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("key", DataSettings.API_KEY));
		params.add(new BasicNameValuePair("stopCode", ArgMnemo));
		if (ArgDepartureCode != -1)
		{
			params.add(new BasicNameValuePair("departureCode", String.valueOf(ArgDepartureCode)));
		}
		
		if (!ArgConnectionsFilter.isEmpty())
		{
			String linesCode = "";
			String destinationsCode = "";
			
			int i = 0;
			while (i < ArgConnectionsFilter.size())
			{
				Line connection = ArgConnectionsFilter.get(i);
				
				linesCode += connection.getName();
				destinationsCode += connection.getArrivalStop().getCode();
				
				if (i < ArgConnectionsFilter.size() - 1)
				{
					linesCode += ",";
					destinationsCode += ",";
				}
				
				i++;
			}
			
			params.add(new BasicNameValuePair("linesCode", linesCode));
			params.add(new BasicNameValuePair("destinationsCode", destinationsCode));
			
		}
		
		HttpResult httpResult = HttpTools.getHttpResultFromUrl(url, params, HttpResult.ResponseType.JSON, HttpTools.HttpMethod.GET);
		
		try {
			
			JSONObject jsonObj = new JSONObject(httpResult.getResult());
			
			String timestamp = jsonObj.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
			DateTools.setCurrentDate(timestamp);
			
			JSONArray departuresJsonArray = jsonObj.getJSONArray("departures");

			JSONObject stopJsonObj = jsonObj.optJSONObject("stop");
			Stop stop = new Stop();
			stop.fromJson(stopJsonObj);
			
			int i = 0;
			while (i < departuresJsonArray.length())
			{
				Departure departure = new Departure();
				departure.fromJson(departuresJsonArray.getJSONObject(i));
				if (departure.getCode() != -1)
				{
					departure.setStop(new Stop(stop));
					departures.add(departure);
				}
				i++;
			}
			
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("DepartureAPIDAO Departure 90", e.getLocalizedMessage());
			departures = new ArrayList<Departure>();
		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
		
		return departures;
		
	}

	@Override
	public List<Departure> getAllGroupedByLineAndStop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Departure> getDayDepartures(String ArgLine, String ArgMnemo, String ArgDestination) {
		List<Departure> departures = new ArrayList<Departure>();
		
		if (ArgLine.isEmpty() || ArgMnemo.isEmpty() || ArgDestination.isEmpty())
		{
			return departures;
		}
		
	 	/*ArgDestination = Normalizer.normalize(ArgDestination, Normalizer.Form.NFD);
		ArgDestination = ArgDestination.replaceAll("[^\\p{ASCII}]", "");
		ArgDestination = ArgDestination.toUpperCase(Locale.FRENCH);*/
		
		String url = DataSettings.API_BASE_URL + "GetAllNextDepartures";
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("key", DataSettings.API_KEY));
		params.add(new BasicNameValuePair("destinationCode", ArgDestination));
		params.add(new BasicNameValuePair("lineCode", ArgLine));
		params.add(new BasicNameValuePair("stopCode", ArgMnemo));
		
		HttpResult httpResult = HttpTools.getHttpResultFromUrl(url, params, HttpResult.ResponseType.JSON, HttpTools.HttpMethod.GET);
		
		try {
			
			JSONObject jsonObj = new JSONObject(httpResult.getResult());
			
			String timestamp = jsonObj.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
			DateTools.setCurrentDate(timestamp);
			
			JSONArray departuresJsonArray = jsonObj.getJSONArray("departures");

			JSONObject stopJsonObj = jsonObj.optJSONObject("stop");
			Stop stop = new Stop();
			stop.fromJson(stopJsonObj);
			
			int i = 0;
			while (i < departuresJsonArray.length())
			{
				Departure departure = new Departure();
				departure.fromJson(departuresJsonArray.getJSONObject(i));
				if (departure.getCode() != -1)
				{
					departure.setStop(new Stop(stop));
					departures.add(departure);
				}
				i++;
			}
			
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("DepartureAPIDAO Departure 90", e.getLocalizedMessage());
			departures = new ArrayList<Departure>();
		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
		
		return departures;
	}

	@Override
	public boolean update(Departure ArgDeparture) {
		// TODO Auto-generated method stub
		return false;
	}

	

	
}
