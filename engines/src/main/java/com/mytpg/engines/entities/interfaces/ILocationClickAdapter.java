package com.mytpg.engines.entities.interfaces;

/**
 * Created by stalker-mac on 21.10.16.
 */

public interface ILocationClickAdapter {
    void onLocationAsked(int argPosition);
    void onLocationAsked(int argPosition, boolean argIsFrom);
}
