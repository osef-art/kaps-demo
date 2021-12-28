package com.mygdx.kaps.level;

import java.util.function.Function;

class Coordinates {
    int x, y;

    Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + " , " + y + ")";
    }

    /**
     * Applies {@param functionX} to x and {@param functionY} to y.
     * @param functionX the effect to apply on x
     * @param functionY the effect to apply on y
     */
    private void map(Function<Integer, Integer> functionX, Function<Integer, Integer> functionY) {
        x = functionX.apply(x);
        y = functionY.apply(y);
    }

    void set(Coordinates coordinates) {
        map(x -> coordinates.x, y -> coordinates.y);
    }

    void add(Coordinates coordinates) {
        map(x -> x + coordinates.x, y -> y + coordinates.y);
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

    Coordinates addedTo(Coordinates coordinates) {
        return addedTo(coordinates.x, coordinates.y);
    }

    Coordinates addedTo(int addedX, int addedY) {
        return mapped(x -> x + addedX, y -> y + addedY);
    }
}
