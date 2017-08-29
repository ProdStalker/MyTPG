/**
 * 
 */
package com.mytpg.engines.data.oldapi;

import android.util.Log;

import com.mytpg.engines.data.interfaces.ILineDAO;
import com.mytpg.engines.entities.Line;
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
public class LineAPIDAO extends APIDAO<Line> implements ILineDAO {

	@Override
	public boolean create(Line ArgLine) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean create(List<Line> ArgLines) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(Line ArgLine) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public Line find(long ArgId, boolean ArgIsComplete) {
        return null;
    }

    /**
     * @param ArgName
     * @return
     */
    @Override
    public Line findByName(String ArgName) {
        return null;
    }

	@Override
	public List<Line> getAll() {
		List<Line> lines = new ArrayList<Line>();
		
		String url = DataSettings.API_BASE_URL + "GetLinesColors";
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("key", DataSettings.API_KEY));
		
		HttpResult httpResult = HttpTools.getHttpResultFromUrl(url, params, HttpResult.ResponseType.JSON, HttpTools.HttpMethod.GET);
		
		try {
			
			JSONObject jsonObj = new JSONObject(httpResult.getResult());
			
			String timestamp = jsonObj.optString(DataSettings.API_JSON_TPG_TIMESTAMP,"");
			DateTools.setCurrentDate(timestamp);
			
			JSONArray linesJsonArray = jsonObj.getJSONArray("colors");
			
			int i = 0;
			while (i < linesJsonArray.length())
			{
				Line line = new Line();
				line.fromJson(linesJsonArray.getJSONObject(i));
				lines.add(line);
				i++;
			}
			
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("LineAPIDAO Line 95", e.getLocalizedMessage());
			lines = new ArrayList<Line>();
		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
		
		return lines;
	}
	@Override
	public List<Line> getAll(final boolean ArgIsDistinct) {
		return new ArrayList<>();
	}

	@Override
	public List<Line> getAllByName(String ArgName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean update(Line ArgLine) {
		// TODO Auto-generated method stub
		return false;
	}

	

	
}
