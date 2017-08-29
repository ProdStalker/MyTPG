/**
 * 
 */
package com.mytpg.program.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.mytpg.engines.data.dao.TicketDAO;
import com.mytpg.engines.entities.Ticket;
import com.mytpg.engines.entities.db.DatabaseHelper;
import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.NotificationSettings;
import com.mytpg.engines.settings.RequestCodeSettings;
import com.mytpg.engines.settings.TicketSettings;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.R;
import com.mytpg.program.core.App;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * @author StalkerA
 *
 */
public class SmsReceiver extends BroadcastReceiver {

	/**
	 * 
	 */
	public SmsReceiver() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@SuppressWarnings("static-access")
	@Override
	public void onReceive(Context context, Intent intent) {
		App app = (App) context.getApplicationContext();

        int minutesBeforePref = 5;

		if (app != null)
		{
			minutesBeforePref = Integer.valueOf(app.getSharedPreferences().getString(AppSettings.PREF_ALARM_TICKET_TIME, "5"));
		}
		
		Bundle bundle = intent.getExtras();
		if (bundle != null)
		{
			SmsMessage[] smsMessages;
			Object[] pdus = (Object[]) bundle.get("pdus");
			smsMessages = new SmsMessage[pdus.length];

			int i = 0;
			while (i < smsMessages.length)
			{
				smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				SmsMessage sms = smsMessages[i];
				if (sms.getOriginatingAddress().equalsIgnoreCase(TicketSettings.TPG_SMS_TICKETS_NUMBER))
				{

					String message = sms.getMessageBody();


					String lines[] = message.split("\\r?\\n");

					if (lines.length < 4)
					{
                        i++;
						continue;
					}

					String ticketTypeText = lines[1].trim();
                    TicketSettings.TicketType ticketType = TicketSettings.TicketType.Not_Defined;

                    int lineDate = -1;


					if (ticketTypeText.equalsIgnoreCase(TicketSettings.ALL_GENEVA))
					{
                        ticketType = TicketSettings.TicketType.All_Geneva;
                        lineDate = 2;
					}
                    else if (ticketTypeText.equalsIgnoreCase(TicketSettings.ALL_DAY))
                    {
                        ticketType = TicketSettings.TicketType.All_Day;
                        lineDate = 2;
                    }
                    else if (ticketTypeText.equalsIgnoreCase(TicketSettings.ALL_DAY_9H))
                    {
                        ticketType = TicketSettings.TicketType.All_Day_9h;
                        lineDate = 3;
                    }

                    if (ticketType == TicketSettings.TicketType.Not_Defined)
                    {
                        i++;
                        continue;
                    }

                    String dateValidity = lines[lineDate];
                    String dateArray[] = dateValidity.split("\\.");
					String hoursValidity = lines[lineDate+1];
					String hoursArray[] = hoursValidity.split(" ");
                    String ticketTarif = lines[lineDate+2];


                    if (dateArray.length != 3)
                    {
                        i++;
                        continue;
                    }


                    String day = dateArray[0];
                    String month = dateArray[1];
                    String year = dateArray[2];
                    if (year.length() == 2)
                    {
                        year = "20" + year;
                    }

					if (hoursArray.length < 3)
					{
                        i++;
						continue;
					}

					String realHour = hoursArray[0];
					String realHourArray[] = realHour.split(":");
					if (realHourArray.length != 2)
					{
                        i++;
						continue;
					}


					String hour = realHourArray[0];
					String minutes = realHourArray[1];

					Calendar smsCal = DateTools.now();
                    smsCal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
                    smsCal.set(Calendar.MONTH, Integer.valueOf(month)-1);
                    smsCal.set(Calendar.YEAR, Integer.valueOf(year));
					smsCal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
					smsCal.set(Calendar.MINUTE, Integer.valueOf(minutes));
                    smsCal.set(Calendar.SECOND,0);
                    smsCal.set(Calendar.MILLISECOND,0);

					
					//now.add(Calendar.MINUTE, 55);
					//now.add(Calendar.SECOND, 15);

                    boolean found;
                    boolean isFull;
                    if (ticketTarif.contains("1/2"))
                    {
                        isFull = false;
                        found = true;
                    }
                    else
                    {
                        isFull = true;
                        found = true;
                    }

                    Calendar tomorrow = DateTools.now();
                    tomorrow.set(Calendar.HOUR_OF_DAY,0);
                    tomorrow.set(Calendar.MINUTE,0);
                    tomorrow.set(Calendar.SECOND,0);
                    tomorrow.set(Calendar.MILLISECOND,0);
                    tomorrow.add(Calendar.DAY_OF_MONTH,1);
                    List<String> ticketCodes = new ArrayList<>();
                    switch (ticketType)
                    {
                        case All_Day:
                            ticketCodes.add(TicketSettings.ALL_DAY_CODE);
                            ticketCodes.add(TicketSettings.ALL_DAY_CODE_NOT_FULL);

                            if (smsCal.get(Calendar.HOUR_OF_DAY) >= 4)
                            {
                                smsCal.add(Calendar.DAY_OF_MONTH,1);
                            }

                            smsCal.set(Calendar.HOUR_OF_DAY,3);
                            smsCal.set(Calendar.MINUTE,30);
                        break;
                        case All_Day_9h:
                            ticketCodes.add(TicketSettings.ALL_DAY_9H_CODE);
                            ticketCodes.add(TicketSettings.ALL_DAY_9H_CODE_NOT_FULL);

                            if (smsCal.get(Calendar.HOUR_OF_DAY) >= 4)
                            {
                                smsCal.add(Calendar.DAY_OF_MONTH,1);
                            }

                            smsCal.set(Calendar.HOUR_OF_DAY,3);
                            smsCal.set(Calendar.MINUTE,30);
                        break;
                        case All_Geneva:
                            ticketCodes.add(TicketSettings.ALL_GENEVA_CODE);
                            ticketCodes.add(TicketSettings.ALL_GENEVA_CODE_NOT_FULL);

                            smsCal.add(Calendar.HOUR_OF_DAY, 1);
                        break;
                        case Not_Defined:
                        break;
                    }

                    if (ticketType == TicketSettings.TicketType.Not_Defined || !found) {
                        i++;
                        continue;
                    }


                    if (found)
                    {
                        DatabaseHelper db = DatabaseHelper.getInstance(context);
                        TicketDAO ticketDAO = new TicketDAO(db);

                        List<Ticket> tickets = ticketDAO.getAll(isFull);
                        Ticket ticket = null;

                        for (Ticket currentTicket : tickets)
                        {
                            if (ticketCodes.contains(currentTicket.getCode()))
                            {
                                ticket = currentTicket;
                                break;
                            }
                        }

                        if (ticket != null)
                        {

                            ticket.setDate(smsCal);
                            ticketDAO.update(ticket);
                            smsCal.add(Calendar.MINUTE,minutesBeforePref * -1);

                        }
                    }


                    Toast.makeText(context, context.getString(R.string.you_will_receive_ticket_expires_notif, minutesBeforePref), Toast.LENGTH_LONG).show();
					
					Intent newIntent = new Intent(context,AlarmReceiver.class);
					newIntent.putExtra(AlarmReceiver.ARG_NOTIF_TYPE, NotificationSettings.NOTIF_TICKET_EXPIRES);
					PendingIntent pendIntent = PendingIntent.getBroadcast(context, RequestCodeSettings.REQ_CODE_TICKET_EXPIRES, newIntent, 0);

					AlarmManager alarmManager = (AlarmManager)context.getApplicationContext().getSystemService(context.getApplicationContext().ALARM_SERVICE);
					alarmManager.set(AlarmManager.RTC, smsCal.getTimeInMillis(), pendIntent);
				}
				i++;
			}
		}
	}

    /*private void showMessage(Context ArgContext,String ArgText) {

        Toast.makeText(ArgContext, ArgText, Toast.LENGTH_SHORT).show();
    }*/

}
