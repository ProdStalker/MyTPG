package com.mytpg.engines.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.mytpg.engines.R;
import com.mytpg.engines.entities.Line;

/**
 * Created by stalker-mac on 13.11.14.
 */
public abstract class LineTools {

    public static void configureTextView(TextView ArgTV, final Line ArgLine) {
        if (ArgTV == null || ArgLine == null) {
            return;
        }

        int textColor = Color.BLACK;
        int backgroundColor = Color.WHITE;
        int strokeColor = Color.BLACK;

        GradientDrawable gd;
        try {
            gd = (GradientDrawable) ArgTV.getBackground();
        } catch (Exception ex) {
            ex.printStackTrace();
            gd = null;
        }

        if (ArgTV.getText().toString().equalsIgnoreCase("...")) {
            textColor = Color.WHITE;
            backgroundColor = Color.BLUE;
        } else {
            if (ArgLine.getColor() != -1) {
                textColor = Color.WHITE;
                backgroundColor = ArgLine.getColor();
                strokeColor = backgroundColor;
            }
        }


        if (gd == null){
            ArgTV.setBackgroundColor(backgroundColor);
        }
        else{
            gd.setStroke(1, strokeColor);
            gd.setColor(backgroundColor);

            if (Build.VERSION.SDK_INT >= 16) {
                ArgTV.setBackground(gd);
            } else {
                //noinspection deprecation
                ArgTV.setBackgroundDrawable(gd);
            }
        }
        ArgTV.setTextColor(textColor);
    }

    public static void configureRemoteTextView(Context argContext, int argId, RemoteViews argRV, final Line argLine)
    {
        if (argId == -1 || argRV == null || argLine == null) {
            return;
        }

        int textColor = Color.BLACK;
        int backgroundColor = Color.WHITE;

        if (argLine.getColor() != -1) {
            textColor = Color.WHITE;
            backgroundColor = argLine.getColor();
        }

        Drawable circle = argContext.getResources().getDrawable(R.drawable.circle);
        circle.setColorFilter(backgroundColor, PorterDuff.Mode.SRC);

       argRV.setInt(argId, "setBackground",
                backgroundColor);
        argRV.setInt(argId, "setTextColor",
               textColor);
    }

    public static String fromCFFCategory(String argCategory) {
        if (argCategory.equalsIgnoreCase("NFB") || argCategory.equalsIgnoreCase("NFO"))
        {
            return "Bus";
        }
        else if (argCategory.equalsIgnoreCase("NFT"))
        {
            return "Tram";
        }

        return argCategory;
    }
}
