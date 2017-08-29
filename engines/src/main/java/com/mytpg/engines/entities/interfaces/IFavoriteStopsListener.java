package com.mytpg.engines.entities.interfaces;

/**
 * Created by BlueEyesSmile on 27.09.2016.
 */

public interface IFavoriteStopsListener {
    void onUpdatedAll();
    void onMoved(int argFrom, int argTo);
    void onDissmiss(int argPosition);
}
