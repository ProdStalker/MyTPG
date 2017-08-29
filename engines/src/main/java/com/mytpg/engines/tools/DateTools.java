package com.mytpg.engines.tools;

import com.mytpg.engines.settings.DateSettings;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by stalker-mac on 08.11.14.
 */
public abstract class  DateTools {
    public static int SECOND_IN_MILLISECONDS = 1000;
    public static int MINUTE_IN_MILLISECONDS = SECOND_IN_MILLISECONDS * 60;
    public static int HOUR_IN_MILLISECONDS = MINUTE_IN_MILLISECONDS * 60;
    public static int DAY_IN_MILLISECONDS = HOUR_IN_MILLISECONDS * 24;

    public static Calendar zeroMillis() {
        Calendar cal = DateTools.now();
        cal.setTimeInMillis(0);

        return cal;
    }

    public enum ComparisonType {OnlyDay, All}
    public enum FormatType {DirectionDate,DirectionTime, OnlyDate, OnlyHour, OnlyHourWithoutSeconds, WithoutSeconds, SearchDirectionsDate, SearchTPGDirectionsDate, SearchDirectionsHour}
    private static Calendar ms_currentDate = Calendar.getInstance(TimeZone.getTimeZone(DateSettings.DEFAULT_TIMEZONE));

    public static Calendar dateAPIToLocaleDate(String ArgTimestamp) {
        Calendar date = Calendar.getInstance();

        if (!ArgTimestamp.isEmpty())
        {
            ArgTimestamp = ArgTimestamp.substring(0,ArgTimestamp.length()-5);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            //DateFormat df = DateFormat.getDateTimeInstance();
            try {
                date.setTime(sdf.parse(ArgTimestamp));
                //date.set(Calendar.SECOND, 0);
            } catch (ParseException e) {
                e.printStackTrace();

                date.setTimeInMillis(0);
            }
        }
        else
        {
            date.setTimeInMillis(0);
        }

        return date;
    }

    public static boolean dateAreDifferent(final Calendar ArgFirstDate, final Calendar ArgSecondDate,final ComparisonType ArgComparisonType)
    {
        if (ArgFirstDate == null && ArgSecondDate == null)
        {
            return false;
        }
        else if ((ArgFirstDate == null && ArgSecondDate != null) || (ArgFirstDate != null && ArgSecondDate == null))
        {
            return true;
        }
        else if (ArgFirstDate.get(Calendar.DAY_OF_MONTH) != ArgSecondDate.get(Calendar.DAY_OF_MONTH) ||
                ArgFirstDate.get(Calendar.MONTH) != ArgSecondDate.get(Calendar.MONTH) ||
                ArgFirstDate.get(Calendar.YEAR) != ArgSecondDate.get(Calendar.YEAR))
        {
            return true;
        }
        else if (ArgComparisonType == ComparisonType.OnlyDay)
        {
            return false;
        }
        else if(ArgFirstDate.get(Calendar.HOUR_OF_DAY) != ArgSecondDate.get(Calendar.HOUR_OF_DAY) ||
                    ArgFirstDate.get(Calendar.MINUTE) != ArgSecondDate.get(Calendar.MINUTE) ||
                    ArgFirstDate.get(Calendar.SECOND) != ArgSecondDate.get(Calendar.SECOND))
        {
            return true;
        }

        return false;
    }

    public static String dateToString(final Calendar ArgCalendar)
    {
        String dateString = "";

        if (ArgCalendar != null)
        {
            DateFormat df = DateFormat.getDateTimeInstance();
            //df.setTimeZone(ArgCalendar.getTimeZone());
            dateString = df.format(ArgCalendar.getTime());
        }

        return dateString;
    }

    public static String dateToString(final Calendar ArgCalendar, final FormatType ArgFormatType)
    {
        String dateString = "";

        if (ArgCalendar != null)
        {
            DateFormat df;

            String format;
            Locale locale = Locale.getDefault();
            switch (ArgFormatType)
            {
                case DirectionDate:
                    //String dayOfWeek = new SimpleDateFormat("E",Locale.FRENCH).format(ArgCalendar.getTime());
                    format = "E, dd.MM.yy ";
                    if (locale.getLanguage().equalsIgnoreCase("en"))
                    {
                        format += "hh:mm a";
                    }
                    else
                    {
                        format += "HH:mm";
                    }
                    dateString = new SimpleDateFormat(format, locale).format(ArgCalendar.getTime());
                    //dateString = dateString.substring(0,2) + dateString.substring(3);
                break;
                case DirectionTime:
                    format = "HH:mm";
                    dateString = new SimpleDateFormat(format, Locale.FRENCH).format(ArgCalendar.getTime());
                    break;
                case OnlyDate:
                    df = DateFormat.getDateInstance();
                    dateString = df.format(ArgCalendar.getTime());
                break;
                case OnlyHour:
                    df = DateFormat.getTimeInstance();
                    dateString = df.format(ArgCalendar.getTime());
                break;
                case OnlyHourWithoutSeconds:
                    format = "HH:mm";
                    if (locale.getLanguage().equalsIgnoreCase("en"))
                    {
                        format = "hh:mm a";
                    }
                    dateString = new SimpleDateFormat(format,locale).format(ArgCalendar.getTime());
                break;
                case SearchDirectionsDate:
                    format = "yyyy-MM-dd";
                    dateString = new SimpleDateFormat(format).format(ArgCalendar.getTime());
                break;
                case SearchTPGDirectionsDate:
                    format = "dd.MM.yyyy";
                    dateString = new SimpleDateFormat(format).format(ArgCalendar.getTime());
                break;
                case SearchDirectionsHour:
                    format = "HH:mm";
                    dateString = new SimpleDateFormat(format).format(ArgCalendar.getTime());
                break;
                case WithoutSeconds:
                    dateString = dateToString(ArgCalendar,FormatType.OnlyDate) + " " + dateToString(ArgCalendar,FormatType.OnlyHourWithoutSeconds);
                break;
            }
        }

        return dateString;
    }

    public static long diffBetweenDates(Calendar argFirstDate, Calendar argSecondDate, int argFieldComparison)
    {
        long difference = argFirstDate.getTimeInMillis() - argSecondDate.getTimeInMillis();

        switch (argFieldComparison)
        {
            case Calendar.SECOND :
                difference = difference / SECOND_IN_MILLISECONDS;
            break;
            case Calendar.MINUTE :
                difference = difference / MINUTE_IN_MILLISECONDS;
            break;
            case Calendar.HOUR :
                difference = difference / HOUR_IN_MILLISECONDS;
            break;
            case Calendar.DAY_OF_MONTH :
                difference = difference / DAY_IN_MILLISECONDS;
            break;
        }

        return difference;
    }

    public static Calendar getCurrentDate()
    {
        return ms_currentDate;
    }

    public static Calendar getLastDay(int ArgDayWanted, int ArgMonth, int ArgYear)
    {
        if (ArgDayWanted < Calendar.SUNDAY || ArgDayWanted > Calendar.SATURDAY)
        {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, ArgMonth);
        cal.set(Calendar.YEAR, ArgYear);

        final int NumberDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int i = NumberDaysInMonth;
        while (i >= 1)
        {
            cal.set(Calendar.DAY_OF_MONTH, i);
            if (cal.get(Calendar.DAY_OF_WEEK) == ArgDayWanted)
            {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            }
            i--;
        }

        return cal;
    }

    public static Calendar now(){
        return Calendar.getInstance(TimeZone.getTimeZone(DateSettings.DEFAULT_TIMEZONE));
    }

    public static boolean serviceAvailable() {
        Calendar now = DateTools.now();
        final int HourDay = now.get(Calendar.HOUR_OF_DAY);
        final int Minute = now.get(Calendar.MINUTE);
        final int TotalMinute = HourDay * 60 + Minute;
        final int Begin = 3 * 60 + 30;
        final int End = 4 * 60 + 15;

        return !(Begin <= TotalMinute && End >= TotalMinute);

    }

    public static void setCurrentDate(Calendar ArgCurrentDate)
    {
        if (ArgCurrentDate ==  null)
        {
            ArgCurrentDate = Calendar.getInstance(TimeZone.getTimeZone(DateSettings.DEFAULT_TIMEZONE));
        }

        ms_currentDate = ArgCurrentDate;
    }

    public static void setCurrentDate(String ArgTimestamp) {
        Calendar cal = Calendar.getInstance();
        if (ArgTimestamp != null && !ArgTimestamp.isEmpty())
        {
            cal = DateTools.dateAPIToLocaleDate(ArgTimestamp);
        }

        setCurrentDate(cal);
    }

    public static Calendar stringToDate(final String ArgDateString)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);

        if (ArgDateString != null && ArgDateString.length() > 0)
        {

            DateFormat df = DateFormat.getDateTimeInstance();
            try {
                cal.setTime(df.parse(ArgDateString));
            } catch (ParseException e) {
                e.printStackTrace();
                cal.setTimeInMillis(0);
            }
        }

        return cal;
    }

    public static Calendar stringToDate(final String ArgDateString, FormatType ArgFormatType)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);

        if (ArgDateString != null && ArgDateString.length() > 0)
        {

            DateFormat df;
            if (ArgFormatType == FormatType.OnlyDate)
            {
                df = DateFormat.getDateInstance();
            }
            else
            {
                df = DateFormat.getTimeInstance();
            }

            try {
                cal.setTime(df.parse(ArgDateString));
            } catch (ParseException e) {
                e.printStackTrace();
                cal.setTimeInMillis(0);
            }
        }

        return cal;
    }

    public static Calendar utcToLocale(Calendar ArgDate)
    {
        Calendar now = Calendar.getInstance();


        final long Millis = ArgDate.getTimeInMillis();
        now.setTimeInMillis(Millis);



        return ArgDate;
    }
}
