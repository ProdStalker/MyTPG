package com.mytpg.engines.entities.olddirections;

import android.text.Html;

import com.mytpg.engines.entities.core.Entity;

import org.json.JSONObject;

/**
 * Created by stalker-mac on 01.01.15.
 */
public abstract class DirectionEntity extends Entity {
    private String m_arrival = "";
    private String m_departure = "";

    /**
     *
     */
    public DirectionEntity() {
        super();
    }

    /**
     * @param ArgId
     */
    public DirectionEntity(long ArgId) {
        super(ArgId);
    }

    public abstract void fromHtml(String ArgHtml);

    @Override
    public abstract void fromJson(JSONObject ArgJsonObj);

    public String getArrival()
    {
        return m_arrival;
    }

    public String getDeparture()
    {
        return m_departure;
    }

    public void setArrival(String ArgArrival)
    {
        if (ArgArrival == null)
        {
            ArgArrival = "";
        }


        m_arrival = Html.fromHtml(ArgArrival.trim()).toString();
    }

    public void setDeparture(String ArgDeparture)
    {
        if (ArgDeparture == null)
        {
            ArgDeparture = "";
        }
        m_departure = Html.fromHtml(ArgDeparture.trim()).toString();
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
        String text = super.toString() + "\n";

        text += "De:" + getDeparture() +"\n";
        text += "A:" + getArrival() +"\n";

        return text;
    }
}
