package com.mytpg.engines.tools;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by BlueEyesSmile on 24.09.2016.
 */

public abstract class LocationTools {

    public static LatLng locToLatLng(Location argLoc)
    {
        return new LatLng(argLoc.getLatitude(), argLoc.getLongitude());
    }
}
