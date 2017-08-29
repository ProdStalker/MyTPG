package com.mytpg.engines.settings;

/**
 * Created by stalker-mac on 08.11.14.
 */
public abstract class DataSettings {
    public final static String API_BASE_URL = "http://prod.ivtr-od.tpg.ch/v1/";//http://rtpi.data.tpg.ch/v1/";
    public final static String API_STOPS_ENDPOINT = "GetStops.json";
    public final static String API_PHYSICAL_STOPS_ENDPOINT = "GetPhysicalStops.json";
    public final static String API_LINES_ENDPOINT = "GetLinesColors.json";
    public final static String API_DISRUPTIONS_ENDPOINT =  "GetDisruptions.json";
    public final static String API_NEXT_DEPARTURES_ENDPOINT = "GetNextDepartures.json?";
    public final static String API_ALL_NEXT_DEPARTURES_ENDPOINT = "GetAllNextDepartures.json?";
    public final static String API_THERMOMETER = "GetThermometer.json";
    public final static String API_KEY = "4c5082e0-f5d9-11e3-948b-0002a5d5c51b";
    public final static String API_URL_KEY = "key=" + API_KEY;

    public final static String API_JSON_TPG_TIMESTAMP = "timestamp";
   // http://m.tpg.ch/thermometer.htm?destination=RIVE&ligne=10&mnemoDepart=BAIR&horaireRef=11458
    /***********************************************************************************************
     *                       ABOUT OFFICIAL TPG MOBILE WEBSITE                                     *
     ***********************************************************************************************/
    public final static String URL_MOBILE_TPG_HOST = "http://m.tpg.ch/";

    public final static String URL_MOBILE_TPG_ENDPOINT_DEPARTURE = "stopDisplay.htm";
    public final static String URL_MOBILE_TPG_BASE_DEPARTURE = URL_MOBILE_TPG_HOST + URL_MOBILE_TPG_ENDPOINT_DEPARTURE;
    public final static String URL_MOBILE_TPG_DEPARTURE = URL_MOBILE_TPG_BASE_DEPARTURE + "?mnemo=%1$s";
    public final static String URL_MOBILE_TPG_DEPARTURE_WITH_CODE = URL_MOBILE_TPG_DEPARTURE + "&horaireRef=%2$s";
    public final static String URL_TPG_ENDPOINT_DIRECTIONS = "bin/tp/query.exe";

    public final static String URL_MOBILE_TPG_ENDPOINT_THERMOMETER = "thermometer.htm";
    public final static String URL_MOBILE_TPG_BASE_THERMOMETER = URL_MOBILE_TPG_HOST + URL_MOBILE_TPG_ENDPOINT_THERMOMETER;
    public final static String URL_MOBILE_TPG_THERMOMETER = URL_MOBILE_TPG_BASE_THERMOMETER + "?destination=%1$s&ligne=%2$s&mnemoDepart=%3$s&horaireRef=%4$s";

    public final static String URL_MOBILE_TPG_ENDPOINT_TIMETABLE = "timetable.htm";
    public final static String URL_MOBILE_TPG_BASE_TIMETABLE = URL_MOBILE_TPG_HOST + URL_MOBILE_TPG_ENDPOINT_TIMETABLE;
    public final static String URL_MOBILE_TPG_TIMETABLE = URL_MOBILE_TPG_BASE_TIMETABLE + "?destination=%1$s&ligne=%2$s&mnemoDepart=%3$s";

    /***********************************************************************************************
     *                                   ABOUT API OPENDATA                                        *
     ***********************************************************************************************/
    public final static String API_OPENDATA_BASE_URL = "http://transport.opendata.ch/v1/";
    public final static String API_OPENDATA_DIRECTIONS_ENDPOINT =  "connections";

    /***********************************************************************************************
     *                                   ABOUT API BUSTED                                          *
     ***********************************************************************************************/
    public static final String BUSTED_API_BASE_URL = "http://busted-app.com/";
    public static final String BUSTED_API_PLATFORM = "a";
    public static final String BUSTED_API_VERSION = "3";
    public static final String BUSTED_API_ALL_ALERTS_ENDPOINT = "alerts-1.json";
    public static final String BUSTED_API_ALL_STOPS_ENDPOINT = "dataset-1.json";
}
