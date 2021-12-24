package com.mygdx.kaps.level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Grid {
    private static class Column {
        private final List<Tile> tiles;

        private Column(int tiles) {
            if (tiles <= 0) throw new IllegalArgumentException("Invalid column size: " + tiles);
            this.tiles = new ArrayList<>(tiles);
            IntStream.range(0, tiles).forEach(c -> this.tiles.add(new Tile()));
        }
    }
    private final List<Column> columns;
    private final Capsule.Position dimensions;

    public Grid(int columns, int rows) {
        if (columns < 2) throw new IllegalArgumentException("Insufficient grid width: " + columns);
        if (rows < 2) throw new IllegalArgumentException("Insufficient grid height: " + rows);
        this.columns = new ArrayList<>(rows);
        IntStream.range(0, columns).forEach(c -> this.columns.add(new Column(rows)));
        dimensions = new Capsule.Position(columns, rows);
    }

    public Capsule.Position getDimensions() {
        return dimensions;
    }
}
