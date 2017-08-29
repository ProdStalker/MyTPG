/**
 * 
 */
package com.mytpg.engines.data.oldapi;

import android.util.Log;

import com.mytpg.engines.entities.Disruption;
import com.mytpg.engines.entities.network.http.HttpResult;
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
public class DisruptionAPIDAO extends APIDAO<Disruption> {

    @Override
    public long count()
    {
        long number;
        try
        {
            number = getAll().size();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            number = 0;
        }

        return number;
    }

	@Override
	public boolean create(Disruption ArgDisruption) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean create(List<Disruption> ArgObj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(Disruption ArgDisruption) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public Disruption find(long ArgId, boolean ArgIsComplete) {
        return null;
    }

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Disruption findByName(String ArgName) {
        return null;
    }

    @Override
	public List<Disruption> getAll() {
		List<Disruption> disruptions = new ArrayList<Disruption>();
		
		String url = DataSettings.API_BASE_URL + "GetDisruptions";
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("key", DataSettings.API_KEY));
		
		HttpResult httpResult = HttpTools.getHttpResultFromUrl(url, params, HttpResult.ResponseType.JSON, HttpTools.HttpMethod.GET);
		
		try {
			
			JSONObject jsonObj = new JSONObject(httpResult.getResult());
			
			String timestamp = jsonObj.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
			DateTools.setCurrentDate(timestamp);
			
			JSONArray disruptionsJsonArray = jsonObj.getJSONArray("disruptions");
			
			int i = 0;
			while (i < disruptionsJsonArray.length())
			{
				Disruption disruption = new Disruption();
				disruption.fromJson(disruptionsJsonArray.getJSONObject(i));
				disruptions.add(disruption);
				i++;
			}
			
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("DisruptionAPIDAO Disruption 77", e.getLocalizedMessage());
			disruptions = new ArrayList<Disruption>();
		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
		
		return disruptions;
	}
	@Override
	public boolean update(Disruption ArgDisruption) {
		// TODO Auto-generated method stub
		return false;
	}

	

	
}
