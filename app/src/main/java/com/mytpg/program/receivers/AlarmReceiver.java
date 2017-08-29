/**
 * 
 */
package com.mytpg.program.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mytpg.engines.settings.NotificationSettings;
import com.mytpg.program.fragments.ThermometerFragment;
import com.mytpg.program.services.NotificationService;

/**
 * @author StalkerA
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	public final static String ARG_NOTIF_TYPE = "NotifType";
	
	/**
	 * 
	 */
	public AlarmReceiver() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent alarmService = new Intent(context, NotificationService.class);
		Bundle bundle = intent.getExtras();
		if (bundle != null)
		{
			final int NotifType = bundle.getInt(ARG_NOTIF_TYPE,-1);
			if (NotifType != -1)
			{
				alarmService.putExtra(ARG_NOTIF_TYPE, NotifType);
				if (NotifType == NotificationSettings.NOTIF_DEPARTURE)
				{
					final int DepartureCode = bundle.getInt(ThermometerFragment.ARG_DEPARTURE_CODE);
					alarmService.putExtra(ThermometerFragment.ARG_DEPARTURE_CODE, DepartureCode);
				}
			}
			
		}
		context.startService(alarmService);
	}

}
