package com.app.jussfun.external.cardstackview.internal;

import android.view.animation.Interpolator;

import com.app.jussfun.external.cardstackview.Direction;


public interface AnimationSetting {
    Direction getDirection();
    int getDuration();
    Interpolator getInterpolator();
}
