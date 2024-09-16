package com.thewizrd.common.helpers;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public interface OnListChangedListener<T> {
    /**
     * Called whenever a change of unknown type has occurred, such as the entire list being
     * set to new values.
     *
     * @param sender The changing list.
     * @param args   The data for the onChanged event.
     */
    void onChanged(@NonNull ArrayList<T> sender, @NonNull ListChangedArgs<T> args);
}
