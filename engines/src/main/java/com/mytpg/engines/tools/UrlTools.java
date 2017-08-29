package com.mytpg.engines.tools;

/**
 * Created by stalker-mac on 10.11.16.
 */
abstract public class UrlTools {
    public static String format(String argText)
    {
        String formattedText = argText;

        formattedText = formattedText.replaceAll(" ", "%20");
        formattedText = formattedText.replaceAll("\\+", "%2B");
        formattedText = formattedText.replaceAll(",", "%2C");

        return formattedText;
    }
}
