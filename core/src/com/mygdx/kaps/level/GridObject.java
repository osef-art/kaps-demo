package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Objects;

interface IGridObject {
    void updateSprite();

    Sprite getSprite();

    boolean isGerm();

    boolean isCapsule();

    boolean isDropping();

    boolean isDestroyed();

    void takeHit();
}

abstract class GridObject implements IGridObject {
    private final Coordinates coordinates;
    private final Color color;
    private boolean destroyed;

    GridObject(Coordinates coordinates, Color color) {
        this.coordinates = Objects.requireNonNull(coordinates).copy();
        this.color = color;
    }

    @Override
    public String toString() {
        return coordinates.toString();
    }

    Coordinates coordinates() {
        return coordinates;
    }

    Color color() {
        return color;
    }

    public boolean isCapsule() {
        return false;
    }

    public boolean isGerm() {
        return false;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isDropping() {
        return false;
    }

    public void takeHit() {
        destroyed = true;
    }

    public void updateSprite() {
    }
}
