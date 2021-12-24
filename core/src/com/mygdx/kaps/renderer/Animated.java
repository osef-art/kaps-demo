package com.mygdx.kaps.renderer;

/**
 * Interface for objects that can be rendered with several frames depending on com.mygdx.kaps.time.
 */
public interface Animated extends Renderable {
    void update();
}
