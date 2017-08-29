package com.mytpg.engines.tools;

import android.graphics.Color;

/**
 * Created by stalker-mac on 19.10.16.
 */

public abstract class ColorTools {
    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
