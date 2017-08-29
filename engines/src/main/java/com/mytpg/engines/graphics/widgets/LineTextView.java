package com.mytpg.engines.graphics.widgets;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mytpg.engines.R;
import com.mytpg.engines.entities.Line;
import com.mytpg.engines.tools.LineTools;

/**
 * Created by stalker-mac on 01.02.17.
 */

public class LineTextView extends TextView {
    public LineTextView(Context context) {
        super(context);
    }

    public LineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(21)
    public LineTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void custom(Line argLine)
    {
        Drawable bg;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bg = getResources().getDrawable(R.drawable.circle, null);
        }
        else
        {
            bg = getResources().getDrawable(R.drawable.circle);
        }
        if (Build.VERSION.SDK_INT >= 16) {
            setBackground(bg);
        } else {
            //noinspection deprecation
            setBackgroundDrawable(bg);
        }
        LineTools.configureTextView(this, argLine);
    }
}
