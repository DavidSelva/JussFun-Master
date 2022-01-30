package com.app.jussfun.helper;

import java.util.Observable;

/**
 * Created by Jussfun on 26/2/18.
 */

public class FragmentObserver extends Observable {
    @Override
    public void notifyObservers() {
        setChanged(); // Set the changed flag to true, otherwise observers won't be notified.
        super.notifyObservers();
    }
}
