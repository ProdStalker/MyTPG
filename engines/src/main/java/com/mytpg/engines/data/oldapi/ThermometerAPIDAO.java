/**
 * 
 */
package com.mytpg.engines.data.oldapi;

import android.util.Log;

import com.mytpg.engines.data.interfaces.IThermometerDAO;
import com.mytpg.engines.entities.Thermometer;
import com.mytpg.engines.entities.network.http.HttpResult;
import com.mytpg.engines.network.http.tools.HttpTools;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.engines.tools.DateTools;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * @author stalker-mac
 *
 */
public class ThermometerAPIDAO extends APIDAO<Thermometer> implements IThermometerDAO {

	@Override
	public boolean create(Thermometer ArgThermometer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean create(List<Thermometer> ArgObj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(Thermometer ArgThermometer) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public Thermometer find(long ArgId, boolean ArgIsComplete) {
        return null;
    }

    @Override
	public Thermometer findByCode(int ArgDepartureCode) {
		Thermometer thermometer;
		
		String url = DataSettings.API_BASE_URL + "GetThermometer";
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("key", DataSettings.API_KEY));
		params.add(new BasicNameValuePair("departureCode", String.valueOf(ArgDepartureCode)));
		
		HttpResult httpResult = HttpTools.getHttpResultFromUrl(url, params, HttpResult.ResponseType.JSON, HttpTools.HttpMethod.GET);
		
		try {
			
			JSONObject jsonObj = new JSONObject(httpResult.getResult());
			String timestamp = jsonObj.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
			DateTools.setCurrentDate(timestamp);
			
			thermometer = new Thermometer();
			thermometer.fromJson(jsonObj);
			
			/*int i = thermometer.getCheckPoints().size()-1;
			while (i >= 0)
			{
				if (!thermometer.getCheckPoints().get(i).isVisible())
				{
					thermometer.getCheckPoints().remove(i);
				}
				i--;
			}*/
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("StopAPIDAO Line 90", e.getLocalizedMessage());
			thermometer = null;
		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
			thermometer = null;
		}
		
		return thermometer;
	}

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Thermometer findByName(String ArgName) {
        return null;
    }

    @Override
	public List<Thermometer> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean update(Thermometer ArgThermometer) {
		// TODO Auto-generated method stub
		return false;
	}

}
