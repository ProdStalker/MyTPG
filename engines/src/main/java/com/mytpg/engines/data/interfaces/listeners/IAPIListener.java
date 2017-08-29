package com.mytpg.engines.data.interfaces.listeners;

import com.android.volley.VolleyError;

import java.util.List;


/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public interface IAPIListener<T> {
    void onError(VolleyError argVolleyError);
    void onSuccess(T argObject);
    void onSuccess(List<T> argObjects);
}
