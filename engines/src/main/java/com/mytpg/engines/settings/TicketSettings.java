package com.mytpg.engines.settings;

/**
 * Created by stalker-mac on 26.01.15.
 */
public class TicketSettings {
    public static final String ALL_GENEVA = "BILLET TOUT GENEVE";
    public static final String ALL_DAY = "CJ TOUT GENEVE";
    public static final String ALL_DAY_9H = "CJ dès 9h";
    public static final String ALL_GENEVA_CODE = "tpg1";
    public static final String ALL_GENEVA_CODE_NOT_FULL = "tpg2";
    public static final String ALL_DAY_CODE = "cj1";
    public static final String ALL_DAY_CODE_NOT_FULL = "cj2";
    public static final String ALL_DAY_9H_CODE = "cj91";
    public static final String ALL_DAY_9H_CODE_NOT_FULL = "cj92";

    public enum TicketType {Not_Defined,All_Geneva, All_Day, All_Day_9h}


    public final static String TPG_SMS_TICKETS_NUMBER = "788";

}
