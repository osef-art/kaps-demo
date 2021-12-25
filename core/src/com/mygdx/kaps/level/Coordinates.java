package com.mygdx.kaps.level;

import java.util.function.Function;

public class Coordinates {
    int x, y;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + " , " + y + ")";
    }

    private Coordinates mapped(Function<Integer, Integer> functionX, Function<Integer, Integer> functionY) {
        Coordinates coordinates = new Coordinates(x, y);
        coordinates.map(functionX, functionY);
        return coordinates;
    }

    public Coordinates mapped(Function<Integer, Integer> function) {
        return mapped(function, function);
    }

    private void map(Function<Integer, Integer> functionX, Function<Integer, Integer> functionY) {
        x = functionX.apply(x);
        y = functionY.apply(y);
    }

    private void map(Function<Integer, Integer> function) {
        map(function, function);
    }

    public Coordinates addedTo(Coordinates coordinates) {
        return mapped(x -> x + coordinates.x, y -> y + coordinates.y);
    }

    public void set(Coordinates coordinates) {
        map(x -> coordinates.x, y -> coordinates.y);
    }

    public void add(Coordinates coordinates) {
        map(x -> x + coordinates.x, y -> y + coordinates.y);
    }
}
