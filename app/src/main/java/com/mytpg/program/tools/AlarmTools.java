package com.mytpg.program.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.mytpg.engines.data.dao.DepartureAlarmDAO;
import com.mytpg.engines.data.dao.LineDAO;
import com.mytpg.engines.data.dao.StopDAO;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.DepartureAlarm;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.entities.stops.Stop;
import com.mytpg.engines.settings.NotificationSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.R;
import com.mytpg.program.fragments.DepartureAlarmsFragment;
import com.mytpg.program.fragments.ThermometerFragment;
import com.mytpg.program.receivers.AlarmReceiver;

import java.util.Calendar;

/**
 * Created by stalker-mac on 16.11.14.
 */
public abstract class AlarmTools {

    private static Intent configureIntent(Context argContext, Departure argDeparture, int argMinutesBefore)
    {
        Intent newIntent = new Intent(argContext.getApplicationContext(),AlarmReceiver.class);
        newIntent.putExtra(AlarmReceiver.ARG_NOTIF_TYPE, NotificationSettings.NOTIF_DEPARTURE);
        newIntent.putExtra(ThermometerFragment.ARG_DEPARTURE_CODE, argDeparture.getCode());
        newIntent.putExtra(DepartureAlarmsFragment.ARG_MINUTES, argMinutesBefore);

        return newIntent;
    }

    public static boolean removeAlarm(Context argContext, DepartureAlarm argDepartureAlarm, int argMinutesBefore)
    {
        return removeAlarm(argContext, argDepartureAlarm, true, argMinutesBefore);
    }

    public static boolean removeAlarm(Context argContext, DepartureAlarm argDepartureAlarm, boolean argNeedInfos, int argMinutesBefore)
    {
        return removeAlarm(argContext, argDepartureAlarm, argNeedInfos, null, argMinutesBefore);
    }

    public static boolean removeAlarm(Context argContext, DepartureAlarm argDepartureAlarm, boolean argNeedInfos, View argView, int argMinutesBefore)
    {
        Departure departure = new Departure();
        departure.setId(argDepartureAlarm.getId());
        departure.setCode(argDepartureAlarm.getDepartureCode());

        return removeAlarm(argContext, departure, argNeedInfos, argView, argMinutesBefore);
    }

    public static boolean removeAlarm(Context argContext, Departure argDeparture, int argMinutesBefore) {
        return removeAlarm(argContext, argDeparture, true, argMinutesBefore);
    }

    public static boolean removeAlarm(Context argContext, Departure argDeparture, boolean argNeedInfos, int argMinutesBefore)
    {
        return removeAlarm(argContext,argDeparture,argNeedInfos,null, argMinutesBefore);
    }

    @SuppressWarnings("static-access")
    public static boolean removeAlarm(Context argContext, Departure argDeparture, boolean argNeedInfos, View argView, int argMinutesBefore){
        DepartureAlarmDAO depAlarmDAO = new DepartureAlarmDAO(DatabaseHelper.getInstance(argContext));
        final boolean Deleted = depAlarmDAO.deleteByCode(argDeparture.getCode());
        
        if (Deleted)
        {
            if (argNeedInfos)
            {
                String text = argContext.getString(R.string.alarm_deleted);
                if (argView != null)
                {
                    Snackbar.make(argView, text, Snackbar.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(argContext, text, Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            return false;
        }

        AlarmManager alarmManager = (AlarmManager)argContext.getApplicationContext().getSystemService(argContext.getApplicationContext().ALARM_SERVICE);
        Intent newIntent = configureIntent(argContext, argDeparture, argMinutesBefore);
        
        PendingIntent pendIntent = PendingIntent.getBroadcast(argContext.getApplicationContext(), argDeparture.getCode(), newIntent, PendingIntent.FLAG_NO_CREATE);
        if (pendIntent != null)
        {
            argDeparture.setAlarm(false);
            alarmManager.cancel(pendIntent);
        }


        return true;
    }

    public static boolean setAlarm(Context argContext, Departure argDeparture, int argMinutesBefore)
    {
        return setAlarm(argContext, argDeparture, null, argMinutesBefore);
    }

    @SuppressWarnings("static-access")
    public static boolean setAlarm(Context argContext, Departure argDeparture, View argView, int argMinutesBefore) {
        if (argDeparture == null)
        {
            return false;
        }

        StopDAO stopDAO = new StopDAO(DatabaseHelper.getInstance(argContext));
        if (argDeparture.getStop().getId() < 1)
        {

            Stop stop = stopDAO.search(argDeparture.getStop().getName());
            if (stop == null)
            {
                return false;
            }

            argDeparture.setStop(stop);
        }

        if (argDeparture.getLine().getArrivalStop().getId() < 1)
        {
            Stop stop = stopDAO.search(argDeparture.getLine().getArrivalStop().getName());
            if (stop == null)
            {
                return false;
            }

            argDeparture.getLine().getArrivalStop().setId(stop.getId());
        }

        if (argDeparture.getLine().getId() < 1)
        {
            LineDAO lineDAO = new LineDAO(DatabaseHelper.getInstance(argContext));
            Line line = lineDAO.findByLine(argDeparture.getLine());
            if (line != null)
            {
                argDeparture.getLine().setId(line.getId());
            }
            /*List<Line> lines = lineDAO.getAllByName(argDeparture.getLine().getName());
            if (lines.isEmpty())
            {
                return false;
            }

            for (Line line : lines)
            {
                if (line.equals(argDeparture.getLine()))
                {
                    argDeparture.getLine().setId(line.getId());
                    break;
                }
            }*/
            if (argDeparture.getLine().getId() < 1)
            {
                return false;
            }
        }

        if (!AlarmTools.canAddAlarm(argDeparture, argMinutesBefore))
        {
            String text = argContext.getString(R.string.need_minimum_minutes, argMinutesBefore);
            if (argView != null)
            {
                Snackbar.make(argView,text,Snackbar.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(argContext, text, Toast.LENGTH_LONG).show();
            }
            return false;
        }

        return createAlarm(argContext, argDeparture, argMinutesBefore, argView);
    }

    private static boolean createAlarm(Context argContext, Departure argDeparture, int argMinutesBefore, View argView)
    {
        Calendar calAlarm = Calendar.getInstance();
        int minutesToAdd = (int) DateTools.diffBetweenDates(argDeparture.getDate(), calAlarm, Calendar.MINUTE)-argMinutesBefore;
        calAlarm.add(Calendar.MINUTE, minutesToAdd);
        calAlarm.set(Calendar.SECOND,0);

        DepartureAlarm depAlarm = new DepartureAlarm();
        depAlarm.setDepartureCode(argDeparture.getCode());
        depAlarm.setDate(argDeparture.getDate());
        depAlarm.getLine().setId(argDeparture.getLine().getId());
        depAlarm.getStop().setId(argDeparture.getStop().getId());
        depAlarm.getLine().getArrivalStop().setId(argDeparture.getLine().getArrivalStop().getId());

        AlarmManager alarmManager = (AlarmManager)argContext.getApplicationContext().getSystemService(argContext.getApplicationContext().ALARM_SERVICE);
        Intent newIntent = configureIntent(argContext, argDeparture, argMinutesBefore);

        removeAlarm(argContext,argDeparture,false, argMinutesBefore);


        DepartureAlarmDAO depAlarmDAO = new DepartureAlarmDAO(DatabaseHelper.getInstance(argContext));
        final boolean Created = depAlarmDAO.create(depAlarm);


        if (!Created)
        {
            String text = argContext.getString(R.string.error_creation_alarm);
            if (argView != null)
            {
                Snackbar.make(argView, text, Snackbar.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(argContext, text, Toast.LENGTH_LONG).show();
            }
            return false;
        }



        PendingIntent pendIntent = PendingIntent.getBroadcast(argContext.getApplicationContext(), argDeparture.getCode(), newIntent, 0);

        alarmManager.set(AlarmManager.RTC, calAlarm.getTimeInMillis(), pendIntent);
        argDeparture.setAlarm(true);

        String text = argContext.getString(R.string.you_will_receive_bus_will_arrive_notif, argMinutesBefore);

        if (argView != null)
        {
            Snackbar.make(argView, text, Snackbar.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(argContext, text, Toast.LENGTH_LONG).show();
        }

        return true;
    }

    public static boolean canAddAlarm(Departure argDeparture, int argMinutesBefore) {
        Calendar calAlarm = Calendar.getInstance();
        //calAlarm.setTimeInMillis(argDeparture.getDate().getTimeInMillis());

        //calAlarm.add(Calendar.MINUTE,Minutes * -1);
        //calAlarm.set(Calendar.SECOND, 0);

        Calendar now = Calendar.getInstance();
        Calendar departure = Calendar.getInstance();
        departure.setTimeInMillis(argDeparture.getDate().getTimeInMillis());
        departure.set(Calendar.SECOND, 0);

        final long DifferenceSeconds = DateTools.diffBetweenDates(departure,now,Calendar.SECOND);

        final int MinutesBefore = (int) (DifferenceSeconds - (argMinutesBefore * 60));
        calAlarm.setTimeInMillis(now.getTimeInMillis());
        calAlarm.add(Calendar.SECOND, MinutesBefore);

        calAlarm.set(Calendar.SECOND, 0);
        return MinutesBefore >= 60;

    }
}