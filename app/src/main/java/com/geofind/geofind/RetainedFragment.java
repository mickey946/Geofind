package com.geofind.geofind;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by mickey on 06/10/14.
 */
public class RetainedFragment<T> extends Fragment {

    // data object we want to retain
    private T data;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
