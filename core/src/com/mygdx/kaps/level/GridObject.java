package com.mygdx.kaps.level;

public abstract class GridObject {
    private final Coordinates coordinates;
    private final Color color;

     GridObject(Coordinates coordinates, Color color) {
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

    public Color color() {
        return color;
    }
}
