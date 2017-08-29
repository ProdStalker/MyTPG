package com.mytpg.engines.data.factories;

import android.content.Context;

import com.mytpg.engines.data.abstracts.AbstractDAO;
import com.mytpg.engines.data.factories.api.APIDAOFactory;
import com.mytpg.engines.data.factories.dao.DAOFactory;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.DepartureAlarm;
import com.mytpg.engines.entities.Disruption;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Thermometer;
import com.mytpg.engines.entities.Ticket;
import com.mytpg.engines.entities.bustedapp.BustedStop;
import com.mytpg.engines.entities.olddirections.Direction;
import com.mytpg.engines.entities.stops.Mnemo;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;

/**
 * Created by stalker-mac on 08.11.14.
 */
public abstract class AbstractDAOFactory {
    public enum FactoryType {API, DB}
    protected FactoryType m_type = FactoryType.DB;
    protected static Context ms_context = null;

    public AbstractDAOFactory(final Context ArgContext)
    {
        ms_context = ArgContext.getApplicationContext();
    }

    public static AbstractDAOFactory getFactory(final Context ArgContext, final FactoryType ArgFactoryType)
    {
        AbstractDAOFactory absDAOFact = null;

        switch (ArgFactoryType)
        {
            case API :
                absDAOFact = new APIDAOFactory(ArgContext.getApplicationContext());
                break;
            case DB :
                absDAOFact = new DAOFactory(ArgContext.getApplicationContext());
                break;
        }

        return absDAOFact;
    }

    public FactoryType getType()
    {
        return this.m_type;
    }

    public abstract void close();

    public abstract AbstractDAO<BustedStop> getAbsBustedStopDAO();

    public abstract AbstractDAO<Connection> getAbsConnectionDAO();

    public abstract AbstractDAO<DepartureAlarm> getAbsDepartureAlarmDAO();

    public abstract AbstractDAO<Departure> getAbsDepartureDAO();

    public abstract AbstractDAO<Direction> getAbsDirectionDAO();

    public abstract AbstractDAO<Disruption> getAbsDisruptionDAO();

    public abstract AbstractDAO<Line> getAbsLineDAO();

    public abstract AbstractDAO<Mnemo> getAbsMnemoDAO();

    public abstract AbstractDAO<PhysicalStop> getAbsPhysicalStopDAO();

    public abstract AbstractDAO<Stop> getAbsStopDAO();

    public abstract AbstractDAO<Thermometer> getAbsThermometerDAO();

    public abstract AbstractDAO<Ticket> getAbsTicketDAO();


}