package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.renderer.SpriteData;

import java.util.Objects;

interface IGridObject {
    boolean isGerm();

    boolean isCapsule();

    boolean isDropping();

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

    public void repaint(Color color) {
        this.color = color;
    }
}
