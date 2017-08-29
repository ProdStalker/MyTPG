package com.mytpg.program.core;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;

import com.mytpg.engines.data.factories.AbstractDAOFactory;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.LocationSettings;
import com.mytpg.engines.tools.DateTools;

import java.util.Calendar;


public class App extends MultiDexApplication {
    public enum FirstLaunchType {Alarm, App, DayDepartures, Direction, FavoriteStops, FilterNextDepartures, Lines, NextDepartures, Stops}

    private boolean mIsFirstAlarm = true;
    private boolean mIsFirstDayDepartures = true;
    private boolean mIsFirstDirection = true;
    private boolean mIsFirstFavoriteStop = true;
    private boolean mIsFirstFavoriteStops = true;
    private boolean mIsFirstFilterNextDepartures = true;
    private boolean mIsFirstLaunchApp = true;
    private boolean mIsFirstLaunchLines = true;
    private boolean mIsFirstLaunchStops = true;
    private boolean mIsFirstMap = true;
    private boolean mIsFirstMenuDayDepartures = true;
    private boolean mIsFirstMoreConnections = true;
    private boolean mIsFirstNextDepartures = true;
    private boolean mIsFirstOfflineDepartures = true;
    private boolean mIsFirstShare = true;
    private boolean mIsFirstThermometer = true;
    private boolean mIsFirstTicket = true;
	
	private static AbstractDAOFactory ms_absDAOFact = null;
		
	private void defaultSettings() {
        getSharedPreferences().edit().putInt(AppSettings.PREF_NUMBER_STOPS_PROXIMITY, LocationSettings.DEFAULT_NUMBER_STOP_PROXIMITY).apply();
	}
	
	private void firstLaunch() {
		if (mIsFirstLaunchApp)
		{
			setFirstLaunch();
		}
	}
	
	public AbstractDAOFactory getAbsDAOFact()
	{
		return ms_absDAOFact;
	}
	
	public SharedPreferences getSharedPreferences()
	{
        return PreferenceManager.getDefaultSharedPreferences(this);
		//return getSharedPreferences(AppSettings.PREFERENCES_NAME, MODE_PRIVATE);
	}

    public boolean isFirstAlarm() {
        return mIsFirstAlarm;
    }

    public boolean isFirstDayDepartures()
    {
        return mIsFirstDayDepartures;
    }

    public boolean isFirstDirection()
    {
        return mIsFirstDirection;
    }

    public boolean isFirstFavoriteStop() {
        return mIsFirstFavoriteStop;
    }

    public boolean isFirstFavoriteStops() {
        return mIsFirstFavoriteStops;
    }

    public boolean isFirstLaunchOfDay(FirstLaunchType ArgFirstLaunchType)
    {

        switch (ArgFirstLaunchType)
        {
            case App :
                return mIsFirstLaunchApp;
            case Lines :
                return mIsFirstLaunchLines;

            case Stops:
                return mIsFirstLaunchStops;
            default :
                return true;
        }


    }

    public boolean isFirstFilterNextDepartures() {
        return mIsFirstFilterNextDepartures;
    }

    public boolean isFirstMap() {
        return mIsFirstMap;
    }

    public boolean isFirstMenuDayDepartures()
    {
        return mIsFirstMenuDayDepartures;
    }

    public boolean isFirstMoreConnections() {
        return mIsFirstMoreConnections;
    }

    public boolean isFirstNextDepartures() {
        return mIsFirstNextDepartures;
    }

    public boolean isFirstOfflineDepartures() {
        return mIsFirstOfflineDepartures;
    }

    public boolean isFirstShare() {
        return mIsFirstShare;
    }

    public boolean isFirstThermometer()
    {
        return mIsFirstThermometer;
    }

    public boolean isFirstTicket() {
        return mIsFirstTicket;
    }

    private boolean isNeedUpdated(long ArgLastUpdatedMillis){
        Calendar now = DateTools.now();
        Calendar beforeYesterday = DateTools.now();
        beforeYesterday.setTimeInMillis(now.getTimeInMillis());
        beforeYesterday.add(Calendar.DAY_OF_MONTH,-2);
        if (ArgLastUpdatedMillis  < beforeYesterday.getTimeInMillis())
        {
            return true;
        }
        else
        {


            Calendar cal = DateTools.now();
            cal.setTimeInMillis(ArgLastUpdatedMillis);

            Calendar today = DateTools.now();
            today.setTimeInMillis(now.getTimeInMillis());
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND,0);
            today.set(Calendar.MILLISECOND, 0);

            if (DateTools.dateAreDifferent(cal, now, DateTools.ComparisonType.OnlyDay))
            {
                final int HourDay = now.get(Calendar.HOUR_OF_DAY);
                final int Minutes = now.get(Calendar.MINUTE);
                if (HourDay >= 3 && Minutes >= 30)
                {
                    return true;
                }
            }
            else
            {

                final int HourDay = cal.get(Calendar.HOUR_OF_DAY);
                final int Minutes = cal.get(Calendar.MINUTE);
                final int NowHourDay = now.get(Calendar.HOUR_OF_DAY);
                final int NowMinutes = now.get(Calendar.MINUTE);

					/*final int Millis = HourDay * 3600 + Minutes * 60;
					today.add(Calendar.MILLISECOND,Millis);
					if (today.get()*/
                if (HourDay <= 3 && Minutes <= 30 &&
                    NowHourDay >= 4 && NowMinutes >= 15)
                {
                    return true;
                }
            }
        }


        return false;
    }
	
	@Override
	public void onCreate()
	{
		super.onCreate();
        readSettings();
		setFactory(AbstractDAOFactory.FactoryType.DB);
	}

	@Override
    public void onTerminate() {
        if (ms_absDAOFact != null)
        {
        	ms_absDAOFact.close();
        }
        super.onTerminate();
    }

    private void readSettings(){

        try {
            int dbVersion = getSharedPreferences().getInt(AppSettings.PREF_CURRENT_VERSION_DB,-1);
            if (dbVersion < DatabaseHelper.DB_VERSION)
            {
                setFirstLaunch();
            }
            else {

                long lastUpdated;

                mIsFirstLaunchApp = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_LAUNCH, true);
                lastUpdated = getSharedPreferences().getLong(AppSettings.PREF_LAST_LINES_UPDATED, -1);
                mIsFirstLaunchLines = isNeedUpdated(lastUpdated);
                lastUpdated = getSharedPreferences().getLong(AppSettings.PREF_LAST_STOPS_UPDATED, -1);
                mIsFirstLaunchStops = isNeedUpdated(lastUpdated);

                mIsFirstDirection = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_DIRECTION, true);
                mIsFirstNextDepartures = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_NEXT_DEPARTURES, true);
                mIsFirstFilterNextDepartures = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_FILTER_NEXT_DEPARTURES, true);
                mIsFirstFavoriteStop = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_FAVORITE_STOP, true);
                mIsFirstShare = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_SHARE, true);
                mIsFirstMoreConnections = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_MORE_CONNECTIONS, true);
                mIsFirstFavoriteStops = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_FAVORITE_STOPS, true);
                mIsFirstTicket = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_TICKET, true);
                mIsFirstAlarm = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_ALARM, true);
                mIsFirstOfflineDepartures = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_OFFLINE_DEPARTURES, true);
                mIsFirstDayDepartures = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_DAY_DEPARTURES, true);
                mIsFirstMenuDayDepartures = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_MENU_DAY_DEPARTURES, true);
                mIsFirstThermometer = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_THERMOMETER, true);
                mIsFirstMap = getSharedPreferences().getBoolean(AppSettings.PREF_IS_FIRST_MAP, true);
            }


            if (mIsFirstLaunchApp) {
                firstLaunch();
            }
        }
        catch (Exception ex)
        {
            mIsFirstLaunchLines = true;
            mIsFirstLaunchStops = true;
            ex.printStackTrace();
        }

    }
	
	public void setAbsDAOFact(final AbstractDAOFactory ArgAbstractDAOFactory)
	{
		ms_absDAOFact = ArgAbstractDAOFactory;
	}
	
	public void setFactory(final AbstractDAOFactory.FactoryType ArgFactoryType)
	{
		setAbsDAOFact(AbstractDAOFactory.getFactory(getApplicationContext(), ArgFactoryType));
	}

    public void setFirstAlarm(boolean argIsFirstAlarm) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_ALARM, argIsFirstAlarm).commit();
        mIsFirstAlarm = argIsFirstAlarm;
    }

    public void setFirstDayDepartures(boolean argIsFirstDayDepartures) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_DAY_DEPARTURES, argIsFirstDayDepartures).commit();
        mIsFirstDayDepartures = argIsFirstDayDepartures;
    }

    public void setFirstDirection(boolean argIsFirstDirection)
    {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_DIRECTION, argIsFirstDirection).commit();
        mIsFirstDirection = argIsFirstDirection;
    }

    public void setFirstFavoriteStop(boolean argIsFirstFavoriteStop)
    {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_FAVORITE_STOP, argIsFirstFavoriteStop).commit();
        mIsFirstFavoriteStop = argIsFirstFavoriteStop;
    }

    public void setFirstFavoriteStops(boolean argIsFirstFavoriteStops)
    {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_FAVORITE_STOPS, argIsFirstFavoriteStops).commit();
        mIsFirstFavoriteStops = argIsFirstFavoriteStops;
    }

    public void setFirstLaunch() {
        defaultSettings();
        getSharedPreferences().edit().putInt(AppSettings.PREF_CURRENT_VERSION_DB, DatabaseHelper.DB_VERSION).commit();
        setFirstLaunchOfDay(FirstLaunchType.App,false);
        setFirstLaunchOfDay(FirstLaunchType.Lines,true);
        setFirstLaunchOfDay(FirstLaunchType.Stops,true);
       /* setFirstDirection(true);
        setFirstNextDepartures(true);
        setFirstFilterNextDepartures(true);*/
        setFirstFavoriteStop(true);
        /*setFirstShare(true);
        setFirstMoreConnections(true);
        setFirstFavoriteStops(true);
        setFirstTicket(true);
        setFirstAlarm(true);
        setFirstOfflineDepartures(true);
        setFirstDayDepartures(true);
        setFirstMenuDayDepartures(true);
        setFirstThermometer(true);
        setFirstMap(true);*/
    }

    public void setFirstLaunchOfDay(FirstLaunchType ArgFirstLaunchType, boolean ArgIsFirstLaunch)
    {
        switch (ArgFirstLaunchType)
        {
            case App :
                getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_LAUNCH,ArgIsFirstLaunch).commit();
                mIsFirstLaunchApp = ArgIsFirstLaunch;
                break;
            case Lines :
                if (ArgIsFirstLaunch) {
                    getSharedPreferences().edit().remove(AppSettings.PREF_LAST_LINES_UPDATED).commit();
                }
                else
                {
                    getSharedPreferences().edit().putLong(AppSettings.PREF_LAST_LINES_UPDATED, DateTools.now().getTimeInMillis()).commit();
                }
                mIsFirstLaunchLines = ArgIsFirstLaunch;
                break;
            case Stops :
                if (ArgIsFirstLaunch) {
                    getSharedPreferences().edit().remove(AppSettings.PREF_LAST_STOPS_UPDATED).commit();
                }
                else
                {
                    getSharedPreferences().edit().putLong(AppSettings.PREF_LAST_STOPS_UPDATED, DateTools.now().getTimeInMillis()).commit();
                }
                mIsFirstLaunchStops = ArgIsFirstLaunch;
            break;
        }
    }

    public void setFirstMap(boolean argIsFirstMap) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_MAP, argIsFirstMap).commit();
        mIsFirstMap = argIsFirstMap;
    }

    public void setFirstFilterNextDepartures(boolean argIsFirstFilterNextDepartures)
    {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_FILTER_NEXT_DEPARTURES, argIsFirstFilterNextDepartures).commit();
        mIsFirstFilterNextDepartures = argIsFirstFilterNextDepartures;
    }

    public void setFirstMenuDayDepartures(boolean argIsFirstMenuDayDepartures) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_MENU_DAY_DEPARTURES, argIsFirstMenuDayDepartures).commit();
        mIsFirstMenuDayDepartures = argIsFirstMenuDayDepartures;
    }

    public void setFirstMoreConnections(boolean argIsFirstMoreConnections) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_MORE_CONNECTIONS, argIsFirstMoreConnections).commit();
        mIsFirstMoreConnections = argIsFirstMoreConnections;
    }

    public void setFirstNextDepartures(boolean argIsFirstNextDepartures)
    {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_NEXT_DEPARTURES, argIsFirstNextDepartures).commit();
        mIsFirstNextDepartures = argIsFirstNextDepartures;
    }

    public void setFirstOfflineDepartures(boolean argIsFirstOfflineDepartures) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_OFFLINE_DEPARTURES, argIsFirstOfflineDepartures).commit();
        mIsFirstOfflineDepartures = argIsFirstOfflineDepartures;
    }

    public void setFirstShare(boolean argIsFirstShare) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_SHARE, argIsFirstShare).commit();
        mIsFirstShare = argIsFirstShare;
    }

    public void setFirstThermometer(boolean argIsFirstThermometer) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_THERMOMETER, argIsFirstThermometer).commit();
        mIsFirstThermometer = argIsFirstThermometer;
    }

    public void setFirstTicket(boolean argIsFirstTicket) {
        getSharedPreferences().edit().putBoolean(AppSettings.PREF_IS_FIRST_TICKET, argIsFirstTicket).commit();
        mIsFirstTicket = argIsFirstTicket;
    }
}
