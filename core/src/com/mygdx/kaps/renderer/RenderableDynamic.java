package com.mygdx.kaps.renderer;

import com.badlogic.gdx.math.Rectangle;

/**
 * Interface for elements that can be rendered depending on their position
 */
public interface RenderableDynamic extends Renderable {
    void render(float x, float y, float width, float height);

    default void render(Rectangle zone) {
        render(zone.x, zone.y, zone.width, zone.height);
    }
}
