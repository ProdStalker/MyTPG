/**
 * 
 */
package com.mytpg.program.services;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mytpg.engines.data.api.ThermometerAPI;
import com.mytpg.engines.data.interfaces.listeners.IAPIListener;
import com.mytpg.engines.entities.CheckPoint;
import com.mytpg.engines.entities.Departure;
import com.mytpg.engines.entities.Thermometer;
import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.NotificationSettings;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;
import com.mytpg.program.core.App;
import com.mytpg.program.fragments.ThermometerFragment;
import com.mytpg.program.receivers.AlarmReceiver;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author StalkerA
 *
 */
public class NotificationService extends Service {
	private static final String GROUP_DEPARTURE = "grpDepartures";
	//private static int ms_numberNotifs = 0;
	private NotificationManager m_notifManager;
	
	/**
	 * 
	 */
	public NotificationService() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("static-access")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		
		m_notifManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);
		
		int requestCode = -1;
		Bundle bundle = intent.getExtras();
		if (bundle != null)
		{
			requestCode = bundle.getInt(AlarmReceiver.ARG_NOTIF_TYPE,-1);
		}
		
		switch (requestCode)
		{
			case NotificationSettings.NOTIF_TICKET_EXPIRES :
				createTicketExpiresNotif(bundle);
			break;
			case NotificationSettings.NOTIF_DEPARTURE :
				createDepartureNotif(bundle);
			break;
			default :
				
			break;
		}
		
		
		return START_NOT_STICKY;
	}
	
	private void createDepartureNotif(Bundle ArgBundle) {
		final int DepartureCode = ArgBundle.getInt(ThermometerFragment.ARG_DEPARTURE_CODE,-1);
		//new ThermometerAsyncTask().execute(DepartureCode);
		
		ThermometerAPI thermometerAPI = new ThermometerAPI(getApplicationContext());
		thermometerAPI.getByCode(DepartureCode, new IAPIListener<Thermometer>() {
			@Override
			public void onError(VolleyError argVolleyError) {
				Toast.makeText(getApplicationContext(), argVolleyError.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onSuccess(Thermometer argObject) {
				CheckPoint chP;
				Thermometer thermo = argObject;
				if (thermo == null)
				{
					return;
				}
				
				chP = null;
				for (CheckPoint checkPoint : thermo.getCheckPoints())
				{
					if (checkPoint.getCode() == DepartureCode)
					{
						chP = checkPoint;
						break;
					}
				}
				Departure dep = new Departure();
				dep.setCode(DepartureCode);
				//AlarmTools.removeAlarm(getApplicationContext(), dep, false);

				createDepartureAlarmContentNotif(chP);
			}

			@Override
			public void onSuccess(List<Thermometer> argObjects) {

			}
		});
		
	}

	private NotificationCompat.Builder createNotification(final String ArgTitle, final String ArgContent, PendingIntent ArgPendingIntent, int notifType)
	{
		NotificationCompat.Builder notifCompatBuilder = new NotificationCompat.Builder(this.getApplicationContext());
		NotificationCompat.BigTextStyle notifStyle = new NotificationCompat.BigTextStyle();
		
		notifStyle.setBigContentTitle(ArgTitle);
		notifCompatBuilder.setTicker(ArgContent);
		notifCompatBuilder.setContentText(ArgContent);
		notifCompatBuilder.setAutoCancel(true);
		notifCompatBuilder.setSmallIcon(R.drawable.ic_launcher);
		notifCompatBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		notifCompatBuilder.setWhen(Calendar.getInstance().getTimeInMillis());
		notifCompatBuilder.setContentTitle(ArgTitle);
		notifCompatBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		notifCompatBuilder.setContentIntent(ArgPendingIntent);
		notifCompatBuilder.setStyle(notifStyle);
		notifStyle.bigText(ArgContent);
		
		//notifStyle.addLine(ArgContent);

		switch (notifType)
		{
			case NotificationSettings.NOTIF_DEPARTURE :
				notifCompatBuilder.setGroup(GROUP_DEPARTURE);
				break;
			case NotificationSettings.NOTIF_TICKET_EXPIRES:
				/*notifCompatBuilder.setAc
				notifCompatBuilder.addAction(YE)*/
			break;
		}
		return notifCompatBuilder;
	}

	private void createTicketExpiresNotif(Bundle ArgBundle) {
		Intent newIntent = new Intent(this.getApplicationContext(),MainActivity.class);
		newIntent.putExtra(MainActivity.ARG_FRAGMENT_WANTED,getString(R.string.menu_tickets));
		PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		App app = (App) getApplicationContext();

		int minutesBeforePref = 5;

		if (app != null)
		{
			minutesBeforePref = Integer.valueOf(app.getSharedPreferences().getString(AppSettings.PREF_ALARM_TICKET_TIME, "5"));
		}

		final String Text = getString(R.string.your_ticket_expires_in_minutes,minutesBeforePref);
		final String Title = getString(R.string.ticket_expiration);
		
		NotificationCompat.Builder notif = createNotification(Title, Text, pendingIntent, NotificationSettings.NOTIF_TICKET_EXPIRES);
		m_notifManager.notify(NotificationSettings.NOTIF_TICKET_EXPIRES, notif.build());
	}
	
	private void createDepartureAlarmContentNotif(CheckPoint argCheckPoint)
	{
		try
		{
			if (argCheckPoint == null)
			{
				return;
			}

			Intent newIntent = new Intent(getApplicationContext(),MainActivity.class);
			newIntent.putExtra(MainActivity.ARG_FRAGMENT_WANTED,getString(R.string.menu_thermometer));
			newIntent.putExtra(ThermometerFragment.ARG_DEPARTURE_CODE, argCheckPoint.getCode());
			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), argCheckPoint.getCode(), newIntent, PendingIntent.FLAG_UPDATE_CURRENT);


			//final int minutes = 5;
			String text = getString(R.string.your_bus_will_arrive,argCheckPoint.getLine().getName(), argCheckPoint.getStop().getName(), argCheckPoint.getLine().getArrivalStop().getName(),argCheckPoint.getArrivalTime());
			final String Title = getString(R.string.bus_arrival);

			final char[] Vowels = new char[]{'A','E','H','I','O','U','Y'};
			final String StopName = argCheckPoint.getLine().getArrivalStop().getName().toUpperCase(Locale.FRENCH);
			final char FirstChar = StopName.charAt(0);

			boolean found = false;
			int i = 0;
			while (i < Vowels.length)
			{
				if (FirstChar == Vowels[i])
				{
					found = true;
					break;
				}
				i++;
			}

			if (found)
			{
				final String Language = Locale.getDefault().getLanguage();
				if (Language.equalsIgnoreCase("fr"))
				{
					text = text.replace("de ", "d'");
				}
			}

			NotificationCompat.Builder notif = createNotification(Title, text, pendingIntent, NotificationSettings.NOTIF_DEPARTURE);
			m_notifManager.notify(NotificationSettings.NOTIF_DEPARTURE+argCheckPoint.getCode(), notif.build());

		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
