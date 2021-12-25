package com.mygdx.kaps.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Grid {
    private static class Column {
        private final List<Optional<Capsule>> tiles;

        private Column(int tiles) {
            if (tiles <= 0) throw new IllegalArgumentException("Invalid column size: " + tiles);
            this.tiles = new ArrayList<>(tiles);
            IntStream.range(0, tiles).forEach(c -> this.tiles.add(Optional.empty()));
        }

        public int height() {
            return tiles.size();
        }

        public void set(int y, Capsule caps) {
            tiles.set(y, Optional.of(caps));
        }

        public Optional<Capsule> get(int i) {
            return tiles.get(i);
        }
    }

    private final List<Column> columns;

    public Grid(int columns, int rows) {
        if (columns < 2) throw new IllegalArgumentException("Insufficient grid width: " + columns);
        if (rows < 2) throw new IllegalArgumentException("Insufficient grid height: " + rows);
        this.columns = new ArrayList<>(rows);
        IntStream.range(0, columns).forEach(c -> this.columns.add(new Column(rows)));
    }

    public Optional<Capsule> get(int x, int y) {
        return columns.get(x).get(y);
    }

    public int getWidth() {
        return columns.size();
    }

    public int getHeight() {
        return columns.get(0).height();
    }

    public void put(Capsule caps) {
        set(caps.getCoordinates().x, caps.getCoordinates().y, caps);
    }

    private void set(int x, int y, Capsule caps) {
        columns.get(x).set(y, caps);
    }
}
