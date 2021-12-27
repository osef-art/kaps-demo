package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Objects;

interface IGridObject {
    Sprite getSprite();

    boolean isGerm();

    boolean isCapsule();
}

abstract class GridObject implements IGridObject {
    private final Coordinates coordinates;
    private final Color color;

    GridObject(Coordinates coordinates, Color color) {
        Objects.requireNonNull(coordinates);
        this.coordinates = coordinates.copy();
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
}
