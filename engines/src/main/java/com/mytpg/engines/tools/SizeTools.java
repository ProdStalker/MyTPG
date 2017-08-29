package com.mytpg.engines.tools;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by stalker-mac on 13.11.14.
 */
public abstract class SizeTools {
    public static int dpToPx(Context ArgContext, int ArgDp)
    {
        final float d = ArgContext.getResources().getDisplayMetrics().density;
        final int px = (int)(ArgDp * d); // margin in pixels

        return px;
    }

    public static int calculateNoOfColumns(Context argContext) {
        DisplayMetrics displayMetrics = argContext.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 60);
        return noOfColumns;
    }
}
