package com.mytpg.engines.data.interfaces.listeners;

import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by BlueEyesSmile on 24.09.2016.
 */

public interface ISearchListener {
    interface BoundsListener {
        void onSuccess(LatLngBounds argLatLngBounds);
        void onNotFound();
    }
}
