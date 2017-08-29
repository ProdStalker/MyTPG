package com.mytpg.engines.network.http.tools;

import android.util.Log;

import com.mytpg.engines.entities.network.http.HttpResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by stalker-mac on 08.11.14.
 */
public class HttpTools
{
    public enum HttpMethod {DELETE, GET, POST, PUT}

    public static String createUrlParamatersFromList(List<NameValuePair> ArgParams)
    {

        String paramsString = "";

        if (ArgParams.size() > 0)
        {
            paramsString = "?";
            int i = 0;
            while (i < ArgParams.size())
            {
                String value = ArgParams.get(i).getValue();
                value = value.replace(",","%2C");
                value = value.replace(" ", "%20");
                value = value.replace("+", "%2B");
                paramsString += ArgParams.get(i).getName() + "=" + value;
                i++;
                if (i < ArgParams.size())
                {
                    paramsString += "&";
                }
            }

        }

        return paramsString;
    }

    public static HttpResult getHttpResultFromUrl(String ArgUrl, List<NameValuePair> ArgParams, HttpResult.ResponseType ArgResponseType, HttpTools.HttpMethod ArgHttpMethod)
    {
        InputStream inputStream = null;
        String html;

        try
        {
            HttpResponse httpResponse = null;


            String paramsString = createUrlParamatersFromList(ArgParams);

            if (ArgResponseType == HttpResult.ResponseType.JSON)
            {
                ArgUrl += ".json";
            }
            else if (ArgResponseType == HttpResult.ResponseType.XML)
            {
                ArgUrl += ".xml";
            }

            Log.d("URL",ArgUrl + paramsString);

            HttpParams my_httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(my_httpParams, 10000);
            //HttpConnectionParams.setSoTimeout(my_httpParams, 1);

            DefaultHttpClient httpClient = new DefaultHttpClient(my_httpParams);

            //HttpPost httpPost = new HttpPost(ArgUrl);
            //httpPost.setEntity(new UrlEncodedFormEntity(ArgParams));

            switch (ArgHttpMethod)
            {
                case DELETE :
                    break;
                case GET :
                    HttpGet httpGet = new HttpGet(ArgUrl+paramsString);
                    httpResponse = httpClient.execute(httpGet);
                    break;
                case POST :
                    HttpPost httpPost = new HttpPost(ArgUrl);
                    httpPost.setEntity(new UrlEncodedFormEntity(ArgParams));
                    httpResponse = httpClient.execute(httpPost);
                    break;
                case PUT :
                    break;
            }

            if (httpResponse != null)
            {
                HttpEntity httpEntity = httpResponse.getEntity();
                inputStream = httpEntity.getContent();
            }
        }
        catch (UnsupportedEncodingException uee)
        {
            uee.printStackTrace();
            return null;
        }
        catch (ClientProtocolException cpe)
        {
            cpe.printStackTrace();
            return null;
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return null;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }

        try
        {
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"),8);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }
            inputStream.close();
            html = stringBuilder.toString();
        }
        catch (Exception ex)
        {
            Log.e("Buffer Error", "Error converting result " + ex.toString());
            return null;
        }

        return new HttpResult(html,ArgResponseType);
    }

    public static JSONObject jsonObjectFromHttpResult(HttpResult ArgHttpResult)
    {

        JSONObject jsonObj;

        if (ArgHttpResult == null)
        {
            return null;
        }


        try
        {
            jsonObj = new JSONObject(ArgHttpResult.getResult());
        }
        catch (JSONException je)
        {
            jsonObj = null;
            je.printStackTrace();
        }

        return jsonObj;
    }
}
