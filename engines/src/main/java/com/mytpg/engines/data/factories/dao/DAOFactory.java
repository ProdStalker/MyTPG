package com.mytpg.engines.data.factories.dao;

import android.content.Context;

import com.mytpg.engines.data.abstracts.AbstractDAO;
import com.mytpg.engines.data.dao.ConnectionDAO;
import com.mytpg.engines.data.dao.DepartureAlarmDAO;
import com.mytpg.engines.data.dao.DepartureDAO;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.MnemoDAO;
import com.mytpg.engines.data.dao.PhysicalStopDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.data.dao.TicketDAO;
import com.mytpg.engines.data.dao.bustedapp.BustedStopDAO;
import com.mytpg.engines.data.factories.AbstractDAOFactory;
import com.mytpg.engines.entities.Connection;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.DepartureAlarm;
import com.mytpg.engines.entities.Disruption;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.Thermometer;
import com.mytpg.engines.entities.Ticket;
import com.mytpg.engines.entities.bustedapp.BustedStop;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.olddirections.Direction;
import com.mytpg.engines.entities.stops.Mnemo;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;

/**
 * Created by stalker-mac on 08.11.14.
 */
public class DAOFactory extends AbstractDAOFactory {
    protected static DatabaseHelper ms_dbHelper;

    public DAOFactory(Context ArgContext) {
        super(ArgContext);
        m_type = FactoryType.DB;
        ms_dbHelper = DatabaseHelper.getInstance(ArgContext);
    }

    @Override
    public void close()
    {
        if (ms_dbHelper != null)
        {
            ms_dbHelper.close();
        }
    }

    @Override
    public AbstractDAO<BustedStop> getAbsBustedStopDAO() {
        return new BustedStopDAO(ms_dbHelper);
    }

    @Override
    public AbstractDAO<Connection> getAbsConnectionDAO() {
        return new ConnectionDAO(ms_dbHelper);
    }

    @Override
    public AbstractDAO<DepartureAlarm> getAbsDepartureAlarmDAO() {
        return new DepartureAlarmDAO(ms_dbHelper);
    }

    @Override
    public AbstractDAO<Departure> getAbsDepartureDAO() {
        return new DepartureDAO(ms_dbHelper);
    }

    @Override
    public AbstractDAO<Direction> getAbsDirectionDAO() {
        return null;
    }

    @Override
    public AbstractDAO<Disruption> getAbsDisruptionDAO(){
        return null;
    }

    @Override
    public AbstractDAO<Line> getAbsLineDAO(){
        return new LineDAO(ms_dbHelper);
    }

    @Override
    public AbstractDAO<Mnemo> getAbsMnemoDAO() {
        return new MnemoDAO(ms_dbHelper);
    }

    @Override
    public AbstractDAO<PhysicalStop> getAbsPhysicalStopDAO() {
        return new PhysicalStopDAO(ms_dbHelper);
    }

    @Override
    public AbstractDAO<Stop> getAbsStopDAO() {
        return new StopDAO(ms_dbHelper);
    }

    @Override
    public AbstractDAO<Thermometer> getAbsThermometerDAO() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractDAO<Ticket> getAbsTicketDAO() {
        return new TicketDAO(ms_dbHelper);
    }

}

