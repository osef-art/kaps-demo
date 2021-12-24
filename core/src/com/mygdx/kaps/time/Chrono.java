package com.mygdx.kaps.time;

import com.badlogic.gdx.utils.TimeUtils;

class Chrono {
    private long start;

    public Chrono() {
        reset();
    }

    public double value() {
        return TimeUtils.nanoTime() - start;
    }

    public void reset() {
        start = TimeUtils.nanoTime();
    }
}
