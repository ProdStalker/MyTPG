package com.mytpg.engines.entities.olddirections;

import android.text.Html;
import android.util.Log;

import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 01.01.15.
 */
public class SubDirection extends DirectionEntity {
    public enum Type {Transport,Walk}

    private Calendar m_arrivalDate = DateTools.now();
    private Calendar m_departureDate = DateTools.now();
    private int m_numberChanges = -1;
    private List<Step> m_steps = new ArrayList<>();

    /**
     *
     */
    public SubDirection() {
        super();
    }

    /**
     * @param ArgId
     */
    public SubDirection(long ArgId) {
        super(ArgId);
    }

    @Override
    public void fromHtml(String ArgHtml)
    {
        Log.d("SUBDIRECTION",ArgHtml);
        String textStation = "class=\"station\"";
        int indexStation = ArgHtml.indexOf(textStation);
        int indexEndStation = ArgHtml.indexOf("</td>",indexStation);

        String station = ArgHtml.substring(indexStation,indexEndStation);
        Log.d("STATION",station);
        String stationDiv = "</div>";
        station = station.substring(station.indexOf(stationDiv)+stationDiv.length());
        String[] stationArray = station.split("<br />");
        setDeparture(stationArray[0]);
        setArrival(stationArray[1]);
        Log.d("STATION AFTER",station);

        ArgHtml = ArgHtml.substring(indexEndStation);
        String textDate = "<td headers=\"hafasOVDate\">";
        int indexDate = ArgHtml.indexOf(textDate);
        int indexEndDate = ArgHtml.indexOf("</td>",indexDate);
        String date = ArgHtml.substring(indexDate + textDate.length(),indexEndDate).trim();
        Log.d("DATE", date);
        date = date.substring(0,8).trim();
        Log.d("DATE AFTER", date);

        ArgHtml = ArgHtml.substring(indexEndDate);
        String textHour = "class=\"time\"";
        int indexHour = ArgHtml.indexOf(textHour);
        int indexEndHour = ArgHtml.indexOf("</td>",indexHour);
        String hour = ArgHtml.substring(indexHour, indexEndHour).trim();
        Log.d("HOUR",hour);
        String hourDiv = "<div>";
        int indexHourDiv = hour.indexOf(hourDiv);
        int indexEndHourDiv = hour.indexOf("</div>");
        hour = Html.fromHtml(hour.substring(indexHourDiv + hourDiv.length(), indexEndHourDiv).trim()).toString();
        hour = hour.replace("dép. ","").trim();
        hour = hour.replace("arr. ","").trim();
        String[] hourArray = hour.split("\\\n");

        String[] hourDepartureArray = hourArray[0].split(":");
        String[] hourArrivalArray = hourArray[1].split(":");
       /* hourArray[0] = hourArray[0].substring(hourArray[0].length()-5);
        hourArray[1] = hourArray[1].substring(hourArray[1].length()-5);*/
        hour = hourArray[0] + " à " + hourArray[1];
        Log.d("HOUR AFTER",hour);

        m_departureDate = DateTools.now();
        m_arrivalDate = DateTools.now();
        String[] dateArray = date.split("\\.");
        m_departureDate.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dateArray[0]));
        m_departureDate.set(Calendar.MONTH, Integer.valueOf(dateArray[1])-1);
        m_departureDate.set(Calendar.YEAR, Integer.valueOf(dateArray[2])+2000);
        m_departureDate.set(Calendar.SECOND,0);
        m_departureDate.set(Calendar.MILLISECOND,0);

        m_arrivalDate.setTimeInMillis(m_departureDate.getTimeInMillis());

        m_departureDate.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hourDepartureArray[0]));
        m_departureDate.set(Calendar.MINUTE, Integer.valueOf(hourDepartureArray[1]));

        m_arrivalDate.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hourArrivalArray[0]));
        m_arrivalDate.set(Calendar.MINUTE, Integer.valueOf(hourArrivalArray[1]));

        ArgHtml = ArgHtml.substring(indexEndHour);
        Log.d("HTML",ArgHtml);
        String textChanges = "class=\"changes\"";
        int indexChanges = ArgHtml.indexOf(textChanges);
        int indexEndChanges = ArgHtml.indexOf("</td>",indexChanges);
        String changes = ArgHtml.substring(ArgHtml.indexOf(">",indexChanges)+1,indexEndChanges).trim();
        setNumberChanges(Integer.valueOf(changes));
    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {

    }

    public Calendar getArrivalDate()
    {
        return m_arrivalDate;
    }

    public Calendar getDepartureDate()
    {
        return m_departureDate;
    }

    public int getNumberChanges()
    {
        return m_numberChanges;
    }

    public List<Step> getSteps()
    {
        return m_steps;
    }

    public void setArrivalDate(Calendar ArgArrivalDate)
    {
        if (ArgArrivalDate == null)
        {
            ArgArrivalDate = DateTools.now();
        }

        m_arrivalDate = ArgArrivalDate;
    }

    public void setDepartureDate(Calendar ArgDepartureDate)
    {
        if (ArgDepartureDate == null)
        {
            ArgDepartureDate = DateTools.now();
        }

        m_departureDate = ArgDepartureDate;
    }

    public void setNumberChanges(int ArgNumberChanges)
    {
        if (ArgNumberChanges < 0)
        {
            ArgNumberChanges = -1;
        }

        m_numberChanges = ArgNumberChanges;
    }

    public void setSteps(List<Step> ArgSteps)
    {
        if (ArgSteps == null)
        {
            ArgSteps = new ArrayList<Step>();
        }
        m_steps = ArgSteps;
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * object. Subclasses are encouraged to override this method and provide an
     * implementation that takes into account the object's type and data. The
     * default implementation is equivalent to the following expression:
     * <pre>
     *   getClass().getName() + '@' + Integer.toHexString(hashCode())</pre>
     * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_toString">Writing a useful
     * {@code toString} method</a>
     * if you intend implementing your own {@code toString} method.
     *
     * @return a printable representation of this object.
     */
    @Override
    public String toString() {
        String text = "";

        text += super.toString() + "\n";
        text += DateTools.dateToString(m_departureDate) + " à " + DateTools.dateToString(m_arrivalDate) + "\n";
        text += "Number changes : " + String.valueOf(m_numberChanges);
        return text;
    }
}
