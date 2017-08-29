package com.mytpg.engines.entities.interfaces;

/**
 * Created by BlueEyesSmile on 22.09.2016.
 */

public interface IItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
