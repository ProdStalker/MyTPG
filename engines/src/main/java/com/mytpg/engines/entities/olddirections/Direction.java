package com.mytpg.engines.entities.olddirections;

import android.util.Log;

import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 01.01.15.
 */
public class Direction extends DirectionEntity {
    private Calendar m_date = DateTools.now();
    private boolean m_isArrival = false;
    private List<SubDirection> m_subDirections = new ArrayList<SubDirection>();
    public String html =  "";

    public Direction()
    {
        super();
    }

    public Direction(long ArgId)
    {
        super(ArgId);
    }

    public Calendar getDate()
    {
        return m_date;
    }

    public List<SubDirection> getSubDirections()
    {
        return m_subDirections;
    }

    public boolean isArrival()
    {
        return m_isArrival;
    }

    public void setArrival(boolean ArgIsArrival)
    {
        m_isArrival = ArgIsArrival;
    }

    public void setDate(Calendar ArgDate)
    {
        if (ArgDate == null)
        {
            ArgDate = DateTools.now();
        }

        m_date = ArgDate;
    }

    public void setSubDirections(List<SubDirection> ArgSubDirections)
    {
        if (ArgSubDirections == null)
        {
            ArgSubDirections = new ArrayList<SubDirection>();
        }
        m_subDirections = ArgSubDirections;
    }

    @Override
    public void fromHtml(String ArgHtml)
    {
        int indexContent = ArgHtml.indexOf("<div id=\"HFSContent\">");
        int indexEndContent = ArgHtml.indexOf("<div id=\"HFSFooter\">");
        if (indexContent != -1 && indexEndContent != -1) {
            ArgHtml = ArgHtml.substring(indexContent, indexEndContent);
        }
        else
        {
            return;
        }

        String textFrom = "<span class=\"output\">";
        int indexFrom = ArgHtml.indexOf(textFrom);
        int indexEndFrom = ArgHtml.indexOf("</span>",indexFrom);


        setDeparture(ArgHtml.substring(indexFrom+textFrom.length(),indexEndFrom));
        ArgHtml = ArgHtml.substring(indexEndFrom);

        String textTo = textFrom;
        int indexTo = ArgHtml.indexOf(textTo);
        int indexEndTo = ArgHtml.indexOf("</span>",indexTo);
        setArrival(ArgHtml.substring(indexTo+textTo.length(),indexEndTo));
        ArgHtml = ArgHtml.substring(indexEndTo);


        String textDate = textFrom;
        int indexDate = ArgHtml.indexOf(textDate);
        int indexEndDate = ArgHtml.indexOf("</span>",indexDate);
        String date = ArgHtml.substring(indexDate+textDate.length(),indexEndDate).trim();
        ArgHtml = ArgHtml.substring(indexEndDate);
        Log.d("DATE",date);
        date = date.substring(date.indexOf(' ')).trim();
        Log.d("DATE AFTER",date);

        String textHour = textFrom;
        int indexHour = ArgHtml.indexOf(textHour);
        int indexEndHour = ArgHtml.indexOf("</span>",indexHour);
        String hour = ArgHtml.substring(indexHour+textHour.length(),indexEndHour).trim();
        ArgHtml = ArgHtml.substring(indexEndHour);
        Log.d("HOUR",hour);
        hour = hour.substring(0,5).trim();
        Log.d("HOUR AFTER",hour);

        Calendar fullDate = DateTools.now();
        String[] dateArray = date.split("\\.");
        fullDate.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dateArray[0]));
        fullDate.set(Calendar.MONTH, Integer.valueOf(dateArray[1])-1);
        fullDate.set(Calendar.YEAR, Integer.valueOf(dateArray[2])+2000);
        fullDate.set(Calendar.SECOND,0);
        fullDate.set(Calendar.MILLISECOND,0);

        String[] hourArray = hour.split(":");
        fullDate.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hourArray[0]));
        fullDate.set(Calendar.MINUTE, Integer.valueOf(hourArray[1]));

        setDate(fullDate);

        int indexTable = ArgHtml.indexOf("<table class=\"resultTable\"");
        int indexEndTable = ArgHtml.indexOf("</table>",indexTable);
        String tableResult = ArgHtml.substring(indexTable,indexEndTable);

        int numberSubDirection = -1;
        String textSubDirection = "<tr class=\"tpOverview";
        int currentIndex = 0;
        while (currentIndex != -1)
        {
            int indexSubDirection = tableResult.indexOf(textSubDirection,currentIndex);
            if (indexSubDirection == -1)
            {
                break;
            }
            int indexEndSubDirection = tableResult.indexOf("</tr>",indexSubDirection);
            currentIndex = indexEndSubDirection;
            String subDirectionHtml = tableResult.substring(indexSubDirection,indexEndSubDirection);

            if (numberSubDirection != -1) {
                SubDirection subDirection = new SubDirection();

                subDirection.fromHtml(subDirectionHtml);

                m_subDirections.add(subDirection);
            }
            numberSubDirection++;
        }

    }

    @Override
    public void fromJson(JSONObject ArgJsonObj) {

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
        String text = super.toString();
        text += "Date:" + DateTools.dateToString(getDate()) +"\n";
        text += "Sub directions :\n";

        for (int i = 0; i < m_subDirections.size(); i++)
        {
            text += "\t- N° " + String.valueOf(i+1) + " : ";

            text += "\t\t" + m_subDirections.get(i).toString() + "\n";
        }
        return text;
    }
}
