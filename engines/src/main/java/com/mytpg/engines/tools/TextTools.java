package com.mytpg.engines.tools;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Created by BlueEyesSmile on 28.09.2016.
 */

public abstract class TextTools {
    public static String removeAccent(String argText)
    {
        String textWithoutAccent = Normalizer.normalize(argText, Normalizer.Form.NFD);
        textWithoutAccent = textWithoutAccent.replaceAll("[^\\p{ASCII}]", "");
        textWithoutAccent= textWithoutAccent.toLowerCase(Locale.FRENCH);

        return textWithoutAccent;
    }
}
