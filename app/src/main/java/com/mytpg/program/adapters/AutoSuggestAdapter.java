package com.mytpg.program.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.mytpg.program.R;


/**
 * Created by BlueEyesSmile on 24.09.2016.
 */

public class AutoSuggestAdapter extends CursorAdapter{
    private LayoutInflater mCursorInflater = null;


    public AutoSuggestAdapter(Context argContext, Cursor argCursor, int argFlags)
    {
        super(argContext,argCursor,argFlags);
        mCursorInflater = (LayoutInflater) argContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mCursorInflater.inflate(R.layout.item_auto_suggest,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTV = (TextView)view.findViewById(R.id.nameTV);
        nameTV.setText(cursor.getString(cursor.getColumnIndex("name")));
    }

}
