package com.mytpg.engines.entities.olddirections;

import com.mytpg.engines.entities.core.Entity;
import com.mytpg.engines.tools.DateTools;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by stalker-mac on 01.01.15.
 */
public class Step extends Entity {
    private Calendar m_arrivalDate = DateTools.now();
    private Calendar m_departureDate = DateTools.now();

    /**
     *
     */
    public Step() {
        super();
    }

    /**
     * @param ArgId
     */
    public Step(long ArgId) {
        super(ArgId);
    }

    public void fromHtml(String ArgHtml)
    {
        
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
    
}
