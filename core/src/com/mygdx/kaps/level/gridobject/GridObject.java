package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;

import java.util.Objects;

interface IGridObject {
    default boolean isGerm() {
        return false;
    }

    default boolean isCapsule() {
        return false;
    }

    default boolean isDropping() {
        return false;
    }

    boolean isDestroyed();

    void takeHit();

    Sprite getSprite(SpriteData data);

    void repaint(Color color);
}

public abstract class GridObject implements IGridObject {
    private final Coordinates coordinates;
    private boolean destroyed;
    private Color color;

    GridObject(Coordinates coordinates, Color color) {
        this.color = color;
        this.coordinates = Objects.requireNonNull(coordinates).copy();
    }

    @Override
    public String toString() {
        return coordinates.toString();
    }

    public Coordinates coordinates() {
        return coordinates;
    }

    public Color color() {
        return color;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void takeHit() {
        destroyed = true;
    }

    public void repaint(Color color) {
        this.color = color;
    }

    public AnimatedSprite poppingAnim() {
        return SpriteData.poppingAnimation(color);
    }
}
