package com.mytpg.engines.data.factories.api;

import android.content.Context;

import com.mytpg.engines.data.abstracts.AbstractDAO;
import com.mytpg.engines.data.factories.AbstractDAOFactory;
import com.mytpg.engines.data.oldapi.DepartureAPIDAO;
import com.mytpg.engines.data.oldapi.DirectionAPIDAO;
import com.mytpg.engines.data.oldapi.DisruptionAPIDAO;
import com.mytpg.engines.data.oldapi.LineAPIDAO;
import com.mytpg.engines.data.oldapi.StopAPIDAO;
import com.mytpg.engines.data.oldapi.ThermometerAPIDAO;
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
public class APIDAOFactory extends AbstractDAOFactory {

    /**
     * @param ArgContext
     */
    public APIDAOFactory(Context ArgContext) {
        super(ArgContext);
    }

    /* (non-Javadoc)
     * @see com.otpg.engines.data.factories.AbstractDAOFactory#close()
     */
    @Override
    public void close() {
    }

    @Override
    public AbstractDAO<BustedStop> getAbsBustedStopDAO() {
        return null;
    }

    @Override
    public AbstractDAO<Connection> getAbsConnectionDAO() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractDAO<DepartureAlarm> getAbsDepartureAlarmDAO() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractDAO<Departure> getAbsDepartureDAO() {
        return new DepartureAPIDAO();
    }

    @Override
    public AbstractDAO<Direction> getAbsDirectionDAO() {
        return new DirectionAPIDAO();
    }

    @Override
    public AbstractDAO<Disruption> getAbsDisruptionDAO(){
        return new DisruptionAPIDAO();
    }

    @Override
    public AbstractDAO<Line> getAbsLineDAO(){
        return new LineAPIDAO();
    }

    @Override
    public AbstractDAO<Mnemo> getAbsMnemoDAO() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractDAO<PhysicalStop> getAbsPhysicalStopDAO() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.otpg.engines.data.factories.AbstractDAOFactory#getAbsStopDAO()
     */
    @Override
    public AbstractDAO<Stop> getAbsStopDAO() {
        return new StopAPIDAO();
    }

    @Override
    public AbstractDAO<Thermometer> getAbsThermometerDAO() {
        return new ThermometerAPIDAO();
    }

    @Override
    public AbstractDAO<Ticket> getAbsTicketDAO() {
        // TODO Auto-generated method stub
        return null;
    }


}
