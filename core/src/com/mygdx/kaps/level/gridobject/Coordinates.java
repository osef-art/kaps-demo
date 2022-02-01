package com.mygdx.kaps.level.gridobject;

import java.util.function.Function;

public class Coordinates {
    public int x, y;

    public Coordinates() {
        this(0, 0);
    }

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + " , " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinates)) return false;
        Coordinates that = (Coordinates) o;
        return x == that.x && y == that.y;
    }

    /**
     * @param functionX the effect to apply on x
     * @param functionY the effect to apply on y
     * @return new coordinates mapped with both functions
     */
    Coordinates mapped(Function<Integer, Integer> functionX, Function<Integer, Integer> functionY) {
        Coordinates coordinates = new Coordinates(x, y);
        coordinates.map(functionX, functionY);
        return coordinates;
    }

    Coordinates mapped(Function<Integer, Integer> function) {
        return mapped(function, function);
    }

    Coordinates copy() {
        return mapped(Function.identity());
    }

    public Coordinates addedTo(Coordinates coordinates) {
        return addedTo(coordinates.x, coordinates.y);
    }

    public Coordinates addedTo(int addedX, int addedY) {
        return mapped(x -> x + addedX, y -> y + addedY);
    }

    /**
     * Applies {@param functionX} to x and {@param functionY} to y.
     *
     * @param functionX the effect to apply on x
     * @param functionY the effect to apply on y
     */
    private void map(Function<Integer, Integer> functionX, Function<Integer, Integer> functionY) {
        x = functionX.apply(x);
        y = functionY.apply(y);
    }

    public void set(int x, int y) {
        map(n -> x, n -> y);
    }

    void add(int x, int y) {
        map(n -> n + x, n -> n + y);
    }

    public void set(Coordinates coordinates) {
        set(coordinates.x, coordinates.y);
    }

    void add(Coordinates coordinates) {
        add(coordinates.x, coordinates.y);
    }
}
