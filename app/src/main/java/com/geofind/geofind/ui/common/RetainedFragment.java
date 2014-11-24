package com.geofind.geofind.ui.common;

import android.app.Fragment;
import android.os.Bundle;

/**
 * A {@link android.app.Fragment} that retains it's instance so that the content would stay across
 * all the {@link android.app.Activity}'s lifecycle.
 * <p/>
 * Created by mickey on 06/10/14.
 */
public class RetainedFragment<T> extends Fragment {

    /**
     * Data object we want to retain.
     */
    private T data;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    /**
     * Set the data.
     * @param data The data to save.
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Get the data.
     * @return The saved data.
     */
    public T getData() {
        return data;
    }
}
