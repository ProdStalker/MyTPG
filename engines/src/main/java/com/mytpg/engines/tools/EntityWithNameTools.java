package com.mytpg.engines.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import com.mytpg.engines.entities.core.EntityWithName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlueEyesSmile on 24.09.2016.
 */

public class EntityWithNameTools {
    public static Cursor toCursorAdapter(Context argContext, List<? extends EntityWithName> argEntityWithNames, int argToId, int argLayoutId)
    {
        String[] columnNames = {"_id","name"};
        MatrixCursor matrixCursor = new MatrixCursor(columnNames);

        List<String> names = new ArrayList<>();
        for (EntityWithName ewn : argEntityWithNames)
        {
            names.add(ewn.getName());
        }
        String[] array = (String[]) names.toArray();
        String[] temp = new String[2];
        int id = 0;
        for (String item : array)
        {
            temp[0] = Integer.toString(id++);
            temp[1] = item;
            matrixCursor.addRow(temp);
        }

        /*String[] from = {"name"};
        int[] to = {argToId};*/

        return matrixCursor;
        //return new SimpleCursorAdapter(argContext, argLayoutId, matrixCursor, from, to);
    }
}
