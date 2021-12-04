package com.app.jussfun.external;

import android.content.Context;
import android.view.MotionEvent;

public abstract class MyOnDoubleTapListener extends OnDoubleTapListener {
    public MyOnDoubleTapListener(Context c) {
        super(c);
    }

    @Override
    public void onDoubleTap(MotionEvent e) {
        super.onDoubleTap(e);
    }
}
